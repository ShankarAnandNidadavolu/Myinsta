package com.example.myinsta;

import static android.app.PendingIntent.getActivity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatlistAdaptor extends RecyclerView.Adapter<chatlistAdaptor.Viewholder> {
    Context context;
    List<chatlistmodel> filteredchatlist;
    String currentuser;
    public chatlistAdaptor(Context context, List<chatlistmodel> filteredchatlist, String currentuser) {
        this.context=context;
        this.filteredchatlist=filteredchatlist;
        this.currentuser=currentuser;
    }

    @NonNull
    @Override
    public chatlistAdaptor.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(context).inflate(R.layout.chatlist_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull chatlistAdaptor.Viewholder holder, int position) {
        String oppositUid=null;
        List<String> participants=filteredchatlist.get(position).getParticipants();

        for (String user:participants) {
            if (!currentuser.equals(user)) {
                oppositUid=user;
                break;
            }
        }

        if (oppositUid==null || oppositUid.isEmpty()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        com.google.firebase.Timestamp lastseen = filteredchatlist.get(position).getlastTimestamp();

        FirebaseFirestore.getInstance().collection("users").document(oppositUid).get(Source.SERVER).addOnSuccessListener(v -> {
            String name = v.getString("username");
            String profile = v.getString("profileImageUrl");

            holder.username.setText(name);

            Glide.with(context).load(profile).placeholder(R.drawable.edittext_background).circleCrop().into(holder.profilephoto);

            if (lastseen != null) {
                Date date = lastseen.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault());
                String newlastseen = sdf.format(date);
                holder.lastseen.setText("Last message: " + newlastseen);
            } else {
                holder.lastseen.setText("No messages yet");
            }
        }).addOnFailureListener(e -> {
            holder.username.setText("Unknown User");
            holder.lastseen.setText("Error loading user");
        });

        String finalOppositUid = oppositUid;
        holder.itemView.setOnClickListener(n -> {
            Intent intent = new Intent(context, ChattingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("childUid", finalOppositUid);
            context.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        return filteredchatlist.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView username,lastseen;
        CircleImageView profilephoto;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.ref_chatname);
            profilephoto=itemView.findViewById(R.id.ref_chatprofile);
            lastseen=itemView.findViewById(R.id.ref_chatlastseen);
        }
    }
}
