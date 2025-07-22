package com.example.myapplication4.ui.chatbot;

public class ChatItem {
    private String text;
    private String id;

    public ChatItem(String text,String id) {
        this.text = text;
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }
}
