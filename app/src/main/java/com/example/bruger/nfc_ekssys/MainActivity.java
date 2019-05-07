package com.example.bruger.nfc_ekssys;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements Serializable {
    private TextView text;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private HashMap<Long, BookImpl> bookMap;
    private Button printButton;
    private Button saveButton;
    private Button loadButton;
    private Button deleteButton;
    private Tag printTag;
    private BookImpl previousBook = null;
    private BookImpl currentBook = null;
    private int scans = 0;
    int duration = Toast.LENGTH_SHORT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scans = 0;
        bookMap = new HashMap<>();
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        printButton = findViewById(R.id.buttonPrint);
        saveButton = findViewById(R.id.saveButton);
        loadButton = findViewById(R.id.loadButton);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteHashMap();
            }
        });
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    loadHashMap();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveHashMap();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printBookMap();
            }
        });
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    private void deleteHashMap() {
        SharedPreferences keyValues = getSharedPreferences("Your_Shared_Prefs", Context.MODE_PRIVATE);
        keyValues.edit().clear().apply();
    }

    private void loadHashMap() throws IOException, ClassNotFoundException {
        SharedPreferences keyValues = getSharedPreferences("Your_Shared_Prefs", Context.MODE_PRIVATE);
        for (String key : keyValues.getAll().keySet()) {
            bookMap.put(Long.valueOf(key), (BookImpl) fromString(keyValues.getString(key, null)));
        }
    }

    private void saveHashMap() throws IOException {

        SharedPreferences keyValues = getSharedPreferences("Your_Shared_Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor keyValuesEditor = keyValues.edit();

        for (Long s : bookMap.keySet()) {
            keyValuesEditor.putString(String.valueOf(s), toString(bookMap.get(s)));
        }
        keyValuesEditor.apply();
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void addToBookMap(byte[] tag) {
        Long i = toDec(tag);
        bookMap.put(i, new BookImpl("abe", i));
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

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            Long i = toDec(id);
            // TODO: fix at bogen placeres i hashmappet efter højeste scannede nr i stedet for scannet rækkefølge
            if (!bookMap.containsKey(i)) {
                addToBookMap(id);
            }
            setUpTextView();
            // second scan
            if (scans >= 1) {
                previousBook = currentBook;
            }
            // first scan
            currentBook = getBook(toDec(id));

            if (scans >= 1) {
                if (isBookPlacedCorrectly(previousBook, currentBook)) {
                    scans = 0;
                    previousBook = null;
                    currentBook = null;
                    return;
                } else currentBook = previousBook;
            }
            scans++;
        }
    }


    private boolean isBookPlacedCorrectly(BookImpl previousBook, BookImpl currentBook) {
        if (previousBook != null && currentBook != null) {
            if (Math.abs(previousBook.getInternalID() - currentBook.getInternalID()) <= 1 && currentBook.getId() != previousBook.getId()) {
                Log.d(String.valueOf(printTag), "bookid 1: " + previousBook.getInternalID() + " - bookid 2: " + currentBook.getInternalID());
                Toast.makeText(this, "Bogen er placeret korrekt", duration).show();
                return true;
            }
            if (currentBook.getId() == previousBook.getId()) {
                Toast.makeText(this, "Du har scannet samme bog to gange", duration).show();
                return false;
            }
        }
        Toast.makeText(this, "Du har placeret bogen forkert, scan igen", duration).show();
        return false;
    }

    private void printBookMap() {
        for (Map.Entry<Long, BookImpl> entry : bookMap.entrySet()) {
            Log.d(String.valueOf(printTag), "printBookMap: " + entry.getKey());
            Log.d(String.valueOf(printTag), "access object: " + entry.getValue().getInternalID());
        }
    }

    private void setUpTextView() {
        final StringBuilder stringBuilder = new StringBuilder();
        String str;
        for (Map.Entry<Long, BookImpl> entry : bookMap.entrySet()) {
            str = entry.getValue().getInternalID() + ", ";
            stringBuilder.append(str);
        }
        text.setText(stringBuilder);
    }

    private BookImpl getBook(Long id) {
        return bookMap.get(id);
    }

    /**
     * Read the object from Base64 string.
     */
    @SuppressLint("NewApi")
    private static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    @SuppressLint("NewApi")
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}


