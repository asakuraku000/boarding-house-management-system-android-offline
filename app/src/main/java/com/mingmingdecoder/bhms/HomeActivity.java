package com.mingmingdecoder.bhms;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private FileHandler fileHandler;
    private TextView tvTenantCount;
    private TextView tvRoomCount;
    private TextView tvPaidCount;
    private TextView tvUnpaidCount;
    private TextView tvWelcomeMessage;
    private TextView tvDate;
    private LineChart chartMonthlyBills;
    private PieChart chartPaymentDistribution;
    private RecyclerView recyclerRecentBills;
  private DrawerLayout drawerLayout;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize FileHandler
        fileHandler = new FileHandler(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Views
        tvTenantCount = findViewById(R.id.tv_tenant_count);
        tvRoomCount = findViewById(R.id.tv_room_count);
        tvPaidCount = findViewById(R.id.tv_paid_count);
        tvUnpaidCount = findViewById(R.id.tv_unpaid_count);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);
        tvDate = findViewById(R.id.tv_date);
        chartMonthlyBills = findViewById(R.id.chart_monthly_bills);
        chartPaymentDistribution = findViewById(R.id.chart_payment_distribution);
        recyclerRecentBills = findViewById(R.id.recycler_recent_bills);

        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        tvDate.setText(dateFormat.format(new Date()));

        // Set welcome message
    //    String username = fileHandler.getStringPreference("current_user", "Admin");
//        tvWelcomeMessage.setText("Welcome back, " + username + "!");

        // Setup FloatingActionButton
       

        // Setup RecyclerView
        recyclerRecentBills.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup button click listeners
        findViewById(R.id.btn_view_all_bills).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to bills list activity
                Intent intent = new Intent(HomeActivity.this, AllRecentBillsActivity.class);
        startActivity(intent);
             //   Toast.makeText(HomeActivity.this, "View All Bills clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Load data
        loadDashboardData();
       // Inside your onCreate() method
// Add these lines after initializing your views
CardView cardTenants = findViewById(R.id.tenant_card);  // You'll need to add this ID to your XML
CardView cardRooms = findViewById(R.id.room_card);      // You'll need to add this ID to your XML
CardView cardPaidBills = findViewById(R.id.paid_card);  // You'll need to add this ID to your XML
CardView cardUnpaidBills = findViewById(R.id.unpaid_card); // You'll need to add this ID to your XML

cardTenants.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
 //   intent.putExtra("section", "tenants");
    startActivity(intent);
});

cardRooms.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
  //  intent.putExtra("section", "rooms");
    startActivity(intent);
});

cardPaidBills.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
   // intent.putExtra("section", "paid_bills");
    startActivity(intent);
});

