package com.example.bruger.nfc_ekssys;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class    MainActivity extends AppCompatActivity implements Serializable {
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
    private int addedBooks=0;
    private List<BookImpl> bookList;
    private int addedNumbers = 0;
    int duration = Toast.LENGTH_SHORT;
    private ImageView acceptView;
    private TextView nextBookText;
    private Button nextBookButton;
    private TextView nextBookPlace;
    private ArrayList<String> bookNames;
    private ArrayList<String> bookNumbers;
    private Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookNames = new ArrayList<>();
        bookNumbers = new ArrayList<>();
        setupBookNumbers();
        setupBookNames();
        scans = 0;
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        bookMap = new HashMap<>();
        bookList = new ArrayList<>();
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.acceptTextMain);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        acceptView = findViewById(R.id.imageView);
        nextBookText=findViewById(R.id.nextBookText);
        text.setText("Scan næste bog");
        nextBookPlace = findViewById(R.id.nextBookPlace);
        nextBookButton = findViewById(R.id.nextBookReset);
        nextBookButton.setVisibility(View.GONE);
        nextBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGUI();
            }
        });
        try {
            loadHashMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        updateList();
        Singleton.getInstance().setArrayList((ArrayList<BookImpl>) bookList);
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            saveHashMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
        updateList();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void addToBookMap(byte[] tag) {
        Long i = toDec(tag);
        if (bookNames != null && addedBooks < bookNames.size() && bookNumbers != null && addedNumbers< bookNumbers.size()){
            bookMap.put(i, new BookImpl(bookNames.get(addedBooks), i,bookNumbers.get(addedNumbers)));
            addedBooks++;
            addedNumbers++;
        }
        else {
            bookMap.put(i, new BookImpl("Harry Potter", i, "9-150-6000"));
        }
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

    @SuppressLint("NewApi")
    private void resolveIntent(Intent intent) {

        String action = intent.getAction();
        //Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            //Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            //nfcAdapter.ignore(tag, 400, null, null);
            Long i = toDec(id);
            // TODO: fix at bogen placeres i hashmappet efter højeste scannede nr i stedet for scannet rækkefølge
            if (!bookMap.containsKey(i)) {
                addToBookMap(id);
            }
            // second scan
            if (scans >= 1) {
                previousBook = currentBook;
            }
            // first scan
            nextBookText.setVisibility(View.VISIBLE);
            nextBookPlace.setVisibility(View.VISIBLE);
            currentBook = getBook(toDec(id));
            nextBookText.setText(currentBook.getName()+" scannet");
            nextBookPlace.setText(currentBook.getForlag()+"");
            text.setText("Scan bogen ved siden af");
            updateList();
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
            // korrekt placeret
            if (Math.abs(previousBook.getInternalID() - currentBook.getInternalID()) <= 1 && currentBook.getId() != previousBook.getId()) {
                Log.d(String.valueOf(printTag), "bookid 1: " + previousBook.getInternalID() + " - bookid 2: " + currentBook.getInternalID());
                Toast.makeText(this, "Bogen er placeret korrekt", duration).show();
                acceptView.setImageResource(R.drawable.ic_flueben);
                acceptView.setVisibility(View.VISIBLE);
                text.setText("Bogen er placeret korrekt");
                nextBookButton.setVisibility(View.VISIBLE);
                v.vibrate(300);
                return true;
            }
            if (currentBook.getId() == previousBook.getId()) {
                // forkert placeret
                Toast.makeText(this, "Du har scannet samme bog to gange", duration).show();
                acceptView.setImageResource(R.drawable.ic_afvist);
                acceptView.setVisibility(View.VISIBLE);
                text.setText("Du har scannet samme bog to gange");
                v.vibrate(1500);
                return false;
            }
        }
        // scannet forkert nummer 2 bog
        acceptView.setImageResource(R.drawable.ic_afvist);
        acceptView.setVisibility(View.VISIBLE);
        text.setText("Du har placeret bogen forkert, scan igen");
        Toast.makeText(this, "Du har placeret bogen forkert, scan igen", duration).show();
        v.vibrate(1500);
        return false;
    }

    private void printBookMap() {
        for (Map.Entry<Long, BookImpl> entry : bookMap.entrySet()) {
            Log.d(String.valueOf(printTag), "printBookMap: " + entry.getKey());
            Log.d(String.valueOf(printTag), "access object: " + entry.getValue().getInternalID());
        }
    }

    private BookImpl getBook(Long id) {
        return bookMap.get(id);
    }

    public void updateList(){
        Collection<BookImpl> demo = bookMap.values();
        ArrayList<BookImpl> listOfKeys = new ArrayList<>(demo);
        bookList = listOfKeys;
        Collections.sort(bookList);
    }
    /**
     * Read the object from Base64 string.
     */
    @SuppressLint("NewApi")
    public static Object fromString(String s) throws IOException,
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
    public static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    void resetGUI(){
        acceptView.setVisibility(View.GONE);
        text.setText("Scan næste bog");
        nextBookText.setVisibility(View.GONE);
        nextBookPlace.setVisibility(View.GONE);
        nextBookButton.setVisibility(View.GONE);
    }
    public void setupBookNames(){
        // TODO: setup books
        bookNames.add("Designing the User Interface");
        bookNames.add("Designing the User Interface");
        bookNames.add("Litteraturens veje");
        bookNames.add("Mænd der hader kvinder");
        bookNames.add("Pigen der legede med ilden");
        bookNames.add("Microbiology with Diseases by Taxonomy");

        bookNames.add("Statistik viden fra data");
        bookNames.add("Ingredienser: Den store bog om råvarer");
        bookNames.add("Harry Potter and the Cursed Child");
        bookNames.add("Mine Helte");
        bookNames.add("Biocatalysts and Enzyme Technology");
        bookNames.add("De bedste cocktails");
    }
    public void setupBookNumbers(){
        bookNumbers.add("2-122-3012");
        bookNumbers.add("2-122-3013");
        bookNumbers.add("2-122-3020");
        bookNumbers.add("2-122-3021");
        bookNumbers.add("2-122-3022");
        bookNumbers.add("2-122-3023");

        // next
        bookNumbers.add("2-123-3030");
        bookNumbers.add("2-123-3031");
        bookNumbers.add("2-123-3032");
        bookNumbers.add("2-123-3035");
        bookNumbers.add("2-123-3037");
        bookNumbers.add("2-123-3038");
    }
}