package com.options.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Option {

    private Date date = new Date();
    private String name;
    private Double last_price;
    private Double bid;
    private Double ask;
    private Integer volume;
    private Integer open_interest;
    private Boolean in_the_money;
    private Double implied_volatility;

    public Option() {
    }

    @JsonCreator
    public Option(
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("contractName") String name,
            @JsonProperty("lastPrice") Double last_price,
            @JsonProperty("bid") Double bid,
            @JsonProperty("ask") Double ask,
            @JsonProperty("volume") Integer volume,
            @JsonProperty("openInterest") Integer open_interest,
            @JsonProperty("impliedVolatility") Double implied_volatility) {
        this.name = name;
        this.last_price = last_price;
        this.bid = bid;
        this.ask = ask;
        this.volume = volume;
        this.open_interest = open_interest;
        this.implied_volatility = implied_volatility;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLast_price() {
        return last_price;
    }

    public void setLast_price(Double last_price) {
        this.last_price = last_price;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getOpen_interest() {
        return open_interest;
    }

    public void setOpen_interest(Integer open_interest) {
        this.open_interest = open_interest;
    }

    public Double getImplied_volatility() {
        return implied_volatility;
    }

    public void setImplied_volatility(Double implied_volatility) {
        this.implied_volatility = implied_volatility;
    }


    public Boolean getIn_the_money() {
        return in_the_money;
    }

    @JsonProperty("inTheMoney")
    public void setIn_the_money(String in_the_money) {
        this.in_the_money = in_the_money.equals("TRUE") ? true : false;
    }

    @Override
    public String toString() {
        return "Option{" +
                "date=" + date +
                ", name='" + name + '\'' +
                ", last_price=" + last_price +
                ", bid=" + bid +
                ", ask=" + ask +
                ", volume=" + volume +
                ", open_interest=" + open_interest +
                ", in_the_money=" + in_the_money +
                ", implied_volatility=" + implied_volatility +
                '}';
    }

}
