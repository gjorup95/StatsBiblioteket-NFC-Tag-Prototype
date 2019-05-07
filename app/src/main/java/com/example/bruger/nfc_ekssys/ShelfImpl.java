package com.example.bruger.nfc_ekssys;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ShelfImpl {

    private ArrayList<BookImpl> shelf;
    private int remainingWidth;
    private int totalWidthInventory;
    private int width = 2500;
    private int depth;
    private long shelfID;
    public ShelfImpl(long id){
    this.shelfID = id;
    shelf = new ArrayList<>();

    }

    public void getTotalWidthInventory() {
        int tempWidth =0;
     for (int i =0; i<shelf.size(); i++){
         tempWidth += shelf.get(i).getWidth();
     }
        totalWidthInventory = tempWidth;
    }

    public void setRemainingWidth() {
        getTotalWidthInventory();
        remainingWidth = width - totalWidthInventory ;
    }

}
