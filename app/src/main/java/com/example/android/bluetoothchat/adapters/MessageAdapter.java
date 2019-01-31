package com.example.android.bluetoothchat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.models.Message;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    LayoutInflater layoutInflater;
    private ArrayList<Message> messageList;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public MessageAdapter(Context context, ArrayList<Message> messages) {
       messageList = messages;
       this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {

        Message message = (Message) messageList.get(position);

        if (message.getSender().equals("Me")) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = layoutInflater
                    .inflate(R.layout.my_message, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = layoutInflater
                    .inflate(R.layout.other_message, parent, false);
            return new MessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
          messageViewHolder.messageText.setText(messageList.get(i).text);
          messageViewHolder.sender.setText(messageList.get(i).sender);
          messageViewHolder.time.setText(messageList.get(i).systemTime);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView messageText,sender,time;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.mText);
            sender = itemView.findViewById(R.id.owner);
            time = itemView.findViewById(R.id.time);
        }
    }
}
