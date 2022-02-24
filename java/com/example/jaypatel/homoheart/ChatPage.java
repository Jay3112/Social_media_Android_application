package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatPage extends AppCompatActivity {

    String logMail,userMail,userProfUrl,userName;

    ArrayList<String> sender_mail;
    ArrayList<String> receiver_mail;
    ArrayList<String> sentMessage;
    ArrayList<Long> time;

    ListView myMessageList;
    TextView profUserName;
    ImageView profImage;
    LinearLayout profileLayout;
    MessageListAdapter adapter;

    ImageView sendBtn;
    EditText messageToSend;
    FirebaseFirestore db;

    Chk_Network checkNetwork;
    ProgressDialog mProgressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_page);
        getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        logMail = prefs.getString("logMail", null);

        final Intent intent = getIntent();
        userMail = intent.getStringExtra("userMail");
        userName = intent.getStringExtra("userName");
        userProfUrl = intent.getStringExtra("userProfImage");

        sendBtn = (ImageView)findViewById(R.id.sendmessage);
        messageToSend = (EditText)findViewById(R.id.messageEdit);
        myMessageList = (ListView)findViewById(R.id.messages);
        profUserName = (TextView)findViewById(R.id.userName);
        profImage = (ImageView)findViewById(R.id.userProf);
        profileLayout = (LinearLayout)findViewById(R.id.profileLayout);

        profUserName.setText(userName);
        Picasso.get().load(userProfUrl).into(profImage);

        db = FirebaseFirestore.getInstance();
        checkNetwork = new Chk_Network();
        mProgressDialogue = new ProgressDialog(ChatPage.this);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkNetwork.isNetworkAvailable(ChatPage.this)){
                    if(messageToSend.getEditableText().toString().isEmpty()){
                        messageToSend.setError("Write something");
                    }else{
                        sendMessage();
                    }
                }else{
                    Toast.makeText(ChatPage.this,"Turn on internet",Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatPage.this, OtherUserProfile.class);
                intent.putExtra("userMail",userMail);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onResume() {
        db.collection("messages")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    sender_mail = new ArrayList<String>();
                    receiver_mail = new ArrayList<String>();
                    sentMessage = new ArrayList<String>();
                    time = new ArrayList<Long>();
                    DispMessages();
                }
            });
        sender_mail = new ArrayList<String>();
        receiver_mail = new ArrayList<String>();
        sentMessage = new ArrayList<String>();
        time = new ArrayList<Long>();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public class MessageListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        Context context;
        List<String> sender_mail;
        List<String> receiver_mail;
        List<String> sentMessage;
        List<Long> messageTime;

        public MessageListAdapter(Context context, List<String> senderMail,List<String> receiverMail,List<String> message,List<Long> time) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.sender_mail = senderMail;
            this.receiver_mail = receiverMail;
            this.messageTime = time;
            this.sentMessage = message;
        }

        private class ViewHolder {
            TextView messageTv, messageTime;
            LinearLayout leftOrRight, spaceLayout, spaceLayout2;
        }

        @Override
        public int getCount() {
            return sender_mail.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View view, ViewGroup parent) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custlist_chat, null, true);

            final MessageListAdapter.ViewHolder viewHolder = new MessageListAdapter.ViewHolder();

            viewHolder.messageTv = (TextView)rowView.findViewById(R.id.msgtv);
            viewHolder.messageTime = (TextView)rowView.findViewById(R.id.timetv);
            viewHolder.leftOrRight = (LinearLayout)rowView.findViewById(R.id.layoutGravitySet);
            viewHolder.spaceLayout = (LinearLayout)rowView.findViewById(R.id.space);
            viewHolder.spaceLayout2 = (LinearLayout)rowView.findViewById(R.id.space2);
            String dateString = new SimpleDateFormat("dd/MM/yyyy  hh:mm").format(new Date(messageTime.get(position)));

            if(sender_mail.get(position).matches(logMail)){
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.RIGHT;
                viewHolder.leftOrRight.setLayoutParams(params);
                viewHolder.messageTv.setLayoutParams(params);
                viewHolder.spaceLayout.setVisibility(View.GONE);
            }else{
                viewHolder.spaceLayout2.setVisibility(View.GONE);
            }

            viewHolder.messageTv.setText(sentMessage.get(position));
            viewHolder.messageTime.setText(dateString);

            return rowView;
        }
    }

    public void sendMessage(){
        Map<String, Object> record = new HashMap<>();
        record.put("sender_mail", logMail);
        record.put("receiver_mail", userMail);
        record.put("message", messageToSend.getEditableText().toString());
        record.put("time", new Date().getTime());

        db.collection("messages")
                .add(record)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        messageToSend.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatPage.this, "Unable to send", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void DispMessages(){
        db.collection("messages").orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if((document.getString("sender_mail").matches(logMail) && document.getString("receiver_mail").matches(userMail)) ||
                                        (document.getString("sender_mail").matches(userMail) && document.getString("receiver_mail").matches(logMail))){

                                    sender_mail.add(document.getString("sender_mail"));
                                    receiver_mail.add(document.getString("receiver_mail"));
                                    sentMessage.add(document.getString("message"));
                                    time.add(document.getLong("time"));
                                }
                            }
                            adapter = new MessageListAdapter(ChatPage.this,sender_mail,receiver_mail,sentMessage,time);
                            myMessageList.setAdapter(adapter);
                            ListScrollHelper.getListViewSize(myMessageList);
                            myMessageList.setSelection(adapter.getCount()-1);
                        } else {
                            Toast.makeText(ChatPage.this, "Unable to get messages", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
