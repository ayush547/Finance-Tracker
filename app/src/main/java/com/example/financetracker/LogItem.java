package com.example.financetracker;

public class LogItem {
    private int year,month,date;
    private String title;
    private int amount;

    public LogItem(int year, int month, int date, String title, int amount) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.title = title;
        this.amount = amount;
    }

    public LogItem(int year, int month, int date, int amount) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.amount = amount;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
