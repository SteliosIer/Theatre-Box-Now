package com.example.myapplication4.ui.chatbot;

import android.util.Log;

import com.example.myapplication4.models.Performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import viewmodel.ChatbotViewModel;

public class ChatbotUtils {
    ChatbotFragment fragment;
    ChatbotUtils(ChatbotFragment fragment){
        this.fragment = fragment;
    }
    //functions to find all performances requested (for example Hamlet may have 2 performances on different hours)
    private List<Performance> findMatchingPerformances(String name, String time) {
        List<Performance> matches = new ArrayList<>();
        for (Performance p : fragment.getViewModel().getPerformancesCache()) {
            if (p.getTitle().toLowerCase().contains(name.toLowerCase())) {
                Log.d("TAG", "add performance ");
                matches.add(p);
                if (!Objects.equals(time, " ") && !time.equals(p.getTime())){
                    Log.d("TAG", "remove performance ");
                    matches.remove(p);
                }
            }
        }
        return matches;
    }

    void handleMatchingPerformances(String performance, String time) {

        List<Performance> matches = findMatchingPerformances(performance,time);
        //how many performances found for the selected play
        if (matches.size() == 1) { //if only one performance if found proceed to booking
            if (!time.equals(" ")){ //if time is already given complete the booking
                fragment.completeBooking(matches.get(0),time);
            }
            else{
                fragment.startBookingFlow(matches.get(0));
            }
        } else if (matches.size() > 1) { //if more performances found let user choose
            fragment.showPerformanceOptions(matches);
        } else {
            fragment.addToChat("Bot: I couldn't find '" + performance + "'. Available performances:","ai");
            fragment.getTheatrePerformance();
        }
    }

    //define selected performance time from user
    void setTime(String message, String time){
        Log.d("TAG", "time is: "+time);
        if (time.isEmpty()){
            fragment.setSelectedTime(" ");
        }
        if  ((message.toLowerCase().contains("afternoon") || message.toLowerCase().contains("matinee")||
                time.contains("2")|| time.contains("14"))){
            fragment.setSelectedTime("14:00");
        }
        if  (message.toLowerCase().contains("evening")||
                time.contains("7")|| time.contains("19")){
            fragment.setSelectedTime("19:00");
        }
    }
}
