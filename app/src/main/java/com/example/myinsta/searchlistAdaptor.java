package com.example.myinsta;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class searchlistAdaptor extends RecyclerView.Adapter<searchlistAdaptor.ViewHolder> {
    Context search;
    List<User> filteredlist;
    public searchlistAdaptor(Context search, List<User> filteredlist) {
        this.search=search;
        this.filteredlist=filteredlist;

    }

    @NonNull
    @Override
    public searchlistAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(search).inflate(R.layout.searchlist_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull searchlistAdaptor.ViewHolder holder, int position) {
        holder.name.setText(filteredlist.get(position).getUsername().toString());
        if(!filteredlist.get(position).getProfileImageUrl().isEmpty()){
            Glide.with(search).load(filteredlist.get(position).getProfileImageUrl()).placeholder(R.drawable.edittext_background).circleCrop().into(holder.image);
        }
        holder.itemView.setOnClickListener(v->{
            FragmentActivity activity=(FragmentActivity) v.getContext();
            Fragment childfragment=new childprofileFragment(filteredlist.get(position).getUid());


            activity.getSupportFragmentManager().beginTransaction().add(R.id.framecontainer, childfragment).addToBackStack(null).commit();



        });
        String currentuserUid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(currentuserUid).collection("following").document(filteredlist.get(position).getUid()).get(Source.DEFAULT.SERVER).addOnSuccessListener(v->{
            final boolean[] following = {v.exists()};
            if(following[0]){
                holder.follow.setText("Unfollow");
                holder.follow.setBackgroundResource(R.drawable.unfollow_buttonbg);
                holder.follow.setTextColor(Color.GRAY);
                holder.follow.setVisibility(View.VISIBLE);
                holder.sendmessage.setVisibility(View.VISIBLE);
            }
            else {
                holder.follow.setText("Follow");
                holder.follow.setBackgroundResource(R.drawable.loginbutton_background);
                holder.follow.setTextColor(Color.WHITE);
                holder.follow.setVisibility(View.VISIBLE);
                holder.sendmessage.setVisibility(View.GONE);

            }
           holder.follow.setOnClickListener(s->{
               holder.follow.setEnabled(false);
               if(following[0]){
                   FirebaseFirestore.getInstance().collection("users").document(currentuserUid).collection("following").document(filteredlist.get(position).getUid()).delete();
                   FirebaseFirestore.getInstance().collection("users").document(filteredlist.get(position).getUid()).collection("followers").document(currentuserUid).delete();
                   following[0] =false;
                   holder.follow.setEnabled(true);
                   notifyItemChanged(position);
               }
               else {
                   Map<String, Object> data = new HashMap<>();
                   data.put("followed at",com.google.firebase.Timestamp.now());
                   FirebaseFirestore.getInstance().collection("users").document(currentuserUid).collection("following").document(filteredlist.get(position).getUid()).set(data);
                   FirebaseFirestore.getInstance().collection("users").document(filteredlist.get(position).getUid()).collection("followers").document(currentuserUid).set(data).addOnSuccessListener(u -> {
                       following[0]=true;
                       holder.follow.setEnabled(true);
                       notifyItemChanged(position);
                   });
               }
           });

            holder.sendmessage.setOnClickListener(i->{
                Intent intent=new Intent(i.getContext(),ChattingActivity.class);
                intent.putExtra("childUid",filteredlist.get(position).getUid());
                i.getContext().startActivity(intent);
            });
        });

    }

    @Override
    public int getItemCount() {
        return filteredlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CircleImageView image;
        AppCompatButton follow;
        ImageButton sendmessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.ref_searchname);
            image=itemView.findViewById(R.id.ref_serachprofile);
            follow=itemView.findViewById(R.id.search_followbutton);
            sendmessage=itemView.findViewById(R.id.search_sendmessageicon);

        }
    }
}
