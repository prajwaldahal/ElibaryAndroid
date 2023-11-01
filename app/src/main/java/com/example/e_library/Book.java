package com.example.e_library;

import androidx.annotation.NonNull;

public class Book {
    protected String img;
    protected String isbnno;
    protected String name;
    protected String publisher;
    protected String author;
    protected String rent;

    public Book(String img, String isbnno, String name, String publisher, String author, String rent) {
        this.img = img;
        this.isbnno = isbnno;
        this.name = name;
        this.publisher = publisher;
        this.author = author;
        this.rent = rent;
    }

    public Book(String img, String isbnno, String name, String publisher, String author) {
        this.img = img;
        this.isbnno = isbnno;
        this.name = name;
        this.publisher = publisher;
        this.author = author;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getIsbnno() {
        return isbnno;
    }

    public void setIsbnno(String isbnno) {
        this.isbnno = isbnno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(String rent) {
        this.rent = rent;
    }


    @NonNull
    @Override
    public String toString() {
        return "Book{" +
                "img='" + img + '\'' +
                ", isbnno='" + isbnno + '\'' +
                ", name='" + name + '\'' +
                ", publisher='" + publisher + '\'' +
                ", author='" + author + '\'' +
                ", rent='" + rent + '\'' +
                '}';
    }
}
