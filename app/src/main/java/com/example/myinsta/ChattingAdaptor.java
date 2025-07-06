package com.example.myinsta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChattingAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<Messagemodel> messagelist;
    String currentuserUid;
    private int left_msg=1;
    private int right_msg=2;
    public ChattingAdaptor(ChattingActivity chattingActivity, ArrayList<Messagemodel> messagelist, String currentuserUid) {
        context=chattingActivity;
        this.messagelist=messagelist;
        this.currentuserUid=currentuserUid;

    }


    @Override
    public int getItemViewType(int position){
        if(currentuserUid.equals(messagelist.get(position).getSender())){
            return right_msg;
        }else {
            return left_msg;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==right_msg){
            return new rightviewHolder(LayoutInflater.from(context).inflate(R.layout.rightmessage,parent,false));
        }else {
            return new leftviewHolder(LayoutInflater.from(context).inflate(R.layout.leftmessage,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof rightviewHolder){
            ((rightviewHolder)holder).textmessage.setText(messagelist.get(position).getMessage());
        }else {
            ((leftviewHolder)holder).textmessage.setText(messagelist.get(position).getMessage());
        }


    }

    @Override
    public int getItemCount() {
        return messagelist.size();
    }

    static class leftviewHolder extends RecyclerView.ViewHolder {
        TextView textmessage;
        public leftviewHolder(@NonNull View itemView) {
            super(itemView);
            textmessage=itemView.findViewById(R.id.leftmessageid);

        }
    }
    static class rightviewHolder extends RecyclerView.ViewHolder{
        TextView textmessage;

        public rightviewHolder(@NonNull View itemView) {
            super(itemView);
            textmessage= itemView.findViewById(R.id.rightmessageid);
        }
    }
}
