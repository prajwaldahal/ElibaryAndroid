package com.example.e_library;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Sort<T extends Book> {
    private final List<T> books;
    private final SortBy which;

    public Sort(SortBy which, List<T> books) {
        this.which = which;
        this.books = books;
    }

    public List<T> mergeSort() {
        if (books == null || books.size() <= 1) {
            return books;
        }

        List<T> tempBooks = new ArrayList<>();
        mergeSort(books, tempBooks, 0, books.size() - 1);
        return books;
    }

    private void mergeSort(List<T> books, List<T> tempBooks, int low, int high) {
        if (low < high) {
            int mid = (low + high) / 2;

            mergeSort(books, tempBooks, low, mid);
            mergeSort(books, tempBooks, mid + 1, high);
            merge(books, tempBooks, low, mid, high);
        }
    }

    private void merge(List<T> books, List<T> tempBooks, int low, int mid, int high) {
        int i = low;
        int j = mid + 1;
        int k = low;

        tempBooks.addAll(books);

        while (i <= mid && j <= high) {
            switch (which) {
                case NAME:
                    if (books.get(i).getName().compareTo(books.get(j).getName()) <= 0) {
                        tempBooks.set(k++, books.get(i++));
                    } else {
                        tempBooks.set(k++, books.get(j++));
                    }
                    break;

                case PRICE:
                    int rentLeft = Integer.parseInt(books.get(i).getRent());
                    int rentRight = Integer.parseInt(books.get(j).getRent());
                    if (rentLeft < rentRight) {
                        tempBooks.set(k++, books.get(i++));
                    } else {
                        tempBooks.set(k++, books.get(j++));
                    }
                    break;

                case ISBNNO:
                    if (books.get(i).getIsbnno().compareTo(books.get(j).getIsbnno()) <= 0) {
                        tempBooks.set(k++, books.get(i++));
                    } else {
                        tempBooks.set(k++, books.get(j++));
                    }
                    break;

                case EXPIRY:
                    if (books.get(0) instanceof RentedBook) {
                        RentedBook rentedBook = (RentedBook) books.get(i);
                        RentedBook rentedBook2 = (RentedBook) books.get(j);
                        if (rentedBook.getExpiryDate().compareTo(rentedBook2.getExpiryDate()) <= 0) {
                            tempBooks.set(k++, books.get(i++));
                        } else {
                            tempBooks.set(k++, books.get(j++));
                        }
                    }
                    break;

                case RENTED:
                    if (books.get(0) instanceof RentedBook) {
                        RentedBook rentedBook = (RentedBook) books.get(i);
                        RentedBook rentedBook2 = (RentedBook) books.get(j);
                        if (rentedBook.getRenteddate().compareTo(rentedBook2.getRenteddate()) <= 0) {
                            tempBooks.set(k++, books.get(i++));
                        } else {
                            tempBooks.set(k++, books.get(j++));
                        }
                    }
                    break;
            }
        }

        while (i <= mid) {
            tempBooks.set(k++, books.get(i++));
        }

        while (j <= high) {
            tempBooks.set(k++, books.get(j++));
        }

        for (k = low; k <= high; k++) {
            books.set(k, tempBooks.get(k));
        }
    }
}