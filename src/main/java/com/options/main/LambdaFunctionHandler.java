package com.options.main;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.options.database.DBService;
import com.options.entities.DownloadableOption;
import com.options.entities.Option;
import com.options.entities.OptionMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

    public static LambdaLogger logger;

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static String API_TOKEN;
    public static String API_URL = "https://eodhistoricaldata.com/api/options/$symbol$?api_token=$api_token$";
    private static int MAX_OPTIONS_PER_THREAD = 2;
    public static int MAX_THREADS = 4;

    public static ObjectMapper objectMapper = new ObjectMapper();


    private static List<Option> options;
    private static List<OptionMetadata> optionsMetadata;



    @Override
    public String handleRequest(Object input, Context context) {
        logger = context.getLogger();

        long start = System.currentTimeMillis();

        try {
            DBService.setup();
        } catch (Exception e) {
            System.out.println("An exception occurred while setting up dabasae: " + e);
            return "Failed because of an excpetion while setting up DB: " + e;
        }

        API_TOKEN = System.getenv("API_TOKEN");
        if(API_TOKEN == null || API_TOKEN.isEmpty()){
            logger.log("Missing API_TOKEN parameter");
            return "Failed because of missing API_TOKEN";
        }

        String maxThreads = System.getenv("MAX_THREADS");
        if(maxThreads == null || maxThreads.isEmpty()){
            try{
                MAX_THREADS = Integer.parseInt(maxThreads);
            } catch (Exception e){
                logger.log("Failed to parse MAX_THREADS parmater, setting to default value of " + MAX_THREADS);
            }
        }

        String maxOptionsPerThread = System.getenv("MAX_OPTIONS_PER_THREAD");
        if(maxOptionsPerThread == null || maxOptionsPerThread.isEmpty()){
            try{
                MAX_OPTIONS_PER_THREAD = Integer.parseInt(maxOptionsPerThread);
            } catch (Exception e){
                logger.log("Failed to parse MAX_OPTIONS_PER_THREAD parmater, setting to default value of " + MAX_OPTIONS_PER_THREAD);
            }
        }


        API_URL = API_URL.replace("$api_token$", API_TOKEN);

        options = Collections.synchronizedList(new ArrayList<Option>());
        optionsMetadata = Collections.synchronizedList(new ArrayList<OptionMetadata>());


        List<DownloadableOption> optionSymbols = DBService.getOptionsList();
        ForkJoinPool pool = new ForkJoinPool(optionSymbols.size() >= MAX_THREADS ? MAX_THREADS : optionSymbols.size());


        pool.invoke(new OptionsRecursiveAction(optionSymbols));
        pool.shutdown();
        try {
            pool.awaitTermination(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.log("An exception occurred while waiting for options processing: " + e);
        }

        logger.log("Options processing done");

        try {
            if(!options.isEmpty())
                DBService.saveBatch(options, "stock_option_eod");
        } catch (Exception e) {
            logger.log("An exception occurred while saving options batch: " + e);
        }
        try {
            if(!optionsMetadata.isEmpty())
                DBService.saveMetadataBatch(optionsMetadata);
        } catch (Exception e) {
            logger.log("An exception occurred while saving options metadata batch: " + e);
        }

        logger.log("Finished after " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds, saved data for " + options.size() + " options");
        return "Processing done";
    }


    static class OptionsRecursiveAction extends RecursiveAction {

        private List<DownloadableOption> symbols;

        OptionsRecursiveAction(List<DownloadableOption> symbols) {
            this.symbols = symbols;
        }

        @Override
        protected void compute() {

            if (symbols.size() > LambdaFunctionHandler.MAX_OPTIONS_PER_THREAD) {

                List<OptionsRecursiveAction> subtasks = new ArrayList<>();

                subtasks.addAll(createSubtasks());

                for (RecursiveAction subtask : subtasks) {
                    subtask.fork();
                }

            } else {
                for (DownloadableOption symbol : symbols) {
                    try {
                        processOption(symbol);
                    } catch (Exception e){
                        logger.log("An Exception occurred while processing an option: " + e);
                    }
                }
            }
        }


        private List<OptionsRecursiveAction> createSubtasks() {

            OptionsRecursiveAction subtask1 = new OptionsRecursiveAction(symbols.subList(0, symbols.size() / 2));
            OptionsRecursiveAction subtask2 = new OptionsRecursiveAction(symbols.subList(symbols.size() / 2, symbols.size()));

            List<OptionsRecursiveAction> subtasks = new ArrayList<>();
            subtasks.add(subtask1);
            subtasks.add(subtask2);
            return subtasks;
        }


        private void processOption(DownloadableOption optionSymbol) {
            String url = API_URL;
            url = url.replace("$symbol$", optionSymbol.getSymbol());

            LocalDate minimumDate = LocalDate.now().plusDays(optionSymbol.getMin_days_to_expiration() != null ? optionSymbol.getMin_days_to_expiration() : 0);

            url = url + "&" + minimumDate.format(dateFormatter);


            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                System.out.println("An exception occurred while creating URL: " + e);
                return;
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                System.out.println("An Exception occurred while opening HTTPS connection: " + e);
                return;
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                System.out.println("An exception occurred while setting request method: " + e);
                return;
            }
            int responseCode = 0;

            try {
                responseCode = con.getResponseCode();
            } catch (IOException e) {
                System.out.println("An exception occurred while getting response code: " + e);
                return;
            }

            String response = null;
            try (InputStream inputStream = con.getInputStream()) {
                response = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                logger.log("An exception occurred while reading response from the API for option " + optionSymbol.getSymbol() + " response code was " + responseCode);
                return;
            }

            JsonNode json = null;
            try {
                json = objectMapper.readTree(response);
            } catch (IOException e) {
                logger.log("An exception occurred while parsing response from the API to JSON, response code was " + responseCode);
                return;
            }


            final String stock = json.get("code").asText();
            final String exchange = json.get("exchange").asText();
            json = json.get("data");

            List<Option> options = new ArrayList<>();
            List<OptionMetadata> optionsMetdata = new ArrayList<>();


            for (int i = 0; i < json.size(); i++) {

                JsonNode call = json.get(i).get("options").get("CALL");
                if (call != null && call.size() != 0) {
                    deserializeOptions(call, options, optionsMetdata, stock, exchange);
                }


                JsonNode put = json.get(i).get("options").get("PUT");
                if (put != null && put.size() != 0) {
                    deserializeOptions(put, options, optionsMetdata, stock, exchange);
                }

            }
            LambdaFunctionHandler.options.addAll(options);
            LambdaFunctionHandler.optionsMetadata.addAll(optionsMetdata);
        }

        private void deserializeOptions(JsonNode json, List<Option> options, List<OptionMetadata> optionsMetadeta, String stockName, String exchange) {


            for (int j = 0; j < json.size(); j++) {

                try {
                    String entry = json.get(j).toString();
                    Option option = objectMapper.readValue(json.get(j).toString(), Option.class);
                    if (validateOption(option)) {
                        options.add(option);
                    }
                } catch (IOException e) {
                    logger.log("An exception occurred while deserializing option for stock " + stockName);
                }

                try {
                    OptionMetadata optionMetadata = objectMapper.readValue(json.get(j).toString(), OptionMetadata.class);
                    optionMetadata.setStock(stockName);
                    optionMetadata.setExchange(exchange);
                    if (validateOptionMetadata(optionMetadata)) {
                        optionsMetadeta.add(optionMetadata);
                    }
                } catch (IOException e) {
                    logger.log("An exception occurred while deserializing option metadata for stock " + stockName);
                }
            }


        }

        private boolean validateOption(Option option) {
            if(option.getName() == null){
                return false;
            }
            if(option.getAsk() == null || option.getAsk() == null || option.getLast_price() == null){
                return false;
            }
            return true;
        }

        private boolean validateOptionMetadata(OptionMetadata metadata) {
            if(metadata.getContract_name() == null || metadata.getContract_name().isEmpty() || metadata.getExpiration_date() == null || metadata.getStrike() == null){
                return false;
            }
            return true;
        }

    }

}
