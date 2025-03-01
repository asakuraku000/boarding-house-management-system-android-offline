package com.mingmingdecoder.bhms;
import android.content.SharedPreferences;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String USER_FILE = "users.json";
    private static final String TENANT_FILE = "tenants.json";
    private static final String BOARDING_HOUSE_DATA_FILE = "boarding_house_data.json";
    private static final String RENT_BILLS_FILE = "rent_bills.json";
    private static final String WATER_BILLS_FILE = "water_bills.json";
    private static final String ELECTRIC_BILLS_FILE = "electric_bills.json";
    
    private Context context;
    private Gson gson;

    public FileHandler(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    // User methods
    public boolean saveUser(User user) {
        List<User> users = readUsers();
        if (users == null) {
            users = new ArrayList<>();
        }
        
        // Check if username already exists
        for (User existingUser : users) {
            if (existingUser.getUsername().equals(user.getUsername())) {
                return false;
            }
        }
        
        users.add(user);
        return writeToFile(USER_FILE, gson.toJson(users));
    }
    
    public boolean verifyUser(String username, String password) {
        List<User> users = readUsers();
        if (users == null) {
            return false;
        }
        
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<User> readUsers() {
        String json = readFromFile(USER_FILE);
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<User>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // Tenant methods
    public boolean saveTenants(List<Tenant> tenants) {
        return writeToFile(TENANT_FILE, gson.toJson(tenants));
    }
    
    public List<Tenant> readTenants() {
        String json = readFromFile(TENANT_FILE);
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Tenant>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // Bill methods
    public boolean saveBills(List<Bill> bills, String billType) {
        String filename = getBillFileName(billType);
        return writeToFile(filename, gson.toJson(bills));
    }
    
    public List<Bill> readBills(String billType) {
        String filename = getBillFileName(billType);
        String json = readFromFile(filename);
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Bill>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    private String getBillFileName(String billType) {
        switch (billType.toLowerCase()) {
            case "rent":
                return RENT_BILLS_FILE;
            case "water":
                return WATER_BILLS_FILE;
            case "electric":
                return ELECTRIC_BILLS_FILE;
            default:
                return RENT_BILLS_FILE;
        }
    }
    
    // Boarding house data methods
    public boolean saveBoardingHouseData(BoardingHouseData data) {
        return writeToFile(BOARDING_HOUSE_DATA_FILE, gson.toJson(data));
    }
    
    public BoardingHouseData readBoardingHouseData() {
        String json = readFromFile(BOARDING_HOUSE_DATA_FILE);
        if (json.isEmpty()) {
            return new BoardingHouseData("ROOM M1", 1000, 0);
        }
        
        return gson.fromJson(json, BoardingHouseData.class);
    }
    
    // Utility methods
    private boolean writeToFile(String filename, String content) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String readFromFile(String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
            br.close();
            isr.close();
            fis.close();
            
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
   
public boolean getBooleanPreference(String key, boolean defaultValue) {
    SharedPreferences sharedPreferences = context.getSharedPreferences("BHMSPrefs", Context.MODE_PRIVATE);
    return sharedPreferences.getBoolean(key, defaultValue);
}

public String getStringPreference(String key, String defaultValue) {
    SharedPreferences sharedPreferences = context.getSharedPreferences("BHMSPrefs", Context.MODE_PRIVATE);
    return sharedPreferences.getString(key, defaultValue);
}

public int getIntPreference(String key, int defaultValue) {
    SharedPreferences sharedPreferences = context.getSharedPreferences("BHMSPrefs", Context.MODE_PRIVATE);
    return sharedPreferences.getInt(key, defaultValue);
}

public void savePreference(String key, boolean value) {
    SharedPreferences sharedPreferences = context.getSharedPreferences("BHMSPrefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, value);
    editor.apply();
}

public void savePreference(String key, String value) {
    SharedPreferences sharedPreferences = context.getSharedPreferences("BHMSPrefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(key, value);
    editor.apply();
}

public void savePreference(String key, int value) {
    SharedPreferences sharedPreferences = context.getSharedPreferences("BHMSPrefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putInt(key, value);
    editor.apply();
}
    
}

// User class
 class User {
    private String username;
    private String password;
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

// Tenant class
class Tenant {
    private String name;
    private String contact;
    private String room;
    
    public Tenant(String name, String contact, String room) {
        this.name = name;
        this.contact = contact;
        this.room = room;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
}

// Bill class
class Bill {
    private String tenantName;
    private double amount;
    private String date;
    private String type;
    private boolean isPaid;
    
    public Bill(String tenantName, double amount, String date, String type) {
        this.tenantName = tenantName;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.isPaid = false;
    }
    
    public String getTenantName() {
        return tenantName;
    }
    
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isPaid() {
        return isPaid;
    }
    
    public void setPaid(boolean paid) {
        isPaid = paid;
    }
}

// BoardingHouseData class
 class BoardingHouseData {
    private String roomNumber;
    private double balance;
    private int coins;
    
    public BoardingHouseData(String roomNumber, double balance, int coins) {
        this.roomNumber = roomNumber;
        this.balance = balance;
        this.coins = coins;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public int getCoins() {
        return coins;
    }
    
    public void setCoins(int coins) {
        this.coins = coins;
    }
}