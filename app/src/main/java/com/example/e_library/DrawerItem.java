package com.example.e_library;

public class DrawerItem {
    private final int image;
    private final String item;

    public DrawerItem(int image, String item) {
        this.image = image;
        this.item = item;
    }
    public int getImage() {
        return image;
    }

    public String getItem() {
        return item;
    }

}
