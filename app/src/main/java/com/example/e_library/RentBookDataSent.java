package com.example.e_library;

public class RentBookDataSent {
    private String name;
    private String isbnNo;
    private String date;

    private int payment;

    public RentBookDataSent(String name, String isbnNo, String date,int payment) {
        this.name = name;
        this.isbnNo = isbnNo;
        this.date = date;
        this.payment=payment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsbnNo() {
        return isbnNo;
    }

    public void setIsbnNo(String isbnNo) {
        this.isbnNo = isbnNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPayment() {
        return payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    @Override
    public String toString() {
        return "RentBookDataSent{" +
                "name='" + name + '\'' +
                ", isbnNo='" + isbnNo + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
