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
import java.util.ArrayList;
import java.util.List;

public class RoomManagementActivity extends AppCompatActivity {
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private Button addRoomBtn;
    private List<Room> roomList;
    private FileHandler fileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        try {
            roomsRecyclerView = findViewById(R.id.roomsRecyclerView);
            addRoomBtn = findViewById(R.id.addRoomBtn);
            fileHandler = new FileHandler(this);

            // Load rooms from file
            roomList = readRooms();
            if (roomList == null) {
                roomList = new ArrayList<>();
            }

            setupRecyclerView();

            addRoomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddRoomDialog();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing Room Management: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
    
    private boolean saveRooms(List<Room> rooms) {
        try {
            // Save rooms to SharedPreferences
            String json = new com.google.gson.Gson().toJson(rooms);
            fileHandler.savePreference("rooms_data", json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setupRecyclerView() {
        try {
            roomAdapter = new RoomAdapter(this, roomList, new RoomAdapter.OnRoomClickListener() {
                @Override
                public void onRoomClick(Room room, int position) {
                    showRoomOptionsDialog(room, position);
                }
            });

            roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            roomsRecyclerView.setAdapter(roomAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddRoomDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_room, null);
            builder.setView(dialogView);

            final EditText roomNumberInput = dialogView.findViewById(R.id.roomNumberInput);
            final EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
            final EditText capacityInput = dialogView.findViewById(R.id.capacityInput);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

            final AlertDialog dialog = builder.create();
            dialog.show();

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String roomNumber = roomNumberInput.getText().toString().trim();
                        String description = descriptionInput.getText().toString().trim();
                        String capacityStr = capacityInput.getText().toString().trim();
                        
                        if (roomNumber.isEmpty()) {
                            Toast.makeText(RoomManagementActivity.this, "Please enter room number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if room number already exists
                        for (Room existingRoom : roomList) {
                            if (existingRoom.getRoomNumber().equals(roomNumber)) {
                                Toast.makeText(RoomManagementActivity.this, "Room number already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        
                        // Parse capacity with validation
                        int capacity = 1; // Default value
                        try {
                            if (!capacityStr.isEmpty()) {
                                capacity = Integer.parseInt(capacityStr);
                                if (capacity <= 0) {
                                    Toast.makeText(RoomManagementActivity.this, "Capacity must be a positive number", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(RoomManagementActivity.this, "Please enter a valid number for capacity", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Room room = new Room(roomNumber, description, capacity);
                        roomList.add(room);
                        roomAdapter.notifyItemInserted(roomList.size() - 1);

                        // Save to SharedPreferences
                        saveRooms(roomList);

                        dialog.dismiss();
                        Toast.makeText(RoomManagementActivity.this, "Room added successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(RoomManagementActivity.this, "Error adding room: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Error displaying Add Room dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showRoomOptionsDialog(final Room room, final int position) {
        try {
            String[] options = {"Edit", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(room.getRoomNumber())
                    .setItems(options, (dialog, which) -> {
                        try {
                            switch (which) {
                                case 0:
                                    showEditRoomDialog(room, position);
                                    break;
                                case 1:
                                    showDeleteConfirmationDialog(room, position);
                                    break;
                            }
                        } catch (Exception e) {
                            Toast.makeText(RoomManagementActivity.this, "Error handling room options: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing room options: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showEditRoomDialog(final Room room, final int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_room, null);
            builder.setView(dialogView);

            final EditText roomNumberInput = dialogView.findViewById(R.id.roomNumberInput);
            final EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
            final EditText capacityInput = dialogView.findViewById(R.id.capacityInput);
            Button saveBtn = dialogView.findViewById(R.id.saveBtn);
            Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

            roomNumberInput.setText(room.getRoomNumber());
            descriptionInput.setText(room.getDescription());
            capacityInput.setText(String.valueOf(room.getCapacity()));

            final AlertDialog dialog = builder.create();
            dialog.show();

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String roomNumber = roomNumberInput.getText().toString().trim();
                        String description = descriptionInput.getText().toString().trim();
                        String capacityStr = capacityInput.getText().toString().trim();

                        if (roomNumber.isEmpty()) {
                            Toast.makeText(RoomManagementActivity.this, "Please enter room number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if the new room number already exists (if it's different from the current one)
                        if (!roomNumber.equals(room.getRoomNumber())) {
                            for (Room existingRoom : roomList) {
                                if (existingRoom.getRoomNumber().equals(roomNumber)) {
                                    Toast.makeText(RoomManagementActivity.this, "Room number already exists", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        
                        // Parse capacity with validation
                        int capacity = 1; // Default value
                        try {
                            if (!capacityStr.isEmpty()) {
                                capacity = Integer.parseInt(capacityStr);
                                if (capacity <= 0) {
                                    Toast.makeText(RoomManagementActivity.this, "Capacity must be a positive number", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(RoomManagementActivity.this, "Please enter a valid number for capacity", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        room.setRoomNumber(roomNumber);
                        room.setDescription(description);
                        room.setCapacity(capacity);
                        roomAdapter.notifyItemChanged(position);

                        saveRooms(roomList);

                        dialog.dismiss();
                        Toast.makeText(RoomManagementActivity.this, "Room updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(RoomManagementActivity.this, "Error updating room: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Error displaying Edit Room dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmationDialog(final Room room, final int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Room")
                    .setMessage("Are you sure you want to delete " + room.getRoomNumber() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            roomList.remove(position);
                            roomAdapter.notifyItemRemoved(position);

                            saveRooms(roomList);

                            Toast.makeText(RoomManagementActivity.this, "Room deleted successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(RoomManagementActivity.this, "Error deleting room: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing delete confirmation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}