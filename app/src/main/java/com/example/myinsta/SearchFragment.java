package com.example.myinsta;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    EditText search;
    RecyclerView userview;
    List<User> userlist=new ArrayList<>();
    List<User> filteredlist=new ArrayList<>();

    searchlistAdaptor searchlistAdaptor;
    TextView noprofile,allusers;




    @SuppressLint({"ResourceAsColor", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_search, container, false);



        search=view.findViewById(R.id.search_searchedittext);
        userview=view.findViewById(R.id.search_recyclerview);
        noprofile=view.findViewById(R.id.noprofile);
        allusers=view.findViewById(R.id.allusers);

        FirebaseFirestore.getInstance().collection("users").get(Source.DEFAULT).addOnSuccessListener(v->{
            userlist.clear();
            for(DocumentSnapshot snap:v){
                User user=snap.toObject(User.class);

                if(!user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    userlist.add(user);
                }
            }
            filteredlist.clear();
            filteredlist.addAll(userlist);
            searchlistAdaptor.notifyDataSetChanged();
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filteredlist.clear();
                allusers.setVisibility(view.GONE);
                noprofile.setVisibility(view.GONE);
                if(charSequence.toString().isEmpty()){
                    filteredlist.addAll(userlist);
                    allusers.setVisibility(view.VISIBLE);
                }else{
                    for(User users:userlist){
                        if(users.getUsername().toLowerCase().contains(charSequence.toString().toLowerCase())){
                            filteredlist.add(users);

                        }
                    }
                    if(filteredlist.isEmpty() && filteredlist.size()==0){
                        noprofile.setVisibility(view.VISIBLE);
                    }
                }

                searchlistAdaptor.notifyDataSetChanged();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchlistAdaptor=new searchlistAdaptor(getContext(),filteredlist);
        userview.setAdapter(searchlistAdaptor);
        userview.setLayoutManager(new LinearLayoutManager(getContext()));









        return view;
    }
}