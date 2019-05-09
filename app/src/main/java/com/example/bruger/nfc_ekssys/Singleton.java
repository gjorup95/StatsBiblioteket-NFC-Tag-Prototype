package com.example.bruger.nfc_ekssys;

import java.util.ArrayList;

public class Singleton {
    private static Singleton uniqInstance;
    public ArrayList<BookImpl> books = new ArrayList<BookImpl>();
    private Singleton() {
    }
    public static Singleton getInstance() {
        if (uniqInstance == null) {
            {
                if (uniqInstance == null)
                    uniqInstance = new Singleton();
            }
        }
        return uniqInstance;
    }
    public void setArrayList(ArrayList<BookImpl> books)
    {
        this.books = books;

    }
    public ArrayList<BookImpl> getArrayList()
    {
        return this.books;

    }
}