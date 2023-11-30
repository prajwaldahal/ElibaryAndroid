package com.example.e_library;

public class RecentBook {
    private String img;
    private String name;
    private String author;
    private String publisher;
    private String rent;
    private String isbnno;

    public RecentBook(String img, String name, String author, String publisher, String rent, String isbnno) {
        this.img = img;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.rent = rent;
        this.isbnno = isbnno;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(String rent) {
        this.rent = rent;
    }

    public String getIsbnno() {
        return isbnno;
    }

    public void setIsbnno(String isbnno) {
        this.isbnno = isbnno;
    }
}

