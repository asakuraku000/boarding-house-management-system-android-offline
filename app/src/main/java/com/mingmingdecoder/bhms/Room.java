package com.mingmingdecoder.bhms;

public class Room {
    private String roomNumber;
    private String description;
    private int capacity;  // New field for tenant capacity

    public Room(String roomNumber, String description) {
        this.roomNumber = roomNumber;
        this.description = description;
        this.capacity = 1;  // Default capacity
    }

    public Room(String roomNumber, String description, int capacity) {
        this.roomNumber = roomNumber;
        this.description = description;
        this.capacity = capacity;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}