cardUnpaidBills.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
//    intent.putExtra("section", "unpaid_bills");
    startActivity(intent);
});
        
   //   Toolbar toolbar = findViewById(R.id.toolbar);
     //   setSupportActionBar(toolbar);
        
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup hamburger icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        

    }

 @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        if (id == R.id.nav_tenants) {
            Intent intent = new Intent(HomeActivity.this, TenantManagementActivity.class);
         //   intent.putExtra("tenant", "tenants");
            startActivity(intent);
            
        } else if (id == R.id.nav_rooms) {
            Intent intent = new Intent(HomeActivity.this, RoomManagementActivity.class);
            //intent.putExtra("room", "rooms");
            startActivity(intent);
        } else if (id == R.id.nav_rent_bills) {
           Intent intent = new Intent(HomeActivity.this, BillManagementActivity.class);
            intent.putExtra("BILL_TYPE", "rent");
            startActivity(intent);
        } else if (id == R.id.nav_water_bills) {
           Intent intent = new Intent(HomeActivity.this, BillManagementActivity.class);
            intent.putExtra("BILL_TYPE", "water");
            startActivity(intent);
        }else if (id == R.id.nav_electric_bills) {
            Intent intent = new Intent(HomeActivity.this, BillManagementActivity.class);
            intent.putExtra("BILL_TYPE", "electric");
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            // Launch settings activity
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        } 

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    
   @Override
    public void onBackPressed() {
        // Close drawer on back press if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Load tenant data
        List<Tenant> tenants = fileHandler.readTenants();
        tvTenantCount.setText(String.valueOf(tenants.size()));

        // Count unique rooms
        Set<String> uniqueRooms = new HashSet<>();
        for (Tenant tenant : tenants) {
            uniqueRooms.add(tenant.getRoom());
        }
        tvRoomCount.setText(String.valueOf(uniqueRooms.size()));

        // Load bill data
        List<Bill> rentBills = fileHandler.readBills("rent");
        List<Bill> waterBills = fileHandler.readBills("water");
        List<Bill> electricBills = fileHandler.readBills("electric");

        // Combine all bills
        List<Bill> allBills = new ArrayList<>();
        allBills.addAll(rentBills);
        allBills.addAll(waterBills);
        allBills.addAll(electricBills);

        // Count paid and unpaid bills
        int paidCount = 0;
        int unpaidCount = 0;
        for (Bill bill : allBills) {
            if (bill.isPaid()) {
                paidCount++;
            } else {
                unpaidCount++;
            }
        }

        tvPaidCount.setText(String.valueOf(paidCount));
        tvUnpaidCount.setText(String.valueOf(unpaidCount));

        // Setup charts
        setupMonthlyBillsChart(rentBills, waterBills, electricBills);
        setupPaymentDistributionChart(paidCount, unpaidCount);

        // Setup recent bills recycler view
        setupRecentBillsRecyclerView(allBills);
        
    }

    private void setupMonthlyBillsChart(List<Bill> rentBills, List<Bill> waterBills, List<Bill> electricBills) {
    // Define constants
    final int MONTHS_TO_DISPLAY = 6;
    
    // Lists to hold our entries and labels
    ArrayList<Entry> rentEntries = new ArrayList<>();
    ArrayList<Entry> waterEntries = new ArrayList<>();
    ArrayList<Entry> electricEntries = new ArrayList<>();
    ArrayList<String> monthLabels = new ArrayList<>();

    // Get monthly totals for each bill type
    Map<Integer, Double> rentByMonth = calculateMonthlyTotals(rentBills);
    Map<Integer, Double> waterByMonth = calculateMonthlyTotals(waterBills);
    Map<Integer, Double> electricByMonth = calculateMonthlyTotals(electricBills);
    
    // Get the most recent months for display in chronological order
    List<Integer> recentMonths = getRecentMonths(MONTHS_TO_DISPLAY);
    
    // Create line entries for recent months
    for (int i = 0; i < recentMonths.size(); i++) {
        int month = recentMonths.get(i);
        
        rentEntries.add(new Entry(i, rentByMonth.get(month).floatValue()));
        waterEntries.add(new Entry(i, waterByMonth.get(month).floatValue()));
        electricEntries.add(new Entry(i, electricByMonth.get(month).floatValue()));
        
        // Add month name
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1); // Convert to 0-based Calendar month
        monthLabels.add(new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime()));
    }

    // Create line datasets
    LineDataSet rentDataSet = createLineDataSet(rentEntries, "Rent", Color.rgb(76, 175, 80)); // Green
    LineDataSet waterDataSet = createLineDataSet(waterEntries, "Water", Color.rgb(33, 150, 243)); // Blue
    LineDataSet electricDataSet = createLineDataSet(electricEntries, "Electric", Color.rgb(255, 193, 7)); // Amber

    // Combine datasets
    LineData lineData = new LineData(rentDataSet, waterDataSet, electricDataSet);
    
    // Configure chart appearance
    configureChartAxis(chartMonthlyBills, monthLabels);
    
    // Set data and display
    chartMonthlyBills.setData(lineData);
    chartMonthlyBills.getDescription().setEnabled(false);
    chartMonthlyBills.getLegend().setEnabled(true);
    chartMonthlyBills.setDrawGridBackground(false);
    chartMonthlyBills.animateXY(1000, 1000);
    chartMonthlyBills.invalidate();
}

