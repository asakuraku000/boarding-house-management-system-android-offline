package com.mingmingdecoder.bhms;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class TenantManagementActivity extends AppCompatActivity {
    private RecyclerView tenantsRecyclerView;
    private TenantAdapter tenantAdapter;
    private Button addTenantBtn;
    private List<Tenant> tenantList;
    private List<Room> roomList;
    private FileHandler fileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_management);

        try {
            tenantsRecyclerView = findViewById(R.id.tenantsRecyclerView);
            addTenantBtn = findViewById(R.id.addTenantBtn);
            fileHandler = new FileHandler(this);

            // Load tenants from file
            tenantList = fileHandler.readTenants();
            if (tenantList == null) {
                tenantList = new ArrayList<>();
            }
            
            // Load rooms from file
            roomList = readRooms();
            if (roomList == null) {
                roomList = new ArrayList<>();
            }

            setupRecyclerView();

            addTenantBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddTenantDialog();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing Tenant Management: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private List<Room> readRooms() {
        try {
            // Get rooms from SharedPreferences
            String roomsJson = fileHandler.getStringPreference("rooms_data", "");
            if (roomsJson.isEmpty()) {
                return new ArrayList<>();
            }
            
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Room>>(){}.getType();
            return new com.google.gson.Gson().fromJson(roomsJson, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void setupRecyclerView() {
        try {
            tenantAdapter = new TenantAdapter(this, tenantList, new TenantAdapter.OnTenantClickListener() {
                @Override
                public void onTenantClick(Tenant tenant, int position) {
                    showTenantOptionsDialog(tenant, position);
                }
            });

            tenantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            tenantsRecyclerView.setAdapter(tenantAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRoomSpinner(Spinner roomSpinner, Tenant tenant) {
        try {
            // Create a list of rooms
            List<String> roomOptions = new ArrayList<>();
            roomOptions.add("Not Assigned"); // Default option
            
            // Add rooms from the roomList
            for (Room room : roomList) {
                roomOptions.add(room.getRoomNumber());
            }
            
            // Create an ArrayAdapter using a simple spinner layout and the rooms list
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, roomOptions);
            
            // Specify the layout to use when the list of choices appears
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            
            // Apply the adapter to the spinner
            roomSpinner.setAdapter(roomAdapter);
            
            // For edit dialog, set the selection if the tenant already has a room
            if (tenant != null && tenant.getRoom() != null) {
                int position = roomOptions.indexOf(tenant.getRoom());
                if (position >= 0) {
                    roomSpinner.setSelection(position);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up room spinner: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
    }

    private String getRoomFromSpinner(Spinner roomSpinner) {
        if (roomSpinner.getSelectedItem() == null) {
            // Return a default value or handle the null case
            return "Not Assigned";
        }
        return roomSpinner.getSelectedItem().toString();
    }

    private void showAddTenantDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_tenant, null);
            builder.setView(dialogView);

            final EditText nameInput = dialogView.findViewById(R.id.nameInput);
            final EditText contactInput = dialogView.findViewById(R.id.contactInput);
            final Spinner roomSpinner = dialogView.findViewById(R.id.roomSpinner);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

            // Setup room spinner with available rooms
            setupRoomSpinner(roomSpinner, null);

            final AlertDialog dialog = builder.create();
            dialog.show();

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String name = nameInput.getText().toString().trim();
                        String contact = contactInput.getText().toString().trim();
                        String room = getRoomFromSpinner(roomSpinner);

                        if (name.isEmpty() || contact.isEmpty()) {
                            Toast.makeText(TenantManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Tenant tenant = new Tenant(name, contact, room);
                        tenantList.add(tenant);
                        tenantAdapter.notifyItemInserted(tenantList.size() - 1);

                        // Save to file
                        fileHandler.saveTenants(tenantList);

                        dialog.dismiss();
                        Toast.makeText(TenantManagementActivity.this, "Tenant added successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(TenantManagementActivity.this, "Error adding tenant: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Error displaying Add Tenant dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showTenantOptionsDialog(final Tenant tenant, final int position) {
        try {
            String[] options = {"Edit", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(tenant.getName())
                    .setItems(options, (dialog, which) -> {
                        try {
                            switch (which) {
                                case 0:
                                    showEditTenantDialog(tenant, position);
                                    break;
                                case 1:
                                    showDeleteConfirmationDialog(tenant, position);
                                    break;
                            }
                        } catch (Exception e) {
                            Toast.makeText(TenantManagementActivity.this, "Error handling tenant options: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing tenant options: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showEditTenantDialog(final Tenant tenant, final int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_tenant, null);
            builder.setView(dialogView);

            final EditText nameInput = dialogView.findViewById(R.id.nameInput);
            final EditText contactInput = dialogView.findViewById(R.id.contactInput);
            final Spinner roomSpinner = dialogView.findViewById(R.id.roomSpinner);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

            nameInput.setText(tenant.getName());
            contactInput.setText(tenant.getContact());
            
            // Setup room spinner with available rooms
            setupRoomSpinner(roomSpinner, tenant);

            final AlertDialog dialog = builder.create();
            dialog.show();

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String name = nameInput.getText().toString().trim();
                        String contact = contactInput.getText().toString().trim();
                        String room = getRoomFromSpinner(roomSpinner);

                        if (name.isEmpty() || contact.isEmpty()) {
                            Toast.makeText(TenantManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        tenant.setName(name);
                        tenant.setContact(contact);
                        tenant.setRoom(room);
                        tenantAdapter.notifyItemChanged(position);

                        fileHandler.saveTenants(tenantList);

                        dialog.dismiss();
                        Toast.makeText(TenantManagementActivity.this, "Tenant updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(TenantManagementActivity.this, "Error updating tenant: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Error displaying Edit Tenant dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmationDialog(final Tenant tenant, final int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Tenant")
                    .setMessage("Are you sure you want to delete " + tenant.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            tenantList.remove(position);
                            tenantAdapter.notifyItemRemoved(position);

                            fileHandler.saveTenants(tenantList);

                            Toast.makeText(TenantManagementActivity.this, "Tenant deleted successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(TenantManagementActivity.this, "Error deleting tenant: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing delete confirmation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the room list in case rooms were added/modified/deleted
        roomList = readRooms();
    }
}