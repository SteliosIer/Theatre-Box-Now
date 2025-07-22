package com.example.myapplication4.ui.chatbot;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication4.R;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatItem> chatList;

    public ChatAdapter(List<ChatItem> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_text_view, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chatItem = chatList.get(position);
        if (chatItem.getId().equals("ai")){
            holder.userLayout.setVisibility(View.GONE);
            holder.aiLayout.setVisibility(View.VISIBLE);
            holder.aiText.setText(chatItem.getText());
        }
        else{
            holder.aiLayout.setVisibility(View.GONE);
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.userText.setText(chatItem.getText());
        }
    }


    @Override
    public int getItemCount() {
        Log.d("AdapterCount", "Items: " + chatList.size());
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView userText,aiText;
        LinearLayout userLayout,aiLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            this.userText = itemView.findViewById(R.id.user_text_view);
            this.aiText = itemView.findViewById(R.id.ai_text_view);
            this.userLayout = itemView.findViewById(R.id.user_layout);
            this.aiLayout = itemView.findViewById(R.id.ai_layout);

            Log.d(TAG,"holder text from ChatViewHolder "+ userText.getText());
        }
    }
}
