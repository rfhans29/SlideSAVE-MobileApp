package com.example.slidesave;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView tvStatus;
    MaterialCardView statusCard;
    TextView tvMoisture;
    TextView tvTilt;
    TextView tvMotion;
    TextView tvBarricade;
    TextView tvDeviceStatus;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference sensorRef;
    DatabaseReference alertRef;
    DatabaseReference barricadeRef;
    private String currentAlertLevel = "SAFE";
    private MediaPlayer mediaPlayer;
    Button btnEmergency;
    private AlertManager alertManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        alertManager = new AlertManager(this);
        alertManager.startListening();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                            android.Manifest.permission.POST_NOTIFICATIONS
                    },
                    100);
        }

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        sensorRef = database.getReference("SensorData");
        alertRef = database.getReference("Alert");
        barricadeRef = database.getReference("Barricade");
        createNotificationChannel();

        tvDeviceStatus = findViewById(R.id.tvDeviceStatus);
        tvStatus = findViewById(R.id.tvStatus);
        statusCard = findViewById(R.id.statusCard);
        tvMoisture = findViewById(R.id.tvMoisture);
        tvTilt = findViewById(R.id.tvTilt);
        tvMotion = findViewById(R.id.tvMotion);
        tvBarricade = findViewById(R.id.tvBarricade);
        btnEmergency = findViewById(R.id.btnEmergency);
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        //Retrieve real-time sensor data from Firebase
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String moisture = "0";
                String tilt = "0";
                String acc = "0";

                if (snapshot.child("moist_value").getValue() != null) {
                    moisture = snapshot.child("moist_value").getValue().toString();
                }

                if (snapshot.child("tilt_value").getValue() != null) {
                    double tiltValue = Double.parseDouble(snapshot.child("tilt_value").getValue().toString());
                    tilt = String.format("%.2f", tiltValue);
                }

                if (snapshot.child("acc_value").getValue() != null) {
                    double accValue = Double.parseDouble(snapshot.child("acc_value").getValue().toString());
                    acc = String.format("%.2f", accValue);
                }

                //Display sensor value
                tvMoisture.setText(moisture + "%");
                tvTilt.setText(tilt + "°");
                tvMotion.setText(acc + " m/s²");

                String sensorTime = snapshot.child("sens_dateTime").getValue(String.class);

                if(sensorTime != null){
                    tvDeviceStatus.setText("Device Connected\nLast Update: " + sensorTime);
                    tvDeviceStatus.setTextColor(Color.WHITE);
                }else{
                    tvDeviceStatus.setText("Device Offline");
                    tvDeviceStatus.setTextColor(
                            getResources().getColor(
                                    android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });

        //Listen for changes in the current alert level
        alertRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String level = "SAFE";
                    if(snapshot.child("alert_level").getValue() != null) {
                            level = snapshot.child("alert_level")
                                    .getValue()
                                    .toString();
                        }
                        tvStatus.setText(level);

                    if(!level.equals(currentAlertLevel)){
                        if(level.equals("WARNING")){
                            showNotification("WARNING", "Potential Landslide Risk Detected",level);
                            }
                        else if(level.equals("DANGER")){
                            showNotification("DANGER", "Landslide Risk Detected - Barricade Activated", level);
                            }
                        else if(level.equals("SAFE")){
                            stopAlarm();
                            }
                            currentAlertLevel = level;
                        }

                        if(level.equals("SAFE")){
                            statusCard.setCardBackgroundColor(Color.parseColor("#09853c"));
                            tvStatus.setTextColor(Color.WHITE);
                        }
                        else if(level.equals("WARNING")) {
                            statusCard.setCardBackgroundColor(Color.parseColor("#FF9800"));
                            tvStatus.setTextColor(Color.WHITE);
                        }
                        else if(level.equals("DANGER")) {
                            statusCard.setCardBackgroundColor(Color.parseColor("#D32F2F"));
                            tvStatus.setTextColor(Color.WHITE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

        barricadeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String status = "CLOSED";

                        if (snapshot.child("barr_status").getValue() != null) {
                            status = snapshot.child("barr_status")
                                    .getValue()
                                    .toString();
                        }
                        tvBarricade.setText(status);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });

        //Dial the emergency number
        btnEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:999"));
            startActivity(intent);
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;}

            else if (id == R.id.nav_maps) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                overridePendingTransition(0, 0);
                return true;}

            else if (id == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this,HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;}

            else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;}

            return false;
        });
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            "slidesave_alert",
                            "SlideSAVE Alerts",
                            NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("Landslide Alert Notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message, String level) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"slidesave_alert")
                        .setSmallIcon(R.drawable.slidesave_logo)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());

        // Play alert sound
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if(level.equals("WARNING")) {
            mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound);
        }
        else if(level.equals("DANGER")){
            mediaPlayer = MediaPlayer.create(this, R.raw.danger_sound);
        }

        if(mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            if(level.equals("WARNING")) {
                new android.os.Handler().postDelayed(() -> {
                    stopAlarm();
                }, 60000); // 1 minute
            }
        }
    }

    private void stopAlarm() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}