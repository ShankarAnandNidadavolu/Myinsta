package com.example.myinsta;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class childprofileFragment extends Fragment {
    private TextView pusername, pbio;
    private CircleImageView profilephoto;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String childuid;
    private Button follow, message;
    private TextView postcountview, followerscountview, followingcountview;
    private RecyclerView postRecyclerview;
    private TextView nopostsavailable;

    public childprofileFragment(String uid) {
        this.childuid = uid;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_childprofile, container, false);

        pusername=view.findViewById(R.id.childprofileusername);
        pbio=view.findViewById(R.id.childprofileBio);
        profilephoto=view.findViewById(R.id.childprofilephoto);
        follow=view.findViewById(R.id.childfollowbutton);
        message=view.findViewById(R.id.childmessagebutton);
        postRecyclerview=view.findViewById(R.id.childprofile_postsrecyclerview);
        nopostsavailable=view.findViewById(R.id.nopostsavailable);

        postcountview=view.findViewById(R.id.childprofile_postcount);
        followerscountview=view.findViewById(R.id.childprofile_followerscount);
        followingcountview=view.findViewById(R.id.childprofile_followingcount);

        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        String parentuid = auth.getCurrentUser().getUid();

        firestore.collection("users").document(childuid).get().addOnSuccessListener(v -> {
            String username=v.getString("username");
            String bio=v.getString("bio");
            String profileurl=v.getString("profileImageUrl");
            pusername.setText(username != null ? username : "No Name");
            pbio.setText(bio != null ? bio : "");
            if (isAdded() && getContext() != null) {
                Glide.with(this).load(profileurl).placeholder(R.drawable.ic_default_profile).into(profilephoto);
            }
        });

        loadFollowCounts();

        follow.setVisibility(View.INVISIBLE);

        firestore.collection("users").document(parentuid).collection("following").document(childuid).get().addOnSuccessListener(v -> {
            boolean[] isFollowing = {v.exists()};
            updateFollowButton(isFollowing[0]);

            follow.setVisibility(View.VISIBLE);
            message.setVisibility(isFollowing[0] ? View.VISIBLE : View.GONE);

            follow.setOnClickListener(e -> {
                follow.setEnabled(false);
                if (isFollowing[0]) {
                    firestore.collection("users").document(parentuid).collection("following").document(childuid).delete();
                    firestore.collection("users").document(childuid).collection("followers").document(parentuid).delete().addOnSuccessListener(u -> {
                        isFollowing[0] = false;
                        updateFollowButton(false);
                        message.setVisibility(View.GONE);
                        loadFollowCounts();
                        follow.setEnabled(true);
                    });
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("followedAt", Timestamp.now());
                    firestore.collection("users").document(parentuid).collection("following").document(childuid).set(data);
                    firestore.collection("users").document(childuid).collection("followers").document(parentuid).set(data).addOnSuccessListener(u -> {
                        isFollowing[0] = true;
                        updateFollowButton(true);
                        message.setVisibility(View.VISIBLE);
                        loadFollowCounts();
                        follow.setEnabled(true);
                    });
                }
            });
        });

        message.setOnClickListener(c -> {
            Intent intent = new Intent(getActivity(), ChattingActivity.class);
            intent.putExtra("childUid", childuid);
            startActivity(intent);
        });

        List<String> postLinks = new ArrayList<>();
        Postlistadaptor adaptor = new Postlistadaptor(getContext(), postLinks);
        postRecyclerview.setAdapter(adaptor);
        postRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 3));

        firestore.collection("posts").document(childuid).collection("images").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(s -> {
            for (DocumentSnapshot d : s.getDocuments()) {
                String link = d.getString("posturl");
                if (link != null) {
                    postLinks.add(link);
                }
            }
            if (!postLinks.isEmpty()) {
                postRecyclerview.setVisibility(View.VISIBLE);
                nopostsavailable.setVisibility(View.GONE);
            } else {
                postRecyclerview.setVisibility(View.GONE);
                nopostsavailable.setVisibility(View.VISIBLE);
            }
            adaptor.notifyDataSetChanged();
        });

        return view;
    }

    private void loadFollowCounts() {
        firestore.collection("posts").document(childuid).collection("images").get().addOnSuccessListener(v -> {
            postcountview.setText(String.valueOf(v.size()));
        });
        firestore.collection("users").document(childuid).collection("followers").get().addOnSuccessListener(v -> {
            followerscountview.setText(String.valueOf(v.size()-1));
        });
        firestore.collection("users").document(childuid).collection("following").get().addOnSuccessListener(v -> {
            followingcountview.setText(String.valueOf(v.size()-1));
        });
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            follow.setText("Unfollow");
            follow.setTextColor(Color.GRAY);
            follow.setBackgroundResource(R.drawable.unfollow_buttonbg);
        } else {
            follow.setText("Follow");
            follow.setTextColor(Color.WHITE);
            follow.setBackgroundResource(R.drawable.loginbutton_background);
        }
    }
}
