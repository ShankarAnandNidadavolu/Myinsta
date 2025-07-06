package com.example.myinsta;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class Forgotpassword extends AppCompatActivity {

    EditText emailEditText;
    Button resetButton;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        emailEditText = findViewById(R.id.forgetpasswordemailedittext);
        resetButton = findViewById(R.id.resetpasswordbutton);
        auth = FirebaseAuth.getInstance();


        resetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim().toLowerCase();
            if (email.isEmpty()) {
                Toast.makeText(Forgotpassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Forgotpassword.this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String error = task.getException().getMessage();
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(Forgotpassword.this, "This email is not registered", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Forgotpassword.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

    }
}
