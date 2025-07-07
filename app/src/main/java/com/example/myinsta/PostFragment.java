package com.example.myinsta;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.VirtualLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.Value;

import java.util.HashMap;
import java.util.Map;


public class PostFragment extends Fragment {

AppCompatButton uploadimage;
ImageButton preview;
EditText desc;

private Uri imageuri;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_post, container, false);
        uploadimage=view.findViewById(R.id.post_uploadbutton);
        preview=view.findViewById(R.id.post_preview);
        desc=view.findViewById(R.id.post_description);

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(getActivity(), config);
        } catch (Exception e) {
            Log.d("Cloudinary", "Already initialized");
        }

        preview.setOnClickListener(s-> loadimage());
        uploadimage.setOnClickListener(s->{
            uploadpost();
        });
        return view;
    }



    private void loadimage() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,1);
    }
    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data){
        super.onActivityResult(req,res,data);
        if(req==1 && res== RESULT_OK && data != null) {
            imageuri = data.getData();
            Glide.with(getActivity()).load(imageuri).transform(new RoundedCorners(50)).into(preview);
            preview.setBackground(null);
        }
    }
    private void uploadpost() {

        uploadimage.setEnabled(false);
        uploadimage.setBackgroundResource(R.drawable.edittext_background);
        uploadimage.setTextColor(Color.GRAY);
        uploadimage.setText("Uploading...");
        if (imageuri == null) {
            Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
            uploadimage.setEnabled(true);
            uploadimage.setTextColor(Color.WHITE);
            uploadimage.setBackgroundResource(R.drawable.loginbutton_background);
            uploadimage.setText("Upload Post");
            return;
        }

        MediaManager.get().upload(imageuri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                uploadimage.setEnabled(true);
                uploadimage.setTextColor(Color.WHITE);
                uploadimage.setBackgroundResource(R.drawable.loginbutton_background);
                uploadimage.setText("Upload Post");

                String currentuser=FirebaseAuth.getInstance().getCurrentUser().getUid();
                String imageurl=resultData.get("secure_url").toString();
                Postmodel post=new Postmodel(currentuser,imageurl,desc.getText().toString(),com.google.firebase.Timestamp.now());

                FirebaseFirestore.getInstance().collection("posts").document(currentuser).collection("images").add(post).addOnSuccessListener(p->{
                    Map<String ,Object> likes=new HashMap<>();
                    likes.put("likes at", FieldValue.serverTimestamp());
                    FirebaseFirestore.getInstance().collection("posts").document(currentuser).collection("images").document(p.getId()).collection("likes").document("metadata").set(likes);

                    post.setPostid(p.getId());

                    FirebaseFirestore.getInstance().collection("posts").document(currentuser).collection("images").document(p.getId()).update("postid", p.getId());

                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                    ((MainActivity)getActivity()).navigatetoprofile();

                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {

            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        }).dispatch();

    }
}
