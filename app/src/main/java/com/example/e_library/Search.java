package com.example.e_library;

import java.util.ArrayList;
import java.util.Iterator;

public class Search {
    static ArrayList<? extends Book> search(ArrayList<? extends Book> books, String s){
        Iterator<? extends Book> iterator=books.iterator();
        while(iterator.hasNext()){
            Book book=iterator.next();
            if(!(book.getName().toLowerCase().contains(s.toLowerCase()) || book.getAuthor().toLowerCase().contains(s.toLowerCase()) || book.getPublisher().toLowerCase().contains(s.toLowerCase()) || book.getIsbnno().toLowerCase().contains(s.toLowerCase()))){
                  iterator.remove();
            }
        }
        return books;
    }

}
