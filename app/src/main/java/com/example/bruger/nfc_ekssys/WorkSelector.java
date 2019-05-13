package com.example.bruger.nfc_ekssys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.bruger.nfc_ekssys.MainActivity.fromString;

public class WorkSelector extends AppCompatActivity {

    private Button afhButton;
    private Button opsButton;
    private Tag printTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_selector);
        afhButton = findViewById(R.id.afhBut);
        opsButton = findViewById(R.id.opsBut);
        afhButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkSelector.this, FetchBooks.class);
                startActivity(intent);
            }
        });
        opsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
