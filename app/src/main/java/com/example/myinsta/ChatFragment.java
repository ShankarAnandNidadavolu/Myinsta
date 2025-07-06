package com.example.myinsta;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private List<chatlistmodel> chatlist = new ArrayList<>();
    private List<chatlistmodel> filteredchatlist = new ArrayList<>();
    private RecyclerView chatsrecyclerview;
    private chatlistAdaptor chatlistadaptor;
    private EditText searchbar;
    private Button newchat;
    private String currentuser;
    private Map<String, String> userNameCache=new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat, container, false);

        chatsrecyclerview=view.findViewById(R.id.chats_recyclerview);
        searchbar=view.findViewById(R.id.search_chatssearchedittext);
        newchat=view.findViewById(R.id.chat_newchatbutton);

        currentuser= FirebaseAuth.getInstance().getCurrentUser().getUid();


        chatlistadaptor=new chatlistAdaptor(getContext(), filteredchatlist, currentuser);
        chatsrecyclerview.setAdapter(chatlistadaptor);
        chatsrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));


        fetchChats();


        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterChats(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        newchat.setOnClickListener(v -> {
            ((MainActivity) getActivity()).navigateToSearch();
        });

        return view;
    }

    private void fetchChats() {
        FirebaseFirestore.getInstance().collection("chats").whereArrayContains("participants", currentuser).addSnapshotListener((value, error) -> {
            if (error!=null) {
                Log.e("Firestore", "Error fetching chats", error);
                return;
            }

            if (value!=null) {
                chatlist.clear();
                filteredchatlist.clear();

                for (DocumentSnapshot snap: value.getDocuments()) {
                    chatlistmodel chat=snap.toObject(chatlistmodel.class);

                    if (chat!=null) {
                        chatlist.add(chat);
                    }
                }

                Collections.sort(chatlist, new Comparator<chatlistmodel>() {
                    @Override
                    public int compare(chatlistmodel chat1, chatlistmodel chat2) {
                        if (chat1.getlastTimestamp()==null && chat2.getlastTimestamp()==null) {
                            return 0;
                        }
                        if (chat1.getlastTimestamp()==null) {
                            return 1;
                        }
                        if (chat2.getlastTimestamp()==null) {
                            return -1;
                        }
                        return chat2.getlastTimestamp().compareTo(chat1.getlastTimestamp());
                    }
                });
                filteredchatlist.addAll(chatlist);
                prefetchUsernames();
                chatlistadaptor.notifyDataSetChanged();
            }
        });
    }

    private void prefetchUsernames() {
        for (chatlistmodel chat : chatlist) {
            List<String> participants=chat.getParticipants();
            for (String uid:participants) {
                if (!uid.equals(currentuser) && !userNameCache.containsKey(uid)) {
                    FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
                        String username=snapshot.getString("username");
                        if (username!=null) {
                            userNameCache.put(uid,username);
                        }
                    });
                }
            }
        }
    }

    private void filterChats(String searchText) {
        filteredchatlist.clear();
        String lowerCaseSearch=searchText.toLowerCase();

        if (!searchText.isEmpty()) {
            for (chatlistmodel chat:chatlist) {
                List<String> participants=chat.getParticipants();
                String oppositeUid=null;

                for (String uid:participants) {
                    if (!uid.equals(currentuser)) {
                        oppositeUid=uid;
                        break;
                    }
                }

                if (oppositeUid!= null) {
                    String username=userNameCache.get(oppositeUid);
                    if (username!=null && username.toLowerCase().contains(lowerCaseSearch)) {
                        filteredchatlist.add(chat);
                    }
                }
            }
        } else {
            filteredchatlist.addAll(chatlist);
        }

        chatlistadaptor.notifyDataSetChanged();
    }
}
