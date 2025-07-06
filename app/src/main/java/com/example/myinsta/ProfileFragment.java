package com.example.myinsta;

import static androidx.fragment.app.FragmentManagerKt.commit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    private TextView pusername, pbio;
    private CircleImageView profilephoto;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    Button signout;
    TextView postcountview,followerscountview,followingcountview;
    RecyclerView postRecyclerview;
    TextView nopostsavailable;

    AppCompatButton editprofile;
    String uid;



    public ProfileFragment() {

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_profile, container, false);

        pusername= (TextView) view.findViewById(R.id.profileusername);
        pbio= (TextView) view.findViewById(R.id.profileBio);
        profilephoto = view.findViewById(R.id.profilephoto);
        signout=view.findViewById(R.id.profile_signoutbutton);
        postRecyclerview=view.findViewById(R.id.profile_postsrecyclerview);
        nopostsavailable=view.findViewById(R.id.nopostsavailable);
        editprofile=view.findViewById(R.id.profile_editprofile);

        postcountview=view.findViewById(R.id.profile_postcount);
        followerscountview=view.findViewById(R.id.profile_followerscount);
        followingcountview=view.findViewById(R.id.profile_followingcount);


        auth=FirebaseAuth.getInstance();
        firestore= FirebaseFirestore.getInstance();

        //profiledata
        uid=auth.getCurrentUser().getUid();
        firestore.collection("users").document(uid).get(Source.DEFAULT).addOnSuccessListener(v->{
            String username=v.getString("username");
            String bio=v.getString("bio");
            String profileurl=v.getString("profileImageUrl");
            pusername.setText(username);
            pbio.setText(bio);
            if (isAdded()) {
                Glide.with(this).load(profileurl).into(profilephoto);
            }

        });
        signout.setOnClickListener(s -> {
            FirebaseFirestore.getInstance().collection("users").document(uid).update("fcmToken", "").addOnSuccessListener(aVoid -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        });

        //post,followers,following,counts
        firestore.collection("posts").document(uid).collection("images").get(Source.DEFAULT).addOnSuccessListener(v->{
            int postcount=0;
            for(DocumentSnapshot c:v.getDocuments()){
                postcount+=1;
            }
            postcountview.setText(String.valueOf(postcount));
        });
        firestore.collection("users").document(uid).collection("followers").get(Source.DEFAULT).addOnSuccessListener(v->{
           int followerscount=0;
           for(DocumentSnapshot c:v.getDocuments()){
               followerscount+=1;
           }
           followerscountview.setText(String.valueOf(followerscount-1));
        });
        firestore.collection("users").document(uid).collection("following").get(Source.DEFAULT).addOnSuccessListener(v->{
            int followingcount=0;
            for(DocumentSnapshot c:v.getDocuments()){
                followingcount+=1;
            }
            followingcountview.setText(String.valueOf(followingcount-1));
        });

        //recyclerviewdata
        List<String> postlinks=new ArrayList<>();
        Postlistadaptor adaptor=new Postlistadaptor(getContext(),postlinks);
        postRecyclerview.setAdapter(adaptor);
        postRecyclerview.setLayoutManager(new GridLayoutManager(getContext(),3));
        firestore.collection("posts").document(uid).collection("images").orderBy("timestamp", Query.Direction.DESCENDING).get(Source.DEFAULT).addOnSuccessListener(s->{
            for(DocumentSnapshot d:s.getDocuments()){
                String link=d.getString("posturl");
                postlinks.add(link);
            }
            if(postlinks.size()!=0){
                postRecyclerview.setVisibility(view.VISIBLE);
                nopostsavailable.setVisibility(view.GONE);
            }else{
                postRecyclerview.setVisibility(view.GONE);
                nopostsavailable.setVisibility(view.VISIBLE);
            }
            adaptor.notifyDataSetChanged();
        });

        editprofile.setOnClickListener(e->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer,new EditprofileFragment(uid)).addToBackStack(null).commit();
        });




        return view;
    }

}