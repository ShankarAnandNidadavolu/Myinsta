package com.example.myinsta;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister,forgotpassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ImageView eye;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername=findViewById(R.id.loginusernameedittext);
        etPassword=findViewById(R.id.loginpasswordedittext);
        btnLogin=findViewById(R.id.loginsigninbutton);
        tvRegister=(TextView) findViewById(R.id.signupbutton);
        eye=findViewById(R.id.login_eye);
        forgotpassword= (TextView) findViewById(R.id.forgotPassword);

        eye.setOnClickListener(e->{
            android.graphics.Typeface typeface = etPassword.getTypeface();

            if(etPassword.getInputType()==(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)){
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eye.setImageResource(R.drawable.visibility_off_24px);
            }else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eye.setImageResource(R.drawable.visibility_24px);
            }
            etPassword.setTypeface(typeface);
            etPassword.setSelection(etPassword.length());
        });

        tvRegister.setOnClickListener(v->{
            startActivity(new Intent(this,Register.class));
        });

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class));
        });

        btnLogin.setOnClickListener(v -> loginUser());

        forgotpassword.setOnClickListener(f->{
            startActivity(new Intent(Login.this, Forgotpassword.class));
        });

    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        btnLogin.setEnabled(false);
        btnLogin.setText("Loading...");
        btnLogin.setTextColor(Color.GRAY);
        btnLogin.setBackgroundResource(R.drawable.edittext_background);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            btnLogin.setTextColor(Color.WHITE);
            btnLogin.setBackgroundResource(R.drawable.loginbutton_background);
            return;
        }

        firestore.collection("users").whereEqualTo("username", username).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                btnLogin.setTextColor(Color.WHITE);
                btnLogin.setBackgroundResource(R.drawable.loginbutton_background);
            } else {
                String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");

                mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    btnLogin.setTextColor(Color.WHITE);
                    btnLogin.setBackgroundResource(R.drawable.loginbutton_background);
                    finish();
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            btnLogin.setTextColor(Color.WHITE);
            btnLogin.setBackgroundResource(R.drawable.loginbutton_background);
        });
    }
}
