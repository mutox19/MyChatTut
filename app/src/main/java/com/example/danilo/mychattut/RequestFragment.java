package com.example.danilo.mychattut;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendsReqDatabaseRef;
    String receiver_list_user_id;

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
        FriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

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
                        receiver_list_user_id = listUsersId;
                        DatabaseReference GetTypeRef = getRef(position).child("request_type").getRef();
                        GetTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                               if(dataSnapshot.exists())
                               {
                                   String reqType = dataSnapshot.getValue().toString();
                                   if(reqType.equals("received"))
                                   {
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

                                               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View view)
                                                   {
                                                       CharSequence options[] = new CharSequence[]
                                                               {
                                                                       "Accept Friend Request",
                                                                       "Cancel Friend Request"
                                                               };

                                                       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                       builder.setTitle("Friend Request options");

                                                       builder.setItems(options, new DialogInterface.OnClickListener() {
                                                           @Override
                                                           public void onClick(DialogInterface dialogInterface, int position)
                                                           {

                                                               if(position == 0)
                                                               {
                                                                   AcceptFriendRequest();
                                                               }
                                                               if(position == 1)
                                                               {
                                                                   CancelFriendRequest();
                                                               }

                                                           }
                                                       });
                                                       builder.show();
                                                   }
                                               });
                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       });
                                   }
                                   else if(reqType.equals("sent"))
                                   {
                                       Button req_sent_btn = (Button)viewHolder.mView.findViewById(R.id.request_acceptBTN);
                                       req_sent_btn.setText("Req Sent");

                                       //hide the cancel button
                                       viewHolder.mView.findViewById(R.id.request_declineBTN).setVisibility(View.INVISIBLE);

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

                                               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View view)
                                                   {
                                                       CharSequence options[] = new CharSequence[]
                                                               {
                                                                       "Cancel Friend Request",
                                                               };

                                                       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                       builder.setTitle("Friend Request Sent");

                                                       builder.setItems(options, new DialogInterface.OnClickListener() {
                                                           @Override
                                                           public void onClick(DialogInterface dialogInterface, int position)
                                                           {

                                                               if(position == 0)
                                                               {
                                                                   CancelFriendRequest();
                                                               }

                                                           }
                                                       });
                                                       builder.show();
                                                   }
                                               });
                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       });
                                   }
                               }
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


    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyy");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsDatabaseRef.child(online_user_id).child(receiver_list_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FriendsDatabaseRef.child(receiver_list_user_id).child(online_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //if the two users become friends remove the friend request nodes

                                        FriendsReqDatabaseRef.child(online_user_id).child(receiver_list_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            FriendsReqDatabaseRef.child(receiver_list_user_id).child(online_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {

                                                                                Toast.makeText(getContext(),"Friend Request Accepted Successfully",Toast.LENGTH_SHORT);
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


    private void CancelFriendRequest() {
        FriendsReqDatabaseRef.child(online_user_id).child(receiver_list_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            FriendsReqDatabaseRef.child(receiver_list_user_id).child(online_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(),"Friend Request Cancelled Successfully", Toast.LENGTH_SHORT);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


}
