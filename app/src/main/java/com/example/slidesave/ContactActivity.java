package com.example.slidesave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidesave.database.Contact;
import com.example.slidesave.database.ContactDao;
import com.example.slidesave.database.DatabaseClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ContactActivity extends AppCompatActivity {
    EditText nameInput;
    EditText relationshipInput;
    EditText telegramInput;

    ImageView btnDelete;
    TextView titleText;
    Button addButton;
    ContactDao contactDao;
    Contact selectedContact = null;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = user.getUid();

        nameInput = findViewById(R.id.nameInput);
        relationshipInput = findViewById(R.id.relationshipInput);
        telegramInput = findViewById(R.id.telegramInput);

        btnDelete = findViewById(R.id.btnDelete);
        titleText = findViewById(R.id.titleText);

        TextView tvUserInfoBot = findViewById(R.id.tvUserInfoBot);
        TextView tvAlertBot = findViewById(R.id.tvAlertBot);

        MaterialToolbar topBar = findViewById(R.id.topBar);
        topBar.setNavigationIcon(R.drawable.ic_back);
        topBar.setNavigationOnClickListener(v -> finish());

        addButton = findViewById(R.id.saveButton);

        // connect SQLite
        contactDao = DatabaseClient
                        .getInstance(this)
                        .getAppDatabase()
                        .contactDao();

        int contactId = getIntent().getIntExtra("contactId",-1);

        if(contactId != -1){
            selectedContact = contactDao.getContactById(contactId);

            if(selectedContact != null){
                nameInput.setText(selectedContact.getName());
                relationshipInput.setText(selectedContact.getRelationship());
                telegramInput.setText(selectedContact.getTelegramChatId());
                addButton.setText("Update");
            }

            btnDelete.setVisibility(View.VISIBLE);
            titleText.setText("Edit Emergency Contact");

            btnDelete.setOnClickListener(v -> {
                if (selectedContact == null) return;

                AlertDialog dialog = new AlertDialog.Builder(ContactActivity.this)
                        .setTitle("Delete Emergency Contact")
                        .setMessage("Are you sure you want to delete this emergency contact?\n\nThis action cannot be undone.")
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton("Delete", (d, which) -> {
                            contactDao.delete(selectedContact);
                            Toast.makeText(ContactActivity.this,
                                    "Contact Deleted",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

                // Make Delete button red
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            });
        }

        addButton.setOnClickListener(v -> {
            String name =nameInput.getText().toString();
            String relationship =relationshipInput.getText().toString();
            String telegram =telegramInput.getText().toString();

            if(name.isEmpty() || relationship.isEmpty() || telegram.isEmpty()){
                Toast.makeText(this,"Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Edit Contact
            if(selectedContact != null){
                selectedContact.setName(name);
                selectedContact.setRelationship(relationship);
                selectedContact.setTelegramChatId(telegram);
                selectedContact.setUserId(userId);
                contactDao.update(selectedContact);
                Toast.makeText(this,"Contact Updated", Toast.LENGTH_SHORT).show();
                selectedContact = null;
            }

            // Add Contact
            else{
                Contact contact = new Contact(name,relationship,telegram,userId);
                contactDao.insert(contact);
                Toast.makeText(this,"Contact Added",Toast.LENGTH_SHORT).show();
            }
            clearInput();
            finish();
        });

        tvUserInfoBot.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/userinfobot"));
            startActivity(intent);
        });

        tvAlertBot.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/slidesave_alert_bot"));
            startActivity(intent);
        });
    }

    private void clearInput(){
        nameInput.setText("");
        relationshipInput.setText("");
        telegramInput.setText("");
    }
}