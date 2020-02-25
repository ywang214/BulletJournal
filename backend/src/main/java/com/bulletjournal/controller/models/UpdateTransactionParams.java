package com.bulletjournal.controller.models;

public class UpdateTransactionParams {

    private String name;

    private String payer;

    private Double amount;

    private String date;

    private Integer transactionType;

    public UpdateTransactionParams(String name, String payer, Double amount, String date, Integer transactionType) {
        this.name = name;
        this.payer = payer;
        this.amount = amount;
        this.date = date;
        this.transactionType = transactionType;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public boolean hasName() {
        return this.name != null;
    }

    public boolean hasPayer() {
        return this.payer != null;
    }

    public boolean hasDate() {
        return this.date != null;
    }

    public boolean hasAmount() {
        return this.amount != null;
    }

    public boolean hasTransactionType() {
        return this.transactionType != null;
    }
}