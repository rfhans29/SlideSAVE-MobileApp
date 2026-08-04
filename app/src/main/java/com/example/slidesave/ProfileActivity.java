package com.example.slidesave;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidesave.database.Contact;
import com.example.slidesave.database.ContactDao;
import com.example.slidesave.database.DatabaseClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    TextView tvName;
    TextView tvEmail;

    LinearLayout contactContainer;
    MaterialButton btnAddContact;

    ContactDao contactDao;
    MaterialButton btnEditProfile;
    MaterialButton btnChangePassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
            finish();
            return;
        }

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);

        contactContainer = findViewById(R.id.contactContainer);
        btnAddContact = findViewById(R.id.btnAddContact);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                mAuth.signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        contactDao =DatabaseClient.getInstance(this).getAppDatabase().contactDao();
        loadUserData();
        loadEmergencyContact();

        //Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            EditText editText = new EditText(this);
            editText.setHint("Enter New Name");
            new AlertDialog.Builder(this)
                    .setTitle("Edit Profile Name")
                    .setView(editText)
                    .setPositiveButton("Save",
                            (dialog, which) -> {
                                String newName = editText.getText().toString().trim();

                                if(!newName.isEmpty()) {
                                    UserProfileChangeRequest profileUpdates =
                                            new UserProfileChangeRequest.Builder().setDisplayName(newName).build();

                                    mAuth.getCurrentUser().updateProfile(profileUpdates)
                                            .addOnCompleteListener(task -> {

                                                if(task.isSuccessful()) {
                                                    tvName.setText(newName);
                                                    Toast.makeText(
                                                            this,
                                                            "Name Updated Successfully",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            });
                                }
                            })
                    .setNegativeButton("Cancel",null)
                    .show();
        });

        btnChangePassword.setOnClickListener(v -> {
            String email = mAuth.getCurrentUser().getEmail();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if(task.isSuccessful()) {
                            Toast.makeText(
                                    this,
                                    "Password reset email sent",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Failed to send email",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if(id == R.id.nav_profile)
                return true;

            if(id == R.id.nav_home){
                startActivity(new Intent(this,MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if(id == R.id.nav_maps){
                startActivity(new Intent(this,MapsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if(id == R.id.nav_history){
                startActivity(new Intent(this,HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadEmergencyContact(){

        while(contactContainer.getChildCount() > 1){
            contactContainer.removeViewAt(0);
        }

        String userId = mAuth.getCurrentUser().getUid();
        List<Contact> contacts = contactDao.getAllContacts(userId);

            for(Contact contact : contacts){
                LinearLayout contactView = new LinearLayout(this);
                contactView.setOrientation(LinearLayout.VERTICAL);
                contactView.setGravity(android.view.Gravity.CENTER);

                int width = (int) (90 * getResources().getDisplayMetrics().density);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                width,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(5,0,5,0);
                contactView.setLayoutParams(params);

                // circle placeholder
                TextView circle =new TextView(this);
                String initials = "";
                String[] words =contact.getName().split(" ");

                for(String word : words){
                    if(!word.isEmpty()){
                        initials += word.substring(0,1).toUpperCase();
                    }
                }

                circle.setText(initials);
                circle.setTextSize(12);
                circle.setGravity(android.view.Gravity.CENTER);
                circle.setBackgroundResource(R.drawable.profile_circle_background);

                int size = (int)(60 * getResources().getDisplayMetrics().density);

                LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(size,size);
                circle.setLayoutParams(circleParams);
                contactView.addView(circle);

                // name below
                TextView name = new TextView(this);
                name.setText(contact.getName());
                name.setTextSize(12);
                name.setGravity(android.view.Gravity.CENTER);
                contactView.addView(name);

                // add before +
                contactContainer.addView(contactView, 0);

                contactView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ContactActivity.class);
                    intent.putExtra("contactId", contact.getId());
                    startActivity(intent);
                });
            }

            btnAddContact.setOnClickListener(v -> {
                startActivity(new Intent(this,ContactActivity.class));
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmergencyContact();
    }

    //Load current user's profile information
    private void loadUserData() {
        if(mAuth.getCurrentUser() != null) {
            String name =mAuth.getCurrentUser().getDisplayName();
            String email =mAuth.getCurrentUser().getEmail();
            if(name != null)
                tvName.setText(name);
            if(email != null)
                tvEmail.setText(email);
        }
    }
}