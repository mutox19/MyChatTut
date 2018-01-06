package com.example.danilo.mychattut;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */


public class ChatsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private View myMainView;
    private DatabaseReference FriendsRef;
    private RecyclerView myChatsList;
    String online_user_id;
    private DatabaseReference usersRef;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView = inflater.inflate(R.layout.fragment_chats2, container, false);
        myChatsList = (RecyclerView) myMainView.findViewById(R.id.chats_list);

        //get the current user thats is logged in
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        //load the friends list offline
        FriendsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //load users list offline
        usersRef.keepSynced(true);
        myChatsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        //reserve the order of the chats
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatsList.setLayoutManager(linearLayoutManager);


        return myMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> FirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (
                        Chats.class,
                        R.layout.all_users_display_layout,
                        ChatsFragment.ChatsViewHolder.class,
                        FriendsRef
                ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, int position) {
                //set the date

                final String list_user_id = getRef(position).getKey();
                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        //set username and thumbimage
                        final String username = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                        if (dataSnapshot.hasChild("online"))
                        {
                            String online_status = (String) dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }
                        viewHolder.setUsername(username);
                        viewHolder.setThumbImage(getContext(), thumbImage);
                        viewHolder.setUserStatus(userStatus);

                        //set the options for the dialog box
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                if(dataSnapshot.child("online").exists())
                                {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                    chatIntent.putExtra("user_name",username);
                                    startActivity(chatIntent);
                                }
                                else
                                {
                                    usersRef.child(list_user_id).child("online").setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                                    chatIntent.putExtra("user_name",username);
                                                    startActivity(chatIntent);
                                                }
                                            });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        myChatsList.setAdapter(FirebaseRecyclerAdapter);

    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }



        public void setUsername(String username) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_name);
            userNameDisplay.setText(username);
        }

        public void setThumbImage(final Context ctx, final String user_thumb_image) {
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            if (!user_thumb_image.equals("default_profile")) {


                //to load images offline
                Picasso.with(ctx).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(thumb_image, new Callback() {
                    @Override
                    public void onSuccess() {

                        //will load images offline
                    }

                    @Override
                    public void onError() {
                        //to load images only when online
                        Picasso.with(ctx).load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
                    }
                });
            }
        }

        public void setUserOnline(String online_status) {
            ImageView online_Status_view = (ImageView) mView.findViewById(R.id.online_status);

            if (online_status.equals("true")) {
                online_Status_view.setVisibility(View.VISIBLE);
            } else {
                online_Status_view.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus)
        {
            TextView user_status = (TextView)mView.findViewById(R.id.all_users_status);
            user_status.setText(userStatus);
        }
    }
}