// Helper method to calculate monthly totals for a bill type
private Map<Integer, Double> calculateMonthlyTotals(List<Bill> bills) {
    Map<Integer, Double> monthlyTotals = new HashMap<>();
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
    
    // Initialize map for all months (1-12)
    for (int i = 1; i <= 12; i++) {
        monthlyTotals.put(i, 0.0);
    }
    
    // Sum bills by month
    for (Bill bill : bills) {
        try {
            Date date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(bill.getDate());
            int month = Integer.parseInt(monthFormat.format(date));
            monthlyTotals.put(month, monthlyTotals.get(month) + bill.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    return monthlyTotals;
}

// Helper method to get a list of recent months in chronological order
private List<Integer> getRecentMonths(int count) {
    List<Integer> months = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    int currentMonth = calendar.get(Calendar.MONTH) + 1; // Convert to 1-based month
    
    for (int i = 0; i < count; i++) {
        int month = currentMonth - i;
        if (month <= 0) month += 12; // Handle previous year
        months.add(0, month); // Add to beginning for chronological order
    }
    
    return months;
}

// Helper method to create and style a line data set
private LineDataSet createLineDataSet(ArrayList<Entry> entries, String label, int color) {
    LineDataSet dataSet = new LineDataSet(entries, label);
    dataSet.setColor(color);
    dataSet.setLineWidth(2f);
    dataSet.setCircleColor(color);
    dataSet.setCircleRadius(4f);
    dataSet.setDrawCircleHole(true);
    dataSet.setCircleHoleRadius(2f);
    dataSet.setValueTextSize(10f);
    dataSet.setDrawValues(true);
    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smoother curve
    dataSet.setDrawFilled(true);
    dataSet.setFillAlpha(50); // Semi-transparent fill
    dataSet.setFillColor(color);
    return dataSet;
}

// Helper method to configure chart axes
private void configureChartAxis(LineChart chart, ArrayList<String> labels) {
    // X-axis configuration
    XAxis xAxis = chart.getXAxis();
    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setGranularity(1f);
    xAxis.setDrawGridLines(false);
    xAxis.setTextSize(12f);
    
    // Y-axis configuration 
    YAxis leftAxis = chart.getAxisLeft();
    leftAxis.setDrawGridLines(true);
    leftAxis.setSpaceTop(30f);
    leftAxis.setAxisMinimum(0f); // Start at zero
    leftAxis.setTextSize(12f);
    
    // Disable right Y-axis
    chart.getAxisRight().setEnabled(false);
    
    // Additional chart styling
    chart.setExtraOffsets(10f, 10f, 10f, 10f);
}

    private void setupPaymentDistributionChart(int paidCount, int unpaidCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(paidCount, "Paid"));
        entries.add(new PieEntry(unpaidCount, "Unpaid"));

        PieDataSet dataSet = new PieDataSet(entries, "Payment Status");
        dataSet.setColors(Color.rgb(76, 175, 80), Color.rgb(244, 67, 54)); // Green, Red
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        chartPaymentDistribution.setData(data);
        chartPaymentDistribution.getDescription().setEnabled(false);
        chartPaymentDistribution.setCenterText("Bill Payments");
        chartPaymentDistribution.setCenterTextSize(16f);
        chartPaymentDistribution.setHoleRadius(40f);
        chartPaymentDistribution.setTransparentCircleRadius(45f);
        chartPaymentDistribution.setDrawEntryLabels(false);
        chartPaymentDistribution.getLegend().setEnabled(true);
        chartPaymentDistribution.invalidate();
    }

    private void setupRecentBillsRecyclerView(List<Bill> allBills) {
        // Sort bills by date (newest first)
        allBills.sort((b1, b2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                Date date1 = format.parse(b1.getDate());
                Date date2 = format.parse(b2.getDate());
                return date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Take most recent 5 bills or less
        List<Bill> recentBills = allBills.size() > 5 ? allBills.subList(0, 5) : allBills;
        
        // Set adapter (implementation of RecentBillsAdapter not shown)
         RecentBillsAdapter adapter = new RecentBillsAdapter(recentBills);
         recyclerRecentBills.setAdapter(adapter);
        
        // Placeholder message if no bills
        if (recentBills.isEmpty()) {
            // Show empty state
        }
    }

  
    

}