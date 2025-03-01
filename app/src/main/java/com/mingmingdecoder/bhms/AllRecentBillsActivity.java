package com.mingmingdecoder.bhms;


import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllRecentBillsActivity extends AppCompatActivity {

    private RecyclerView recyclerAllBills;
    private FileHandler fileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recent_bills);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Recent Bills");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize FileHandler
        fileHandler = new FileHandler(this);

        // Initialize RecyclerView
        recyclerAllBills = findViewById(R.id.recycler_all_bills);
        recyclerAllBills.setLayoutManager(new LinearLayoutManager(this));

        // Load all bills
        loadAllRecentBills();
    }

    private void loadAllRecentBills() {
        // Read all bills
        List<Bill> rentBills = fileHandler.readBills("rent");
        List<Bill> waterBills = fileHandler.readBills("water");
        List<Bill> electricBills = fileHandler.readBills("electric");

        // Combine all bills into one list
        List<Bill> allBills = rentBills;
        allBills.addAll(waterBills);
        allBills.addAll(electricBills);

        // Sort bills by date (newest first)
        Collections.sort(allBills, (b1, b2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                Date date1 = format.parse(b1.getDate());
                Date date2 = format.parse(b2.getDate());
                return date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });

        // Set up RecyclerView adapter
        RecentBillsAdapter adapter = new RecentBillsAdapter(allBills);
        recyclerAllBills.setAdapter(adapter);

        if (allBills.isEmpty()) {
            Toast.makeText(this, "No recent bills found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
