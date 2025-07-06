package com.example.myinsta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class Postlistadaptor extends RecyclerView.Adapter<Postlistadaptor.viewholder> {
    Context context;
    List<String> postlinks;
    public Postlistadaptor(Context context, List<String> postlinks) {
        this.context=context;
        this.postlinks=postlinks;
    }

    @NonNull
    @Override
    public Postlistadaptor.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewholder(LayoutInflater.from(context).inflate(R.layout.postlist_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Postlistadaptor.viewholder holder, int position) {
        String postlink=postlinks.get(position);
        Glide.with(context).load(postlink).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return postlinks.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ImageView image;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.ref_postview);
        }
    }
}
