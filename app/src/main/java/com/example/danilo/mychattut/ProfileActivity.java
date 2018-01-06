package com.example.danilo.mychattut;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    private Button sendFriendReqBtn, declineFriendRegBtn;
    private TextView profileName, profileStatus;
    private ImageView profileImage;
    private DatabaseReference UsersRef;
    private String CURRENT_STATE;
    private DatabaseReference friendReqRef;
    private FirebaseAuth mAuth;
    //this id is for the person sending the request
    private String sender_user_id;
    private String receiver_user_id;
    private DatabaseReference friendsRef;
    private DatabaseReference notificationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //create a node for friend_request
        friendReqRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        // keep the friend request ref synced while offline
        friendReqRef.keepSynced(true);

        //create freinds ref node
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        //keep friends ref synced for offline
        friendsRef.keepSynced(true);

        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationsRef.keepSynced(true);

        //create a node for the users
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        //the current sender making the request
        sender_user_id = mAuth.getCurrentUser().getUid();

        //this id is for the person receiving the request
        Intent intent = getIntent();
        receiver_user_id = intent.getExtras().get("visit_user_id").toString();

        Log.d("TAGGEDDD", "onCreate: " + receiver_user_id);

        sendFriendReqBtn = (Button) findViewById(R.id.profileVisitSendReqBtn);
        declineFriendRegBtn = (Button) findViewById(R.id.profileVisitDeclineFriendReqBtn);

        profileName = (TextView) findViewById(R.id.profileVisitUserName);
        profileStatus = (TextView) findViewById(R.id.profileVisitUserStatus);
        profileImage = (ImageView) findViewById(R.id.profile_visit_user_image);

        //set the current state
        CURRENT_STATE = "not_friends";

        UsersRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                if (!image.equals("Default_Profile")) {
                    Picasso.with(getBaseContext()).load(image).placeholder(R.drawable.default_profile).into(profileImage);
                }

                friendReqRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(receiver_user_id))
                            {
                                String reqType = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                if (reqType.equals("sent"))
                                {
                                    CURRENT_STATE = "request_sent";
                                    sendFriendReqBtn.setText("Cancel Friend Request");

                                    //invisible the decline button
                                    declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                    //disable the decline button
                                    declineFriendRegBtn.setEnabled(false);
                                }
                                else if (reqType.equals("received"))
                                {
                                    CURRENT_STATE = "request_received";
                                    sendFriendReqBtn.setText("Accept Friend Request");
                                    //visible the decline button
                                    declineFriendRegBtn.setVisibility(View.VISIBLE);
                                    //enable the decline button
                                    declineFriendRegBtn.setEnabled(true);

                                    declineFriendRegBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            DeclineFriendRequest();
                                        }
                                    });
                                }
                            }
                         else {
                            friendsRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // if the current snap shot has a child
                                    if (dataSnapshot.hasChild(receiver_user_id)) {
                                        CURRENT_STATE = "friends";
                                        sendFriendReqBtn.setText("Unfriend This Person");

                                        //invisible the decline button
                                        declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                        //disable the decline button
                                        declineFriendRegBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //invisible the decline button
        declineFriendRegBtn.setVisibility(View.INVISIBLE);
        //disable the decline button
        declineFriendRegBtn.setEnabled(false);

        if (!sender_user_id.equals(receiver_user_id)) {
            sendFriendReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //set the send friend request button to false
                    sendFriendReqBtn.setEnabled(false);

                    //if the current state not friends then you can send friend request
                    if (CURRENT_STATE.equals("not_friends")) {
                        SendFriendRequestToAPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        UnfriendAFriend();
                    }

                }
            });
        } else {
            sendFriendReqBtn.setVisibility(View.INVISIBLE);
            declineFriendRegBtn.setVisibility(View.INVISIBLE);
        }

    }


    //decline a friend request method
    private void DeclineFriendRequest()
    {
        friendReqRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            friendReqRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                sendFriendReqBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendReqBtn.setText("Send Friend Request");

                                                //invisible the decline button
                                                declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                                //disable the decline button
                                                declineFriendRegBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }




    //unfriend a friend method
    private void UnfriendAFriend() {
        friendsRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            friendsRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                sendFriendReqBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendReqBtn.setText("Send Friend Request");

                                                //invisible the decline button
                                                declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                                //disable the decline button
                                                declineFriendRegBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }






    //accept a friend request method
    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyy");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());

        friendsRef.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        friendsRef.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //if the two users become friends remove the friend request nodes

                                        friendReqRef.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            friendReqRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                sendFriendReqBtn.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendFriendReqBtn.setText("Unfriend This Person");

                                                                                declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                                                                declineFriendRegBtn.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                    }
                                });
                    }
                });
    }




    //cancel a friend request method
    private void CancelFriendRequest() {
        friendReqRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            friendReqRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                sendFriendReqBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendReqBtn.setText("Send Friend Request");

                                                //invisible the decline button
                                                declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                                //disable the decline button
                                                declineFriendRegBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }




    //send a friend request to a person method
    private void SendFriendRequestToAPerson() {
        //send a request from the person who is online to the current person the user
        //wants to send request to
        friendReqRef.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    friendReqRef.child(receiver_user_id).child(sender_user_id).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {


                                //create a hashmap of the notification put the notification into the database
                                HashMap<String, String> notificationsData = new HashMap<String, String>();
                                notificationsData.put("from", sender_user_id);
                                notificationsData.put("type", "request");

                                notificationsRef.child(receiver_user_id).push().setValue(notificationsData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            sendFriendReqBtn.setEnabled(true);
                                            CURRENT_STATE = "request_sent";
                                            sendFriendReqBtn.setText("Cancel Friend Request");

                                            //invisible the decline button
                                            declineFriendRegBtn.setVisibility(View.INVISIBLE);
                                            //disable the decline button
                                            declineFriendRegBtn.setEnabled(false);
                                        }
                                    }
                                });


                            }
                        }
                    });
                }
            }
        });
    }
}
