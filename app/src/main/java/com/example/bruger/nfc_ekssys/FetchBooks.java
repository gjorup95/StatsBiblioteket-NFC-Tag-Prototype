package com.example.bruger.nfc_ekssys;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * By Mathias Laursen copyright
 */
public class FetchBooks extends AppCompatActivity {
    private  Tag printTag;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView successText;
    private int nextBook;
    private TextView nextText;
    private Button nextBookButton;
    private ImageView acceptView;
    private Button fakeItButton;
    private List<BookImpl> bookList;
    private boolean bookWasCorrectlyScanned = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookList = new ArrayList<>();
        if (Singleton.getInstance().getArrayList() != null){
            bookList = Singleton.getInstance().getArrayList();

        }
        setContentView(R.layout.activity_fetch_books);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        fakeItButton = findViewById(R.id.fakeIt);
        fakeItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fakeIt();
            }
        });
        successText = findViewById(R.id.successText);
        nextText = findViewById(R.id.nextText);
        nextText.setText(bookList.get(0).getName());
        nextBookButton = findViewById(R.id.nextBookButton);
        nextBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getNextBookToScan() != null) {
                    nextText.setText(getNextBookToScan().getName());
                }
                successText.setText("");
                nextBookButton.setVisibility(View.GONE);
                acceptView.setVisibility(View.GONE);
            }
        });
        acceptView = findViewById(R.id.acceptView);
        acceptView.setVisibility(View.GONE);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

            long bookID = toDec(id);

                for (int i=0; i<bookList.size(); i++) {
                    if (bookList.get(i).getId() == bookID) {
                        scanBook(bookList.get(i));
                        bookWasCorrectlyScanned = true;
                    }
                }
                if (bookWasCorrectlyScanned) {

                    successText.setText("Correct");
                    acceptView.setVisibility(View.VISIBLE);
                    acceptView.setImageResource(R.drawable.ic_flueben);
                    nextBook++;
                    nextBookButton.setVisibility(View.VISIBLE);
                    bookWasCorrectlyScanned = false;
                }
            }
            else{
                successText.setText("Wrong book, try again");
                acceptView.setVisibility(View.VISIBLE);
                acceptView.setImageResource(R.drawable.ic_afvist);
            }
        }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    public void fakeIt(){
        printBooks();
        successText.setText("Correct");
        acceptView.setVisibility(View.VISIBLE);
        acceptView.setImageResource(R.drawable.ic_flueben);
        nextBook++;
        nextBookButton.setVisibility(View.VISIBLE);
    }
    public void scanBook(BookImpl book){
        book.setScanned(true);
    }
    public void printBooks(){
        if (bookList != null){
            for (int i =0; i<bookList.size(); i++){
                Log.d(String.valueOf(printTag), "bookID: " + bookList.get(i).getInternalID() + " Name: " + bookList.get(i).getName());
            }
        }
    }
    public ArrayList<BookImpl> getScannedBooks (){
        ArrayList<BookImpl> tempList = new ArrayList<>();
        for (int i=0; i<bookList.size(); i++){
            if(bookList.get(i).isScanned()){
                tempList.add(bookList.get(i));
            }
        }
return tempList;
    }

    public ArrayList<BookImpl> getRemaingNotScannedBooks(){
        ArrayList<BookImpl> tempList = new ArrayList<>();
        for (int i=0; i<bookList.size(); i++){
            if(bookList.get(i).isScanned()== false){
                tempList.add(bookList.get(i));
            }
        }
        return tempList;
    }
    public BookImpl getNextBookToScan(){
        for (int i=0; i<bookList.size(); i++){
            if (!bookList.get(i).isScanned()){
                return bookList.get(i);
            }
        }
        // TODO:: Add there are no remaining books
        return null;
    }
}