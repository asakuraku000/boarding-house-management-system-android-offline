package com.mingmingdecoder.bhms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView balanceText, coinText, roomText, usernameText;
    private Button rentBtn, waterBtn, electricBtn, manageTenantsBtn;
    private Spinner tenantSelectionSpinner;
    private FileHandler fileHandler;
    private List<Tenant> tenants;
    private Map<String, Double> tenantBalances = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        // Initialize UI components
        balanceText = findViewById(R.id.balanceText);
    //    coinText = findViewById(R.id.coinText);
        roomText = findViewById(R.id.roomText);
        usernameText = findViewById(R.id.usernameText);
        tenantSelectionSpinner = findViewById(R.id.tenantSelectionSpinner);
        
        rentBtn = findViewById(R.id.rentBtn);
        waterBtn = findViewById(R.id.waterBtn);
        electricBtn = findViewById(R.id.electricBtn);
        manageTenantsBtn = findViewById(R.id.manageTenantsBtn);
        
        // Initialize file handler
        fileHandler = new FileHandler(this);
        
        // Get data from intent
        String username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            usernameText.setText(username);
        }
        
        // Set up tenant spinner and load data
        setupTenantSpinner();
        
        // Set click listeners
        rentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBillActivity("rent");
            }
        });
        
        waterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBillActivity("water");
            }
        });
        
        electricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBillActivity("electric");
            }
        });
        
        manageTenantsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TenantManagementActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        
        
       Intent intent = getIntent();
     if (intent.hasExtra("section")) {
        String billType = intent.getStringExtra("section");
        openBillActivity(billType);
    }else if(intent.hasExtra("tenant")){
        Intent intent1 = new Intent(MainActivity.this, TenantManagementActivity.class);
       startActivityForResult(intent1, 1);
    }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        setupTenantSpinner();
    }
    
    private void setupTenantSpinner() {
        try {
            // Load tenants
            tenants = fileHandler.readTenants();
            List<String> tenantNames = new ArrayList<>();
            
            if (tenants != null && !tenants.isEmpty()) {
                for (Tenant tenant : tenants) {
                    tenantNames.add(tenant.getName());
                }
            } else {
                // Add a default option if no tenants
                tenantNames.add("No tenants available");
            }
            
            // Create and set adapter for tenant spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, tenantNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tenantSelectionSpinner.setAdapter(adapter);
            
            // Calculate tenant balances
            calculateTenantBalances();
            
            // Set spinner selection listener
            tenantSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateDisplayForSelectedTenant(position);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
            
            // Select first tenant by default if available
            if (!tenantNames.isEmpty()) {
                tenantSelectionSpinner.setSelection(0);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up tenant spinner: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void calculateTenantBalances() {
        tenantBalances.clear();
        
        // Initialize all tenants with zero balance
        if (tenants != null) {
            for (Tenant tenant : tenants) {
                tenantBalances.put(tenant.getName(), 0.0);
            }
        }
        
        // Add up all unpaid bills for each tenant
        addBillsToBalance("rent");
        addBillsToBalance("water");
        addBillsToBalance("electric");
    }
    
    private void addBillsToBalance(String billType) {
        List<Bill> bills = fileHandler.readBills(billType);
        if (bills != null) {
            for (Bill bill : bills) {
                if (!bill.isPaid() && tenantBalances.containsKey(bill.getTenantName())) {
                    double currentBalance = tenantBalances.get(bill.getTenantName());
                    tenantBalances.put(bill.getTenantName(), currentBalance + bill.getAmount());
                }
            }
        }
    }
    
    private void updateDisplayForSelectedTenant(int position) {
        try {
            if (tenants != null && !tenants.isEmpty() && position < tenants.size()) {
                Tenant selectedTenant = tenants.get(position);
                String tenantName = selectedTenant.getName();
                
                // Update displayed tenant info
                usernameText.setText(tenantName);
                roomText.setText(selectedTenant.getRoom());
                
                // Update balance (sum of all unpaid bills)
                double balance = tenantBalances.getOrDefault(tenantName, 0.0);
                balanceText.setText(String.format("%.2f", balance));
                
                // Load coins from boarding house data for this tenant
                // Note: If you want to track coins per tenant, you'll need to modify your data structure
                BoardingHouseData data = fileHandler.readBoardingHouseData();
            /*    if (data != null) {
                coinText.setText(String.valueOf(data.getCoins()));
                } else {
                    coinText.setText("0");
                }
                */
            } else {
                // No tenants or invalid selection
                usernameText.setText("No Tenant");
                roomText.setText("No Room");
                balanceText.setText("0.00");
            //    coinText.setText("0");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating display: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void openBillActivity(String billType) {
        try {
            // Get the selected tenant name to pass to the bill activity
            String selectedTenantName = null;
            int position = tenantSelectionSpinner.getSelectedItemPosition();
            
            if (tenants != null && !tenants.isEmpty() && position < tenants.size()) {
                selectedTenantName = tenants.get(position).getName();
            }
            
            Intent intent = new Intent(MainActivity.this, BillManagementActivity.class);
            intent.putExtra("BILL_TYPE", billType);
            if (selectedTenantName != null) {
                intent.putExtra("SELECTED_TENANT", selectedTenantName);
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening bill activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refresh data when returning from tenant management
        if (requestCode == 1) {
            setupTenantSpinner();
        }
    }
}