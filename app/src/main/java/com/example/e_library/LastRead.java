package com.example.e_library;

public class LastRead {
    private String name;
    private String author;
    private String imageUrl;

    private String file;

    public LastRead(String name, String author, String imageUrl, String file) {
        this.name = name;
        this.author = author;
        this.imageUrl = imageUrl;
        this.file=file;
    }
    public void setName(String name){
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImage() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
