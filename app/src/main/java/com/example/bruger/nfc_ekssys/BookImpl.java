package com.example.bruger.nfc_ekssys;

import java.io.Serializable;

class BookImpl implements Serializable {
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
    public BookImpl (String name, long id){
        this.name = name;
        this.id = id;
        height = 285;
        depth = 26;
        width = 245;
        weigth = 1100;
        objectID++;
        this.internalID = objectID;


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
    public String getName() {
        return name;
    }

    public String getForlag() {
        return forlag;
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
}
