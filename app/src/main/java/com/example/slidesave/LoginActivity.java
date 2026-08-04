package com.example.slidesave;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.editPassword);
        setupRegisterLink();
    }

    private void setupRegisterLink() {
        TextView tvRegisterHint = findViewById(R.id.tvRegisterHint);
        String text = "Don't have an account? Sign Up";
        SpannableString spannableString = new SpannableString(text);

        int start = text.indexOf("Sign Up");
        int end = start + "Sign Up".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
        tvRegisterHint.setText(spannableString);
        tvRegisterHint.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void loginUser(View v) {
        // Retrieve email and password entered by the user
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();
        //Validate login input
        if (e.isEmpty()) {
            email.setError("Please enter email");
            email.requestFocus();
            return;
        }
        if (p.isEmpty()) {
            password.setError("Please enter password");
            password.requestFocus();
            return;
        }

        //Authenticate the user using Firebase Authentication
        mAuth.signInWithEmailAndPassword(e, p).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,"Login Successful", Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    public void forgotPassword(View v) {
        String e = email.getText().toString().trim();
        if (e.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT
            ).show();
            return;
        }

        mAuth.sendPasswordResetEmail(e).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,"Password reset email sent",Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}