package com.example.e_library;

import androidx.annotation.NonNull;

public class RentedBook extends Book {
    private String file;
    private  String renteddate;

    private String upto;


    public RentedBook(String isbnno, String name, String author, String upto, String img, String file,String publisher,String renteddate) {
        super(img,isbnno,name,publisher,author);
        this.file = file;
        this.upto=upto;
        this.renteddate=renteddate;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getExpiryDate() {
        return upto;
    }

    public void setExpiryDate(String upto) {
        this.upto = upto;
    }

    public String getRenteddate() {
        return renteddate;
    }

    public void setRenteddate(String renteddate) {
        this.renteddate = renteddate;
    }

    public String getUpto() {
        return upto;
    }

    public void setUpto(String upto) {
        this.upto = upto;
    }

    @NonNull
    @Override
    public String toString() {
        return "RentedBook{" +
                "name='" + name + '\'' +
                ", isbnno='" + isbnno + '\'' +
                ", img='" + img + '\'' +
                ", file='" + file + '\'' +
                ", author='" + author + '\'' +
                ", renteddate='" + renteddate + '\'' +
                ", upto='" + upto + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }
}
