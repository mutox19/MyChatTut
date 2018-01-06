package com.example.danilo.mychattut;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Danilo on 2018-01-05.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }



    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_users, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();

        // this is the message for the user that is online that is sending
        if(fromUserID.equals(messageSenderID))
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
            holder.messageText.setTextColor(Color.BLACK);
            holder.messageText.setGravity(Gravity.RIGHT);
        }
        else
        {
            //this is for the user that is receieving and not sending
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageText.setGravity(Gravity.LEFT);
        }
        holder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount()
    {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView messageText;
        public CircleImageView userProfileImage;

        public MessageViewHolder(View view)
        {
            super(view);

            messageText = (TextView)view.findViewById(R.id.message_text);
            //userProfileImage =(CircleImageView)view.findViewById(R.id.message_profile_image);
        }
    }
}
