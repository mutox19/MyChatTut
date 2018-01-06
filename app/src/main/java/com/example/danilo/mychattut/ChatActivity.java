package com.example.danilo.mychattut;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName;
    private android.support.v7.widget.Toolbar chatToolbar;
    private TextView userNameTitle, userLastSeen;
    private CircleImageView userChatProfileImage;
    private DatabaseReference rootRef;
    private ImageButton sendMessageBtn, selectImageButton;
    private EditText inputMessage;
    private FirebaseAuth mAuth;
    private String messageSenderID;
    private RecyclerView userMessagesList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        chatToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        rootRef = FirebaseDatabase.getInstance().getReference();
        userNameTitle = (TextView)findViewById(R.id.custom_displayUsername);
        userLastSeen = (TextView)findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = (CircleImageView)findViewById(R.id.custom_profile_image);
        sendMessageBtn = (ImageButton)findViewById(R.id.sendMessage);
        selectImageButton = (ImageButton)findViewById(R.id.selectImage);
        inputMessage = (EditText)findViewById(R.id.inputMessage);
        messageAdapter = new MessageAdapter(messageList);
        userMessagesList = (RecyclerView)findViewById(R.id.messageListOfUsers);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        FetchMessages();

        userNameTitle.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String user_thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(userChatProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                        //will load images offline
                    }

                    @Override
                    public void onError() {
                        //to load images only when online
                        Picasso.with(ChatActivity.this).load(user_thumb_image).placeholder(R.drawable.default_profile).into(userChatProfileImage);
                    }
                });

                if(online.equals("true"))
                {
                    userLastSeen.setText("online");
                }
                else
                {
                    LastSeenTime getTime = new LastSeenTime();
                    long lastSeen = Long.parseLong(online);
                    String lastSeenDisplayTime = getTime.getTimeAgo(lastSeen,getApplicationContext()).toString();
                    userLastSeen.setText(lastSeenDisplayTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              SendMessage();
            }
        });
    }

    private void FetchMessages()
    {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messageList.add(messages);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage()
    {
        String messageText = inputMessage.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this,"Please Write your message",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            //create a key for the message
            DatabaseReference user_MessageKey = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            //get the users message key
            String message_Push_ID = user_MessageKey.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + message_Push_ID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + message_Push_ID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if(databaseError != null)
                    {
                        Log.d("CHATLOG", "onComplete: " + databaseError.getMessage().toString() );
                    }
                    inputMessage.setText(null);
                }
            });
        }
    }
}
