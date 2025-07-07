package com.example.myinsta;
import com.example.myinsta.MainActivity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingActivity extends AppCompatActivity {
    String childUid;
    CircleImageView childprofilephoto;
    TextView childusername;
    EditText messagebox;
    ImageButton sendbutton;
    RecyclerView chatrecyclerview;
    LinearLayout profile;

    public static boolean isChatting = false;
    public static String activeChatUid = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        childUid=getIntent().getStringExtra("childUid");

        if (childUid==null) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_chatting);

        childprofilephoto= (CircleImageView) findViewById(R.id.chattingprofilephoto);
        childusername= (TextView) findViewById(R.id.chattingusername);
        messagebox= (EditText) findViewById(R.id.chattingmessagebox);
        sendbutton= (ImageButton) findViewById(R.id.chattingsendbutton);
        chatrecyclerview= (RecyclerView) findViewById(R.id.chattingrecyclerview);
        profile= (android.widget.LinearLayout) findViewById(R.id.chatting_profile);

        childUid= getIntent().getStringExtra("childUid");

        FirebaseFirestore.getInstance().collection("users").document(childUid).get(Source.DEFAULT).addOnSuccessListener(v->{
           String username=v.getString("username");
           String profileurl=v.getString("profileImageUrl");

           childusername.setText(username);
            Glide.with(this).load(profileurl).into(childprofilephoto);
        });
        profile.setOnClickListener(p->{
            Intent i=new Intent(this,MainActivity.class);
            i.putExtra("childuid",childUid);
            i.putExtra("openicon","chats");
            i.putExtra("openfragment","childfragment");
            startActivity(i);
            finish();

        });

        loadChatFromIntent();



    }
    private void loadChatFromIntent() {
        FirebaseAuth auth=FirebaseAuth.getInstance();

        String currentuserUid=auth.getCurrentUser().getUid();

        String chatid=currentuserUid.compareTo(childUid)<0? currentuserUid+"_"+childUid : childUid+"_"+currentuserUid;
        chatlistmodel chatdata=new chatlistmodel();
        chatdata.setParticipants(Arrays.asList(currentuserUid,childUid));


        FirebaseFirestore.getInstance().collection("users").document(currentuserUid).get().addOnSuccessListener(userSnapshot -> {
            String currentUsername = userSnapshot.getString("username");

            sendbutton.setOnClickListener(s -> {
                String text = messagebox.getText().toString();
                if (!text.isEmpty()) {
                    messagebox.setText("");

                    Messagemodel message = new Messagemodel(currentuserUid, text, com.google.firebase.Timestamp.now());

                    FirebaseFirestore.getInstance().collection("chats").document(chatid).collection("messages").add(message).addOnSuccessListener(t -> {

                        chatdata.setlastmessage(message.getMessage());
                        chatdata.setLasttimetimestamp(com.google.firebase.Timestamp.now());

                        FirebaseFirestore.getInstance().collection("chats").document(chatid).set(chatdata, SetOptions.merge());
                    });

                    FirebaseFirestore.getInstance().collection("users").document(childUid).get().addOnSuccessListener(snapshot -> {
                        String token = snapshot.getString("fcmToken");
                        sendNotification(token, currentUsername, text, currentuserUid);

                    });
                }
            });
        });





        ArrayList<Messagemodel> messagelist=new ArrayList<>();
        ChattingAdaptor chatadapt=new ChattingAdaptor(this,messagelist,currentuserUid);

        FirebaseFirestore.getInstance().collection("chats").document(chatid).collection("messages").orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(((value, error) -> {
            if (value != null && !value.isEmpty()) {
                messagelist.clear();
                for(DocumentSnapshot snap:value.getDocuments()){
                    Messagemodel msg=snap.toObject(Messagemodel.class);
                    messagelist.add(msg);
                };
                chatadapt.notifyDataSetChanged();
                chatrecyclerview.scrollToPosition((messagelist.size())-1);

            }
        }));

        LinearLayoutManager layout=new LinearLayoutManager(this);
        layout.setStackFromEnd(true);

        chatrecyclerview.setAdapter(chatadapt);
        chatrecyclerview.setLayoutManager(layout);
    }


    public void sendNotification(String token, String title, String message, String childUid) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
            json.put("title", title);
            json.put("body", message);
            json.put("childUid", childUid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FCM_SEND", "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("FCM_SEND", "Response: " + response.body().string());
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        childUid=intent.getStringExtra("childUid");
        loadChatFromIntent();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent=new Intent(this, MainActivity.class);
        intent.putExtra("openicon","chats");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        isChatting=true;
        activeChatUid=childUid;
    }


    @Override
    protected void onPause() {
        super.onPause();
        isChatting=false;
        activeChatUid=null;
    }












}
