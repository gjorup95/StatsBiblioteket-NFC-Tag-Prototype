package com.example.bruger.nfc_ekssys;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/**
 * By Mathias Laursen copyright
 */
public class FetchBooks extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private HashMap<Long,BookImpl> bookMap;
    private TextView successText;
    private int nextBook;
    private TextView nextText;
    private Button nextBookButton;
    private ImageView acceptView;
    private Button fakeItButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_books);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        setupBookDB();
        fakeItButton = findViewById(R.id.fakeIt);
        fakeItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fakeIt();
            }
        });
        successText = findViewById(R.id.successText);
        nextText = findViewById(R.id.nextText);
        nextText.setText(bookMap.get(2987440147L).getName());
        nextBookButton = findViewById(R.id.nextBookButton);
        nextBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextText.setText(bookMap.get(getNextBook()).getName());
                successText.setText("");
                nextBookButton.setVisibility(View.GONE);
                acceptView.setVisibility(View.GONE);
            }
        });
        acceptView = findViewById(R.id.acceptView);
        acceptView.setVisibility(View.GONE);
    }

    private void setupBookDB() {
        bookMap = new HashMap<>();
        BookImpl book1 = new BookImpl("Studiekort",2987440147L);
        BookImpl book2= new BookImpl("Dankort",4174334015L);
        BookImpl book3=new BookImpl("Studiekort2",1935306686L);
        bookMap.put(book1.getId(),book1);
        bookMap.put(book2.getId(),book2);
        bookMap.put(book3.getId(),book3);
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

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            System.out.println(toDec(id)+"hejhej");
            if(toDec(id)==getNextBook()){
                successText.setText("Correct");
                acceptView.setVisibility(View.VISIBLE);
                acceptView.setImageResource(R.drawable.ic_flueben);
                nextBook++;
                nextBookButton.setVisibility(View.VISIBLE);
            }
            else{
                successText.setText("Wrong book, try again");
                acceptView.setVisibility(View.VISIBLE);
                acceptView.setImageResource(R.drawable.ic_afvist);
            }
        }
    }

    private long getNextBook() {
        if(nextBook==0){
            return 2987440147L;
        }
        else if(nextBook==1){
            return 1935306686;
        }
        return 4174334015L;
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
        successText.setText("Correct");
        acceptView.setVisibility(View.VISIBLE);
        acceptView.setImageResource(R.drawable.ic_flueben);
        nextBook++;
        nextBookButton.setVisibility(View.VISIBLE);
    }
}