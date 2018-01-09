package com.example.danilo.mychattut;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {


    private RecyclerView myRequestList;
    private View myMainView;
    private DatabaseReference FriendsRequestRef;
    private FirebaseAuth mAuth;
    String online_user_id;
    private DatabaseReference UsersRef;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView = inflater.inflate(R.layout.fragment_request, container, false);

        myRequestList = (RecyclerView)myMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        FriendsRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myRequestList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        //show the newest friend request at the top
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myRequestList.setLayoutManager(linearLayoutManager);
        return myMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Request,RequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Request, RequestViewHolder>
                        (
                                Request.class,
                                R.layout.friends_request_all_users_layout,
                                RequestFragment.RequestViewHolder.class,
                                FriendsRequestRef

                        ) {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position)
                    {
                        final String listUsersId = getRef(position).getKey();
                        UsersRef.child(listUsersId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                //set username and thumbimage
                                final String username = dataSnapshot.child("user_name").getValue().toString();
                                final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                viewHolder.setUserName(username);
                                viewHolder.setThumb_User_Image(thumbImage,getContext());
                                viewHolder.setUser_Status(userStatus);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };

        myRequestList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUserName(String username)
        {
            TextView userNameDisplay = (TextView)mView.findViewById(R.id.request_profile_name);
            userNameDisplay.setText(username);
        }

        public void setThumb_User_Image(final String thumbImage, final Context ctx)
        {
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.request_profile_image);

                //to load images offline
                Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(thumb_image, new Callback() {
                    @Override
                    public void onSuccess() {

                        //will load images offline
                    }

                    @Override
                    public void onError() {
                        //to load images only when online
                        Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile).into(thumb_image);
                    }
                });

        }

        public void setUser_Status(String userStatus)
        {
            TextView status = (TextView)mView.findViewById(R.id.request_profile_status);
            status.setText(userStatus);
        }
    }

}
