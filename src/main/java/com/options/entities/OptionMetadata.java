package com.options.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionMetadata {

    private String contract_name;
    private String stock;
    private String exchange;
    private String type;
    private Double strike;
    private Date expiration_date;
    private String currency;
    private String contract_size;
    private Date last_trade_date;


    public OptionMetadata(){}

    @JsonCreator
    public OptionMetadata(
            @JsonProperty("contractName") String contract_name,
            @JsonProperty("type") String type,
            @JsonProperty("strike") Double strike,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            @JsonProperty("expirationDate") Date expiration_date,
            @JsonProperty("currency") String currency,
            @JsonProperty("contractSize") String contract_size,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("lastTradeDateTime") Date last_trade_date) {
        this.contract_name = contract_name;
        this.type = type;
        this.strike = strike;
        this.expiration_date = expiration_date;
        this.currency = currency;
        this.contract_size = contract_size;
        this.last_trade_date = last_trade_date;
    }

    @Override
    public String toString() {
        return "OptionMetadata{" +
                "contract_name='" + contract_name + '\'' +
                ", stock='" + stock + '\'' +
                ", exchange='" + exchange + '\'' +
                ", type='" + type + '\'' +
                ", strike=" + strike +
                ", expiration_date=" + expiration_date +
                ", currency='" + currency + '\'' +
                ", contract_size='" + contract_size + '\'' +
                ", last_trade_date=" + last_trade_date +
                '}';
    }

    public Date getLast_trade_date() {
        return last_trade_date;
    }

    public void setLast_trade_date(Date last_trade_date) {
        this.last_trade_date = last_trade_date;
    }

    public String getContract_name() {
        return contract_name;
    }

    public void setContract_name(String contract_name) {
        this.contract_name = contract_name;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getStrike() {
        return strike;
    }

    public void setStrike(Double strike) {
        this.strike = strike;
    }

    public Date getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(Date expiration_date) {
        this.expiration_date = expiration_date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getContract_size() {
        return contract_size;
    }

    public void setContract_size(String contract_size) {
        this.contract_size = contract_size;
    }

}
