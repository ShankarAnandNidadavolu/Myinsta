package com.example.myinsta;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.measurement.AppMeasurement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class homefeed_adaptor extends RecyclerView.Adapter<homefeed_adaptor.viewholder> {
    Context context;
    List<Postmodel> homeposts;
    public homefeed_adaptor(Context context, List<Postmodel> homeposts) {
        this.context=context;
        this.homeposts=homeposts;
    }

    @NonNull
    @Override
    public homefeed_adaptor.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewholder(LayoutInflater.from(context).inflate(R.layout.homefeed_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull homefeed_adaptor.viewholder holder, int position) {
        String useruid=homeposts.get(position).getUserUid();
        String posturl=homeposts.get(position).getPosturl();
        String postdesc=homeposts.get(position).getPost_desc();
        String postid=homeposts.get(position).getPostid();

        String currentuserid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(useruid).get().addOnSuccessListener(u->{
            String username=u.getString("username");
            String profilephoto=u.getString("profileImageUrl");

            holder.username.setText(username);
            holder.username2.setText("@"+username+" ");
            Glide.with(context).load(profilephoto).into(holder.profilephoto);
            holder.desc.setText(postdesc);

        });

        com.google.firebase.Timestamp timestamp=homeposts.get(position).getTimestamp();

        holder.postimage.setImageDrawable(null);
        Glide.with(context).load(posturl).dontTransform().thumbnail(0.05f).into(holder.postimage);


        holder.timestamp.setText(new SimpleDateFormat("dd/MM/yy, hh:mm a").format(timestamp.toDate()));

        holder.username.setOnClickListener(v->{
            FragmentActivity activity=(FragmentActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer, new childprofileFragment(useruid)).addToBackStack(null).commit();
        });
        holder.username2.setOnClickListener(v->{
            FragmentActivity activity=(FragmentActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer, new childprofileFragment(useruid)).addToBackStack(null).commit();
        });
        holder.profilephoto.setOnClickListener(v->{
            FragmentActivity activity=(FragmentActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer, new childprofileFragment(useruid)).addToBackStack(null).commit();
        });

        //likes

        FirebaseFirestore.getInstance().collection("posts").document(useruid).collection("images").document(postid).collection("likes").document(currentuserid).get().addOnSuccessListener(v->{
            boolean liked[]={v.exists()};
            if(liked[0]){
                holder.likebutton.setImageResource(R.drawable.like_selected);
            }else {
                holder.likebutton.setImageResource(R.drawable.like_unselected);
            }
            holder.likebutton.setOnClickListener(l->{
                if(liked[0]){
                    holder.likebutton.setImageResource(R.drawable.like_unselected);
                    FirebaseFirestore.getInstance().collection("posts").document(useruid).collection("images").document(postid).collection("likes").document(currentuserid).delete();
                    liked[0]=false;

                }else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("liked at",com.google.firebase.Timestamp.now());
                    holder.likebutton.setImageResource(R.drawable.like_selected);
                    FirebaseFirestore.getInstance().collection("posts").document(useruid).collection("images").document(postid).collection("likes").document(currentuserid).set(data);
                    liked[0]=true;
                }
                loadlikescount(useruid,postid,holder);
            });
            loadlikescount(useruid,postid,holder);
        });

        holder.postimage.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubeclick(View v) {
                holder.likebutton.performClick();
            }
        });




    }
    private void loadlikescount(String useruid,String postid,homefeed_adaptor.viewholder holder){
        FirebaseFirestore.getInstance().collection("posts").document(useruid).collection("images").document(postid).collection("likes").get().addOnSuccessListener(l->{
            int likes=0;
            for(DocumentSnapshot d:l.getDocuments()){
                likes+=1;
            }
            if((likes-1)==1){
                holder.likescount.setText(String.valueOf(likes-1)+" like");
            }else {
                holder.likescount.setText(String.valueOf(likes-1)+" likes");
            }

        });
    }

    @Override
    public int getItemCount() {
        return homeposts.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ImageView profilephoto,postimage,likebutton;
        TextView username,username2,desc,timestamp,likescount;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            profilephoto=itemView.findViewById(R.id.post_profilephoto);
            username=itemView.findViewById(R.id.post_username);
            postimage=itemView.findViewById(R.id.post_image);
            desc=itemView.findViewById(R.id.post_desc);
            username2=itemView.findViewById(R.id.post_username2);
            timestamp=itemView.findViewById(R.id.post_timestamp);
            likebutton=itemView.findViewById(R.id.post_likebutton);
            likescount=itemView.findViewById(R.id.post_likescount);
        }
    }

}
