package com.example.myinsta;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class EditprofileFragment extends Fragment {

private ImageView profilephoto, eye1,eye2;
private AppCompatButton selectimage;
private EditText Bio,currentpassword,newpassword;
private Button save;
private String currentuseruid;
    private Uri imageuri;

    public EditprofileFragment(String uid) {
        this.currentuseruid=uid;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_editprofile, container, false);
        profilephoto=view.findViewById(R.id.editprofile_profileImage);
        eye1=view.findViewById(R.id.editprofile_curentpasswordeye);
        eye2=view.findViewById(R.id.editprofile_newpasswordeye);
        selectimage=view.findViewById(R.id.editprofile_btnSelectImage);
        Bio=view.findViewById(R.id.editprofile_bioedittext);
        currentpassword=view.findViewById(R.id.editprofile_currentpasswordedittext);
        newpassword=view.findViewById(R.id.editprofile_newpasswordedittext);
        save=view.findViewById(R.id.editprofile_savebutton);

        profilephoto.setOnClickListener(v-> loadimage());
        selectimage.setOnClickListener(v-> loadimage());

        save.setOnClickListener(v-> {
            uploaduserdata();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(getActivity(), config);
        } catch (Exception e) {
            Log.d("Cloudinary", "Already initialized");
        }

        eye1.setOnClickListener(e->{
            android.graphics.Typeface typeface = currentpassword.getTypeface();
            if(currentpassword.getInputType()==(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)){
                currentpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eye1.setImageResource(R.drawable.visibility_off_24px);
            }else {
                currentpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eye1.setImageResource(R.drawable.visibility_24px);
            }
            currentpassword.setTypeface(typeface);
            currentpassword.setSelection(currentpassword.length());
        });
        eye2.setOnClickListener(e->{
            android.graphics.Typeface typeface = newpassword.getTypeface();
            if(newpassword.getInputType()==(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)){
                newpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eye2.setImageResource(R.drawable.visibility_off_24px);
            }else {
                newpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eye2.setImageResource(R.drawable.visibility_24px);
            }
            newpassword.setTypeface(typeface);
            newpassword.setSelection(newpassword.length());
        });

        return view;
    }



    private void loadimage() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }
    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 1 && res == RESULT_OK && data != null) {
            imageuri = data.getData();
            Glide.with(this).load(imageuri).circleCrop().into(profilephoto);

        }
    }

    private void uploaduserdata() {
        String NEWBIO=Bio.getText().toString().trim();
        String NEWPASSWORD=newpassword.getText().toString().trim();
        String CURRENT_PASSWORD=currentpassword.getText().toString().trim();

        FirebaseAuth auth=FirebaseAuth.getInstance();

        FirebaseFirestore.getInstance().collection("users").document(currentuseruid).update("bio", NEWBIO);

        if (imageuri != null) {
            MediaManager.get().upload(imageuri).callback(new UploadCallback() {
                @Override public void onStart(String requestId) {}
                @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                @Override public void onReschedule(String requestId, ErrorInfo error) {}
                @Override public void onError(String requestId, ErrorInfo error) {}

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String imageurl = resultData.get("secure_url").toString();
                    FirebaseFirestore.getInstance().collection("users").document(currentuseruid).update("profileImageUrl", imageurl);
                }
            }).dispatch();
        }

        if (!NEWPASSWORD.isEmpty()) {
            if (NEWPASSWORD.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (CURRENT_PASSWORD.isEmpty()) {
                Toast.makeText(getContext(), "Enter current password to change password", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentEmail = auth.getCurrentUser().getEmail();
            auth.getCurrentUser().reauthenticate(
                    EmailAuthProvider.getCredential(currentEmail, CURRENT_PASSWORD)
            ).addOnSuccessListener(task -> {
                auth.getCurrentUser().updatePassword(NEWPASSWORD)
                        .addOnSuccessListener(v ->
                                Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }




}
