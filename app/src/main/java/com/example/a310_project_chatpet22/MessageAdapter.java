package com.example.a310_project_chatpet22;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bot_message, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textViewMessage.setText(messageList.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        public ViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }

}
