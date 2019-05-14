package com.example.bruger.nfc_ekssys;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.bruger.nfc_ekssys.MainActivity.fromString;

/**
 * By Mathias Laursen copyright
 */
public class FetchBooks extends AppCompatActivity {
    private Tag printTag;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView successText;
    private TextView nextText;
    private Button nextBookButton;
    private ImageView acceptView;
    private Button fakeItButton;
    private List<BookImpl> bookList;
    private boolean bookWasCorrectlyScanned = false;
    private HashMap<Long, BookImpl> bookMap;

    private int hasNotBeenPressed = 1;

    // Recycler stuff

    private BookAdapter adapter;
    private RecyclerView recyclerView;
    private int state=0;
    private Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO:: Adding GUI to display proper information about remaining books and completed books
        super.onCreate(savedInstanceState);
        bookList = new ArrayList<>();
        bookMap = new HashMap<>();

        setContentView(R.layout.activity_fetch_books);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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
                hasNotBeenPressed = 1;
            }
        });
        acceptView = findViewById(R.id.acceptView);
        acceptView.setVisibility(View.GONE);
        try {
            loadHashMap();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        updateList();
        nextText.setText("Ingen bøger i databasen");
        if (bookList.size() != 0) {
            nextText.setText(bookList.get(0).getName());
        }
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setVisibility(View.GONE);
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

    @Override
    protected void onStart() {
        super.onStart();
        updateList();

    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) && hasNotBeenPressed== 1) {
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            hasNotBeenPressed = 0;
            long bookID = toDec(id);

            for (int i = 0; i < bookList.size(); i++) {
                if (bookList.get(i) != null && bookList.get(i).getId() == bookID && bookList.get(i).isScanned() == false) {
                    scanBook(bookList.get(i));
                    bookWasCorrectlyScanned = true;
                }
            } // TODO:: Adding information about each book when scanned
            if (bookWasCorrectlyScanned) {
                successText.setText("Correct");
                acceptView.setVisibility(View.VISIBLE);
                acceptView.setImageResource(R.drawable.ic_flueben);
                nextBookButton.setVisibility(View.VISIBLE);
                bookWasCorrectlyScanned = false;
                v.vibrate(300);

            } else {
                successText.setText("Wrong book, try again");
                acceptView.setVisibility(View.VISIBLE);
                acceptView.setImageResource(R.drawable.ic_afvist);
                v.vibrate(1500);

            }
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

    public void fakeIt() {
        // Recycler stuff
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List list = getRemainingNotScannedBooks();
        adapter = new BookAdapter(this, getRemainingNotScannedBooks());
        recyclerView.setAdapter(adapter);
        if(state==0) {
            recyclerView.setVisibility(View.VISIBLE);
            state=1;
            fakeItButton.setText("Skjul manglende bøger");
            return;
        }else if(state==1){
            recyclerView.setVisibility(View.GONE);
            state=0;
            fakeItButton.setText("Vis manglende bøger");
        }
    }

    public void scanBook(BookImpl book) {
        book.setScanned(true);
    }

    public void printBooks() {
        if (bookList != null) {
            for (int i = 0; i < bookList.size(); i++) {
                Log.d(String.valueOf(printTag), "bookID: " + bookList.get(i).getInternalID() + " Name: " + bookList.get(i).getName());
            }
        }
    }

    public ArrayList<BookImpl> getScannedBooks() {
        ArrayList<BookImpl> tempList = new ArrayList<>();
        for (int i = 0; i < bookList.size(); i++) {
            if (bookList.get(i).isScanned()) {
                tempList.add(bookList.get(i));
            }
        }
        return tempList;
    }

    public ArrayList<BookImpl> getRemainingNotScannedBooks() {
        ArrayList<BookImpl> tempList = new ArrayList<>();
        for (int i = 0; i < bookList.size(); i++) {
            if (bookList.get(i).isScanned() == false) {
                tempList.add(bookList.get(i));
            }
        }
        return tempList;
    }

    public BookImpl getNextBookToScan() {
        for (int i = 0; i < bookList.size(); i++) {
            if (!bookList.get(i).isScanned()) {
                return bookList.get(i);
            }
        }
        // TODO:: Add there are no remaining books
        return null;
    }

    private void loadHashMap() throws IOException, ClassNotFoundException {
        SharedPreferences keyValues = getSharedPreferences("Your_Shared_Prefs", Context.MODE_PRIVATE);
        for (String key : keyValues.getAll().keySet()) {
            bookMap.put(Long.valueOf(key), (BookImpl) fromString(keyValues.getString(key, null)));
        }
    }

    public void updateList() {
        Collection<BookImpl> demo = bookMap.values();
        ArrayList<BookImpl> listOfKeys = new ArrayList<>(demo);
        bookList = listOfKeys;
        Collections.sort(bookList);
    }
    public void randomizeBookList(){

        for (int i =0; i<bookList.size(); i++){
            if (ThreadLocalRandom.current().nextInt(0, 10+1)> 6){
                bookList.get(i).setScanned(true);
            }

        }

    }
}