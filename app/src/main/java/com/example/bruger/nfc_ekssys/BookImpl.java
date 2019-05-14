package com.example.bruger.nfc_ekssys;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Comparator;

class BookImpl implements Serializable, Comparable<BookImpl> {
    private String name;
    private String forlag;
    private int version;
    private long id;
    private int internalID;
    private static int objectID = 0;
    // millimeter
    private int height;
    private int depth;
    private int width;
    // Gram
    private int weigth;
    private boolean isScanned;


    public BookImpl (String name, long id){
        this.name = name;
        this.id = id;
        height = 285;
        depth = 26;
        width = 245;
        weigth = 1100;
        objectID++;
        this.internalID = objectID;
        this.isScanned = false;


    }
    public BookImpl (String name, long id, int height, int width, int depth, int weigth, String forlag, int version){
        this.name =name;
        this.id = id;
        this.height= height;
        this.width= width;
        this.depth= depth;
        this.weigth= weigth;
        this.forlag=forlag;
        this.version=version;
    }
    public BookImpl (String name, long id, String forlag){
        this.forlag = forlag;
        this.name = name;
        this.id = id;
        height = 285;
        depth = 26;
        width = 245;
        weigth = 1100;
        objectID++;
        this.internalID = objectID;
        this.isScanned = false;
    }
    public String getName() {
        return name;
    }

    public String getForlag() {
        return forlag;
    }

    public void setForlag(String setForlag){
        forlag = setForlag;
    }

    public int getVersion() {
        return version;
    }

    public long getId() {
        return id;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public int getWidth() {
        return width;
    }

    public int getWeigth() {
        return weigth;
    }

    public int getInternalID() {
        return internalID;
    }

    private static Comparator <BookImpl> getComparator(){
        @SuppressLint({"NewApi", "LocalSuppress"}) Comparator<BookImpl> result = Comparator.comparing(BookImpl::getInternalID);
        return result;
    }

    @Override
    public int compareTo(@NonNull BookImpl that) {
        Comparator<BookImpl> COMPARATOR = getComparator();
        int result = COMPARATOR.compare(this, that);
        if (result==0){

        }
        return result;
    }

    public boolean isScanned() {
        return isScanned;
    }

    public void setScanned(boolean scanned) {
        isScanned = scanned;
    }
}
