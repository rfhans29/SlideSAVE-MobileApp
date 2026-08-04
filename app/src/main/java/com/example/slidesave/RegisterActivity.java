package com.example.slidesave;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editConfirmPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        setupLoginLink();
    }

    public void createUser(View v) {
        //Validate user input before creating new account
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if(name.isEmpty()){
            editName.setError("Enter your name");
            editName.requestFocus();
            return;}

        if(email.isEmpty()){
            editEmail.setError("Enter email");
            editEmail.requestFocus();
            return;}

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editEmail.setError("Invalid email");
            editEmail.requestFocus();
            return;}

        if(password.length() < 6){
            editPassword.setError("Minimum 6 characters");
            editPassword.requestFocus();
            return;}

        if(!password.equals(confirmPassword)){
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return;}

        //Creating new account using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        mAuth.getCurrentUser()
                                .updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    mAuth.signOut();

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Registration Successful. Please log in.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    // Redirect to Login page
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void setupLoginLink() {
        TextView tvLogin = findViewById(R.id.tvBackToLogin);
        String text = "Already have an account? Login";
        SpannableString spannableString = new SpannableString(text);

        int start = text.indexOf("Login");
        int end = start + "Login".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);

                ds.setColor(android.graphics.Color.parseColor("#0B5E7A"));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        spannableString.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvLogin.setText(spannableString);
        tvLogin.setMovementMethod(LinkMovementMethod.getInstance());
    }
}