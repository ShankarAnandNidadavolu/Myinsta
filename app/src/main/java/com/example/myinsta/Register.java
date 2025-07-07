package com.example.myinsta;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {
    protected static int image_picker_code = 1;
    private EditText etusername, etemail, etpassword, etbio;
    private AppCompatButton selectimage, signup;
    private TextView login;
    private CircleImageView previewimage;
    private Uri imageuri;
    private ImageView eye;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etusername= (EditText) findViewById(R.id.regusernameedittext);
        etemail= (EditText) findViewById(R.id.regemailedittext);
        etpassword= (EditText) findViewById(R.id.regpasswordedittext);
        etbio= (EditText) findViewById(R.id.regbioedittext);
        selectimage= (AppCompatButton) findViewById(R.id.btnSelectImage);
        signup= (AppCompatButton) findViewById(R.id.regsignupbutton);
        login= (TextView) findViewById(R.id.regloginbutton);
        previewimage= (CircleImageView) findViewById(R.id.profileImage);
        eye=findViewById(R.id.register_eye);

        eye.setOnClickListener(e->{
            android.graphics.Typeface typeface = etpassword.getTypeface();
            if(etpassword.getInputType()==(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)){
                etpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eye.setImageResource(R.drawable.visibility_off_24px);
            }else {
                etpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eye.setImageResource(R.drawable.visibility_24px);
            }
            etpassword.setTypeface(typeface);
            etpassword.setSelection(etpassword.length());
        });

        login.setOnClickListener(v -> {
            finish();
        });

        previewimage.setOnClickListener(v->loadimage());
        selectimage.setOnClickListener(v->loadimage());
        signup.setOnClickListener(v->uploaduserdata());

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {
            Log.d("Cloudinary", "Already initialized");
        }



    }


    private void loadimage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, image_picker_code);
    }

    ;

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == image_picker_code && res == RESULT_OK && data != null) {
            imageuri = data.getData();
            Glide.with(this).load(imageuri).circleCrop().into(previewimage);

        }
    }

    private void uploaduserdata() {
        String username=etusername.getText().toString().trim();
        String email=etemail.getText().toString().trim();
        String password=etpassword.getText().toString().trim();
        String bio=etbio.getText().toString().trim();

        signup.setEnabled(false);
        signup.setText("Loading...");
        signup.setTextColor(Color.GRAY);
        signup.setBackgroundResource(R.drawable.edittext_background);

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || imageuri == null) {
            Toast.makeText(this, "Fill all the fields", Toast.LENGTH_SHORT).show();
            showsignupbutton();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            showsignupbutton();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            showsignupbutton();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").whereEqualTo("username", username).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                Toast.makeText(this, "Username already taken. Choose another one.", Toast.LENGTH_SHORT).show();
                showsignupbutton();
                return;
            }

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                    if (emailExists) {
                        Toast.makeText(this, "Email already registered. Try logging in.", Toast.LENGTH_SHORT).show();
                        showsignupbutton();
                        return;
                    }

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            String uid = authTask.getResult().getUser().getUid();

                            MediaManager.get().upload(imageuri).callback(new UploadCallback() {
                                @Override
                                public void onStart(String requestId) {
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    String imageurl = resultData.get("secure_url").toString();

                                    User user = new User(uid, username, email, imageurl, bio);
                                    FirebaseFirestore.getInstance().collection("users").document(uid).set(user).addOnSuccessListener(v -> {
                                        Map<String, Object> follow = new HashMap<>();
                                        follow.put("note", "list initialized");
                                        follow.put("createdAt", FieldValue.serverTimestamp());
                                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                                .collection("followers").document("metadata").set(follow);
                                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                                .collection("following").document("metadata").set(follow);

                                        Toast.makeText(Register.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register.this, MainActivity.class));
                                        showsignupbutton();
                                        finish();
                                    }).addOnFailureListener(e -> {
                                        showsignupbutton();
                                        Toast.makeText(Register.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    showsignupbutton();
                                    Toast.makeText(Register.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {
                                }
                            }).dispatch();
                        } else {
                            showsignupbutton();
                            Toast.makeText(this, "Failed to SignUp: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    showsignupbutton();
                    Toast.makeText(this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showsignupbutton() {
        signup.setEnabled(true);
        signup.setText("sign up");
        signup.setTextColor(Color.WHITE);
        signup.setBackgroundResource(R.drawable.loginbutton_background);
    }

}
