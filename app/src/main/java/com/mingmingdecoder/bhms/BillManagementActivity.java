package com.mingmingdecoder.bhms;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.app.DatePickerDialog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class BillManagementActivity extends AppCompatActivity {
    private TextView titleText;
    private RecyclerView billsRecyclerView;
    private BillAdapter billAdapter;
    private Button addBillBtn;
    private Button autoBillSettingsBtn;
    private SearchView searchView;
    private List<Bill> billList;
    private List<Bill> filteredBillList;
    private FileHandler fileHandler;
    private String billType;
    private boolean isAutoBillingEnabled = false;
    private String billingFrequency = "monthly"; // "monthly" or "weekly"
    private int weeklyInterval = 2; // Default to every 2 weeks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_management);

        try {
            // Get bill type from intent
            billType = getIntent().getStringExtra("BILL_TYPE");
            if (billType == null) {
                billType = "rent"; // Default
            }

            // Initialize UI components
            titleText = findViewById(R.id.titleText);
            billsRecyclerView = findViewById(R.id.billsRecyclerView);
            addBillBtn = findViewById(R.id.addBillBtn);
            autoBillSettingsBtn = findViewById(R.id.autoBillSettingsBtn);
            searchView = findViewById(R.id.searchView);
            
            // Set title based on bill type
            titleText.setText(billType.substring(0, 1).toUpperCase() + billType.substring(1) + " Bills");
            
            // Initialize file handler
            fileHandler = new FileHandler(this);
            
            // Load auto-billing settings
            loadAutoBillingSettings();
            
            // Load bills from file
            billList = fileHandler.readBills(billType);
            if (billList == null) {
                billList = new ArrayList<>();
            }
            
            // Initialize filtered list
            filteredBillList = new ArrayList<>(billList);
            
            // Check if we need to generate automatic bills
            checkAndGenerateAutomaticBills();
            
            // Setup recycler view
            setupRecyclerView();
            
            // Setup search functionality
            setupSearchView();
            
            // Set click listener for add bill button
            addBillBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddBillDialog();
                }
            });
            
            // Set click listener for auto billing settings button
            autoBillSettingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAutoBillingSettingsDialog();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing Bill Management: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupSearchView() {
        try {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterBills(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterBills(newText);
                    return true;
                }
            });
            
            // Set the hint text
            searchView.setQueryHint("Search...");
            
            // Make sure the search view is not iconified by default
            searchView.setIconifiedByDefault(false);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up search view: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void filterBills(String query) {
        try {
            filteredBillList.clear();
            
            if (query.isEmpty()) {
                filteredBillList.addAll(billList);
            } else {
                String lowerCaseQuery = query.toLowerCase();
                for (Bill bill : billList) {
                    if (bill.getTenantName().toLowerCase().contains(lowerCaseQuery) || 
                        bill.getDate().contains(lowerCaseQuery) ||
                        String.valueOf(bill.getAmount()).contains(lowerCaseQuery)) {
                        filteredBillList.add(bill);
                    }
                }
            }
            
            billAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Error filtering bills: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadAutoBillingSettings() {
        try {
            // This would typically come from SharedPreferences
            // For now, just mock loading the settings
            isAutoBillingEnabled = fileHandler.getBooleanPreference(billType + "_auto_billing_enabled", false);
            billingFrequency = fileHandler.getStringPreference(billType + "_billing_frequency", "monthly");
            weeklyInterval = fileHandler.getIntPreference(billType + "_weekly_interval", 2);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading auto-billing settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void saveAutoBillingSettings() {
        try {
            // This would typically save to SharedPreferences
            fileHandler.savePreference(billType + "_auto_billing_enabled", isAutoBillingEnabled);
            fileHandler.savePreference(billType + "_billing_frequency", billingFrequency);
            fileHandler.savePreference(billType + "_weekly_interval", weeklyInterval);
        } catch (Exception e) {
            Toast.makeText(this, "Error saving auto-billing settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void checkAndGenerateAutomaticBills() {
        try {
            if (!isAutoBillingEnabled) {
                return; // Auto-billing is disabled
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date currentDate = new Date();
            
            // Get the list of tenants
            List<Tenant> tenants = fileHandler.readTenants();
            if (tenants == null || tenants.isEmpty()) {
                return; // No tenants, nothing to do
            }
            
            // Check each tenant for the need to generate a new bill
            for (Tenant tenant : tenants) {
                // Find the most recent bill for this tenant
                Bill mostRecentBill = null;
                for (Bill bill : billList) {
                    if (bill.getTenantName().equals(tenant.getName())) {
                        if (mostRecentBill == null) {
                            mostRecentBill = bill;
                        } else {
                            try {
                                Date currentBillDate = dateFormat.parse(bill.getDate());
                                Date recentBillDate = dateFormat.parse(mostRecentBill.getDate());
                                
                                if (currentBillDate.after(recentBillDate)) {
                                    mostRecentBill = bill;
                                }
                            } catch (ParseException e) {
                                // Skip this bill if date can't be parsed
                                continue;
                            }
                        }
                    }
                }
                
                // Skip if no bills found for this tenant
                if (mostRecentBill == null) {
                    continue;
                }
                
                // Calculate the next billing date
                Date lastBillDate;
                try {
                    lastBillDate = dateFormat.parse(mostRecentBill.getDate());
                } catch (ParseException e) {
                    continue; // Skip if date can't be parsed
                }
                
                Date nextBillDate = calculateNextBillingDate(lastBillDate);
                
                // Check if we need to generate a new bill (if next billing date is today or earlier)
                if (nextBillDate.compareTo(currentDate) <= 0) {
                    // Generate new bill
                    Bill newBill = new Bill(
                            tenant.getName(),
                            mostRecentBill.getAmount(),
                            dateFormat.format(nextBillDate),
                            billType
                    );
                    newBill.setPaid(false);
                    billList.add(newBill);
                    filteredBillList.add(newBill);
                }
            }
            
            // Save the updated bill list
            if (billList != null) {
                fileHandler.saveBills(billList, billType);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error generating automatic bills: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private Date calculateNextBillingDate(Date lastBillDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastBillDate);
        
        if (billingFrequency.equals("monthly")) {
            // Add one month
            calendar.add(Calendar.MONTH, 1);
        } else if (billingFrequency.equals("weekly")) {
            // Add the specified number of weeks
            calendar.add(Calendar.WEEK_OF_YEAR, weeklyInterval);
        }
        
        return calendar.getTime();
    }
    
    private void setupRecyclerView() {
        try {
            billAdapter = new BillAdapter(this, filteredBillList, new BillAdapter.OnBillClickListener() {
                @Override
                public void onBillClick(Bill bill, int position) {
                    showBillOptionsDialog(bill, position);
                }
            });
            
            billsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            billsRecyclerView.setAdapter(billAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupTenantSpinner(Spinner tenantSpinner, String selectedTenant) {
        try {
            // Load tenants into spinner
            List<Tenant> tenants = fileHandler.readTenants();
            List<String> tenantNames = new ArrayList<>();
            
            if (tenants != null && !tenants.isEmpty()) {
                for (Tenant tenant : tenants) {
                    tenantNames.add(tenant.getName());
                }
            } else {
                // Add a default option if no tenants
                tenantNames.add("No tenants available");
            }
            
            // Create adapter for tenant spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, tenantNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            
            // Set the adapter to the spinner
            tenantSpinner.setAdapter(adapter);
            
            // Set selection if editing
            if (selectedTenant != null && !selectedTenant.isEmpty()) {
                int position = tenantNames.indexOf(selectedTenant);
                if (position >= 0) {
                    tenantSpinner.setSelection(position);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up tenant spinner: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getTenantFromSpinner(Spinner tenantSpinner) {
        if (tenantSpinner.getSelectedItem() == null) {
            return "Unknown";
        }
        return tenantSpinner.getSelectedItem().toString();
    }
    
    private void showDatePicker(final EditText dateInput) {
        try {
            // Get current date as default
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            
            // Create DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format the date and set it to the EditText
                        String formattedDate = selectedYear + "-" + 
                                String.format("%02d", selectedMonth + 1) + "-" + 
                                String.format("%02d", selectedDay);
                        dateInput.setText(formattedDate);
                    },
                    year, month, day);
            
            datePickerDialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing date picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showAutoBillingSettingsDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_auto_billing_settings, null);
            builder.setView(dialogView);
            
            final CheckBox enableAutoCheckbox = dialogView.findViewById(R.id.enableAutoCheckbox);
            final RadioGroup frequencyRadioGroup = dialogView.findViewById(R.id.frequencyRadioGroup);
            final RadioButton monthlyRadio = dialogView.findViewById(R.id.monthlyRadio);
            final RadioButton weeklyRadio = dialogView.findViewById(R.id.weeklyRadio);
            final Spinner weekIntervalSpinner = dialogView.findViewById(R.id.weekIntervalSpinner);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
            
            // Set current values
            enableAutoCheckbox.setChecked(isAutoBillingEnabled);
            if (billingFrequency.equals("monthly")) {
                monthlyRadio.setChecked(true);
                weekIntervalSpinner.setEnabled(false);
            } else {
                weeklyRadio.setChecked(true);
                weekIntervalSpinner.setEnabled(true);
            }
            
            // Setup week interval spinner
            List<String> weekOptions = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                weekOptions.add(i + " Week" + (i > 1 ? "s" : ""));
            }
            ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, weekOptions);
            weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            weekIntervalSpinner.setAdapter(weekAdapter);
            weekIntervalSpinner.setSelection(weeklyInterval - 1);
            
            // Set radio group listener
            frequencyRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.weeklyRadio) {
                    weekIntervalSpinner.setEnabled(true);
                } else {
                    weekIntervalSpinner.setEnabled(false);
                }
            });
            
            final AlertDialog dialog = builder.create();
            dialog.show();
            
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Save settings
                        isAutoBillingEnabled = enableAutoCheckbox.isChecked();
                        billingFrequency = monthlyRadio.isChecked() ? "monthly" : "weekly";
                        weeklyInterval = weekIntervalSpinner.getSelectedItemPosition() + 1;
                        
                        // Save to preferences
                        saveAutoBillingSettings();
                        
                        dialog.dismiss();
                        Toast.makeText(BillManagementActivity.this, "Auto-billing settings saved", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(BillManagementActivity.this, "Error saving settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error showing auto-billing settings dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showAddBillDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_bill, null);
            builder.setView(dialogView);
            
            final Spinner tenantSpinner = dialogView.findViewById(R.id.tenantSpinner);
            final EditText amountInput = dialogView.findViewById(R.id.amountInput);
            final EditText dateInput = dialogView.findViewById(R.id.dateInput);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
            
            // Make date input read-only and show date picker on click
            dateInput.setFocusable(false);
            dateInput.setClickable(true);
            dateInput.setOnClickListener(v -> showDatePicker(dateInput));
            
            // Setup the tenant spinner
            setupTenantSpinner(tenantSpinner, null);
            
            final AlertDialog dialog = builder.create();
            dialog.show();
            
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String tenantName = getTenantFromSpinner(tenantSpinner);
                        String amountStr = amountInput.getText().toString().trim();
                        String date = dateInput.getText().toString().trim();
                        
                        if (amountStr.isEmpty() || date.isEmpty()) {
                            Toast.makeText(BillManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        double amount;
                        try {
                            amount = Double.parseDouble(amountStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(BillManagementActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        Bill bill = new Bill(tenantName, amount, date, billType);
                        billList.add(bill);
                        
                        // Also add to filtered list if it matches current filter
                        String currentQuery = searchView.getQuery().toString().toLowerCase();
                        if (currentQuery.isEmpty() || 
                            tenantName.toLowerCase().contains(currentQuery) ||
                            date.contains(currentQuery) ||
                            String.valueOf(amount).contains(currentQuery)) {
                            filteredBillList.add(bill);
                            billAdapter.notifyItemInserted(filteredBillList.size() - 1);
                        }
                        
                        // Save to file
                        fileHandler.saveBills(billList, billType);
                        
                        dialog.dismiss();
                        Toast.makeText(BillManagementActivity.this, "Bill added successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(BillManagementActivity.this, "Error adding bill: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error showing add bill dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showBillOptionsDialog(final Bill bill, final int position) {
    try {
        // Determine which payment status option to show based on current status
        String paymentOption = bill.isPaid() ? "Mark as Unpaid" : "Mark as Paid";
        
        String[] options = {"Edit", paymentOption, "Delete"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bill Options")
                .setItems(options, (dialog, which) -> {
                    try {
                        switch (which) {
                            case 0: // Edit
                                showEditBillDialog(bill, position);
                                break;
                            case 1: // Toggle payment status
                                bill.setPaid(!bill.isPaid()); // Toggle the paid status
                                billAdapter.notifyItemChanged(position);
                                fileHandler.saveBills(billList, billType);
                                String statusMessage = bill.isPaid() ? "Bill marked as paid" : "Bill marked as unpaid";
                                Toast.makeText(BillManagementActivity.this, statusMessage, Toast.LENGTH_SHORT).show();
                                break;
                            case 2: // Delete
                                showDeleteConfirmationDialog(bill, position);
                                break;
                        }
                    } catch (Exception e) {
                        Toast.makeText(BillManagementActivity.this, "Error handling bill options: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    } catch (Exception e) {
        Toast.makeText(this, "Error showing bill options: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
    
    private void showEditBillDialog(final Bill bill, final int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_bill, null);
            builder.setView(dialogView);
            
            final Spinner tenantSpinner = dialogView.findViewById(R.id.tenantSpinner);
            final EditText amountInput = dialogView.findViewById(R.id.amountInput);
            final EditText dateInput = dialogView.findViewById(R.id.dateInput);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
            
            // Make date input read-only and show date picker on click
            dateInput.setFocusable(false);
            dateInput.setClickable(true);
            dateInput.setOnClickListener(v -> showDatePicker(dateInput));
            
            // Setup the tenant spinner and pre-select current tenant
            setupTenantSpinner(tenantSpinner, bill.getTenantName());
            
            // Pre-fill fields with bill data
            amountInput.setText(String.valueOf(bill.getAmount()));
            dateInput.setText(bill.getDate());
            
            final AlertDialog dialog = builder.create();
            dialog.show();
            
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String tenantName = getTenantFromSpinner(tenantSpinner);
                        String amountStr = amountInput.getText().toString().trim();
                        String date = dateInput.getText().toString().trim();
                        
                        if (amountStr.isEmpty() || date.isEmpty()) {
                            Toast.makeText(BillManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        double amount;
                        try {
                            amount = Double.parseDouble(amountStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(BillManagementActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Update bill
                        bill.setTenantName(tenantName);
                        bill.setAmount(amount);
                        bill.setDate(date);
                        
                        // Check if bill still matches filter
                        String currentQuery = searchView.getQuery().toString().toLowerCase();
                        if (currentQuery.isEmpty() || 
                            tenantName.toLowerCase().contains(currentQuery) ||
                            date.contains(currentQuery) ||
                            String.valueOf(amount).contains(currentQuery)) {
                            billAdapter.notifyItemChanged(position);
                        } else {
                            // Remove from filtered list if no longer matches
                            filteredBillList.remove(position);
                            billAdapter.notifyItemRemoved(position);
                        }
                        
                        // Save to file
                        fileHandler.saveBills(billList, billType);
                        
                        dialog.dismiss();
                        Toast.makeText(BillManagementActivity.this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(BillManagementActivity.this, "Error updating bill: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error showing edit bill dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showDeleteConfirmationDialog(final Bill bill, final int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Bill")
                    .setMessage("Are you sure you want to delete this bill?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            // Get the position in the original list
                            int originalPosition = billList.indexOf(bill);
                            if (originalPosition != -1) {
                                billList.remove(originalPosition);
                            }
                            
                            // Remove from filtered list
                            filteredBillList.remove(position);
                            billAdapter.notifyItemRemoved(position);
                            
                            // Save to file
                            fileHandler.saveBills(billList, billType);
                                  Toast.makeText(BillManagementActivity.this, "Bill deleted successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(BillManagementActivity.this, "Error deleting bill: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing delete confirmation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}