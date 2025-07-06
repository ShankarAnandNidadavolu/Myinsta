package com.example.myinsta;

import android.annotation.SuppressLint;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    ImageButton chat;
    RecyclerView feedrecyclerview;
    TextView loadingText,nofollowing;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_home, container, false);
        chat=view.findViewById(R.id.home_chaticon);
        feedrecyclerview=view.findViewById(R.id.home_recyclerview);
        loadingText=view.findViewById(R.id.loading_text);
        nofollowing=view.findViewById(R.id.nofollowing_text);

        loadingText.setVisibility(View.VISIBLE);
        nofollowing.setVisibility(View.GONE);
        feedrecyclerview.setVisibility(View.GONE);

        chat.setOnClickListener(c->{
            ((MainActivity)getActivity()).navigatetochat();
        });

        String currentuseruid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String > followinglist=new ArrayList<>();
        List<Postmodel> homeposts=new ArrayList<>();

        homefeed_adaptor feedadaptor=new homefeed_adaptor(getContext(),homeposts);


        FirebaseFirestore.getInstance().collection("users").document(currentuseruid).collection("following").get().addOnSuccessListener(f->{
           for(DocumentSnapshot d:f.getDocuments()){
               if(!d.getId().equals("metadata")){
                   followinglist.add(d.getId());
               }
           }
            if (followinglist.isEmpty()) {
                loadingText.setVisibility(View.GONE);
                nofollowing.setVisibility(View.VISIBLE);
                return;
            }
            final int totalUsers = followinglist.size();
            final int[] completed = {0};


            for(String user:followinglist){
                FirebaseFirestore.getInstance().collection("posts").document(user).collection("images").get().addOnSuccessListener(p->{
                    for(DocumentSnapshot post:p.getDocuments()){
                        String posturl=post.getString("posturl");
                        String desc=post.getString("post_desc");
                        com.google.firebase.Timestamp timestamp=post.getTimestamp("timestamp");
                        String senderuid=post.getString("userUid");
                        String postid=post.getString("postid");

                        Postmodel temppost=new Postmodel();
                        temppost.setPosturl(posturl);
                        temppost.setPost_desc(desc);
                        temppost.setTimestamp(timestamp);
                        temppost.setUserUid(senderuid);
                        temppost.setPostid(postid);

                        homeposts.add(temppost);

                    }
                    completed[0]++;
                    if (completed[0] == totalUsers){
                        java.util.Collections.shuffle(homeposts);
                        feedadaptor.notifyDataSetChanged();

                        loadingText.setVisibility(View.GONE);
                        feedrecyclerview.setVisibility(View.VISIBLE);
                    }

                });
            }

        });


        feedrecyclerview.setAdapter(feedadaptor);
        feedrecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        feedadaptor.notifyDataSetChanged();



        return view;
    }
}