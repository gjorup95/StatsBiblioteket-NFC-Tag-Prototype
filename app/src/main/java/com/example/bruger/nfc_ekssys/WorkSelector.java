package com.example.bruger.nfc_ekssys;

import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


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
