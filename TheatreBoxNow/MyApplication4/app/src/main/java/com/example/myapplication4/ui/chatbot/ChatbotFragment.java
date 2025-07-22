package com.example.myapplication4.ui.chatbot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication4.data.NLPClient;
import com.example.myapplication4.data.TheaterDatabase;
import com.example.myapplication4.databinding.ChatbotFragmentBinding;

import com.example.myapplication4.models.Performance;

import org.json.JSONException;
import org.json.JSONObject;

import viewmodel.ChatbotViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.example.myapplication4.ui.chatbot.ChatbotUtils;
public class ChatbotFragment extends Fragment {
    private ChatbotFragmentBinding binding;
    private ImageButton send_btn;

    private EditText mess_sent;

    List<ChatItem> chatList;

    RecyclerView recyclerView;

    private ChatbotViewModel viewModel;
    private TheaterDatabase database;
    private ChatbotUtils chatbotUtils;
    private NLPClient nlpClient;
    ChatAdapter chatAdapter;

    //boolean to check if there is any message currently on scrren
    int numOfCurrAIMess = 0;
    // Conversation states
    private static final int STATE_NORMAL = 0;
    private static final int STATE_AWAITING_PERFORMANCE = 1; //Wait until user selects a performance for the play
    private static final int STATE_AWAITING_TIME = 2; //Wait until user specifies performance based on time
    private int currentState = STATE_NORMAL; //initialize current state
    private Performance selectedPerformance;
    private String selectedTime;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = ChatbotFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //declare views
        recyclerView = binding.recView;
        send_btn = binding.sendBtn;
        mess_sent = (EditText)(binding.editText);

        //setting up recycler View
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);

        //set layout manager
        LinearLayoutManager linearLayoutManager =  new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        //define database
        database = TheaterDatabase.getInstance(requireContext());

        //define view model
        ChatbotViewModel.Factory factory = new ChatbotViewModel.Factory(database);
        viewModel = new ViewModelProvider(this, factory).get(ChatbotViewModel.class);

        // Initialize NLP client
        nlpClient = new NLPClient();

        //initialize utils
        chatbotUtils = new ChatbotUtils(this);

        setupUI();
        observePerformances();
    }

    private void setupUI() {
        addToChat("Bot: Hello how can i help you?", "ai");
        //on click button
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mess_sent.getText().toString().trim();
                //Log.d(TAG,"Text from click listener: " + message);
                if (!message.isEmpty())
                {
                    addToChat(message,"user");
                    processUserMessage(message);
                    mess_sent.setText("");
                }
            }
        });
    }

    //function to store available performances
    private void observePerformances() {

        viewModel.getPerformances().observe(getViewLifecycleOwner(), performances -> {
            List<Performance> availPerformances = new ArrayList<>();
            if (performances != null && !performances.isEmpty()) {

                for (Performance perf : performances){
                    if (perf.canBookSeats(1)){
                        availPerformances.add(perf);
                    }
                }
                // Store performances for later reference
                viewModel.setPerformancesCache(performances);
            }
        });
    }
    private void resetState() {
        currentState = STATE_NORMAL;
        selectedPerformance = null;
        selectedTime = null;
    }
    private void processUserMessage(String message) {
        //check Internet Connection
        if (!isNetworkAvailable()) {
            addToChat("Bot: No internet connection. Please check your network.", "ai");
            return;
        }

        // Process with NLP
        nlpClient.processText(message, new NLPClient.NLPCallback() {
            @Override
            public void onResult(String intent, JSONObject entities) {
                Log.d("TAG", "got result from process ");

                new Thread(() -> {
                    try {
                        handleIntent(message, intent, entities);
                    } catch (Exception e) {
                        Log.e("Chatbot", "Intent handling error", e);
                        requireActivity().runOnUiThread(() -> {
                            addToChat("Bot: Sorry, I encountered an error. Please try again.", "ai");
                        });
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                logError("NLP_ERROR", "NLP processing failed: " + error, null);
            }
        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
    private void handleIntent(String message, String intent, JSONObject entities) throws JSONException {
        Log.d("TAG", "CURRENT STATE: "+ currentState);
        Log.d("TAG", "num of ai mess: "+ numOfCurrAIMess);
        //get entities
        String performance = entities.optString("performance");
        String time = entities.optString("time");
        
        //if performance is selected switch state
        if (!performance.isEmpty()) { currentState = STATE_AWAITING_PERFORMANCE; }
        
        //find selected time (or return empty string if not selected)
        chatbotUtils.setTime(message,time);

        //handle states
        if (currentState == STATE_AWAITING_PERFORMANCE) {
            if (Objects.equals(intent, "TICKET_PRICE")) {
                addToChat("Bot: Tickets cost 20$. " +
                            "We have a discount at 15$ for students and elders. " +
                            "Tickets for children cost 10$.", "ai");
            }
            else{
                chatbotUtils.handleMatchingPerformances(performance, selectedTime);
            }
            return;
        } else if (currentState == STATE_AWAITING_TIME) {
            if (Objects.equals(intent, "TICKET_PRICE")) {
                addToChat("Bot: Tickets cost 20$. " +
                        "We have a discount at 15$ for students and elders. " +
                        "Tickets for children cost 10$.", "ai");
            }
            if (!selectedTime.equals(" ")){
                handleTimeSelection();
            }
            else{
                addToChat("Bot: Please specify time: ","ai");
            }
            return;
        }

        //handle intent cases only in default state
        switch(intent) {
            case "BOOK_TICKET":
                getTheatrePerformance();
                currentState = STATE_AWAITING_PERFORMANCE;
                break;

            case "LIST_PERFORMANCES":
                getTheatrePerformance();
                break;

            case "SHOW_TIMES":
                getCompletePerformances();
                break;

            case "TICKET_PRICE":
                addToChat("Bot: Tickets cost 20$. " +
                        "We have a discount at 15$ for students and elders. " +
                        "Tickets for children cost 10$.", "ai");
                break;

            case "THANKS":
                addToChat("Bot: Your welcome! If you need anything else don't hesitate to ask.", "ai");
                break;

            default: //including GREETING case
                addToChat("Bot: I can help with booking tickets or showing performances. What would you like?","ai");
        }
    }

    //show only performances by names (plays)
    void getTheatrePerformance(){
        requireActivity().runOnUiThread(() -> {
            viewModel.getPerformances().observe(getViewLifecycleOwner(), items -> {
                //check if there are any available performances
                if (items.isEmpty()) {
                    addToChat("Bot: Unfortunately no performances are available", "ai");
                    return;
                }
                StringBuilder messageWithPerformances = new StringBuilder("Bot: Here are our performances:\n");

                //avoid showing multiple times the same play
                Performance lastItem = items.get(0);
                messageWithPerformances.append(lastItem.getTitle()).append("\n");

                for (Performance item : items) {
                    //dont add the same play more than once
                    if (!item.getTitle().equals(lastItem.getTitle())) {
                        messageWithPerformances.append(item.getTitle()).append("\n");//add performance to message
                    }
                    lastItem = item;
                }
                addToChat(messageWithPerformances.toString(), "ai");

            });
        });
    }

    //show performances with number of seats and times included
    private void getCompletePerformances(){
        requireActivity().runOnUiThread(() -> {
            viewModel.getPerformances().observe(getViewLifecycleOwner(), items -> {
                //check if there are any available performances
                if (items.isEmpty()) {
                    addToChat("Bot: Unfortunately no performances are available", "ai");
                    return;
                }

                StringBuilder messageWithPerformances = new StringBuilder("Bot: Here are our performances:\n");

                for (Performance item : items) {
                    messageWithPerformances.append(item.getTitle()).append(" number of seats: ")
                            .append(item.getAvailableSeats())
                            .append(" at: ").append(item.getTime())
                            .append("\n"); //add performance to message
                }

                addToChat(messageWithPerformances.toString(), "ai");

            });
        });
    }
    private void handleTimeSelection() {
        if (selectedPerformance != null) {
            if (!selectedTime.equals(" ")){
                completeBooking(selectedPerformance,selectedTime);
            } else {
                addToChat("Bot: Please specify 'matinee' or 'evening'","ai");
                return;
            }
        }
        currentState = STATE_NORMAL; //return to normal state after booking
    }

    //request performance based on time and given play
    void startBookingFlow(Performance performance) {
        selectedPerformance = performance;

        addToChat("Bot: " + performance.getTitle() + " has available seats. " +
                "Would you like matinee (2PM) or evening (7PM)?","ai");
        currentState = STATE_AWAITING_TIME;
    }

    //show list of matching performances (same performance different times)
    void showPerformanceOptions(List<Performance> performances) {
        StringBuilder response = new StringBuilder("Bot: Performances are: \n");
        for (int i = 0; i < performances.size(); i++) {
            Performance p = performances.get(i);
            response.append(i+1).append(". ").append(p.getTitle())
                    .append(" at ").append(p.getTime())
                    .append(" number of seats: ").append(p.getAvailableSeats())
                    .append("\n");
        }
        response.append("Please specify which one you want.");
        addToChat(response.toString(),"ai");
        currentState = STATE_AWAITING_PERFORMANCE; //remain to current state until only one performance is selected
    }


    void completeBooking(Performance performance, String time) {
        //check available seats
        if (performance.getAvailableSeats() <= 0) {
            addToChat("Bot: Sorry, " + performance.getTitle() + " is sold out.","ai");
            resetState();
            return;
        }
        //update performance
        performance.setAvailableSeats(performance.getAvailableSeats()-1);
        new Thread(() -> {
            database.bookingDao().updatePerformance(performance);
        }).start();
        // Implement actual booking logic

        addToChat("Bot: Booking confirmed for " + performance.getTitle() +
                " at " + time + ". Your reference is #" + (1000 + (int) (Math.random() * 9000)), "ai");

        resetState();
    }
    private void logError(String tag, String message, Exception e) {
        Log.e(tag, message, e);
        addToChat("Bot: Sorry, I encountered a technical issue. Please try again later.","ai");
        resetState();
    }

    void addToChat(String message,String id){
        requireActivity().runOnUiThread(new Runnable(){

            @Override
            public void run() {
                if ("ai".equals(id)) {
                    // Check the last message
                    if (!chatList.isEmpty()) {
                        ChatItem lastItem = chatList.get(chatList.size() - 1);
                        if ("ai".equals(lastItem.getId())) {
                            // AI message already exists, skip adding this one
                            Log.d("TAG", "Skipping duplicate AI message");
                            return;
                        }
                    }
                }
                //if there is already a message on screen from AI dont display another one
                if (Objects.equals(id, "ai")) {
                    numOfCurrAIMess++;
                    if (numOfCurrAIMess>1){
                        Log.d("TAG", "numOfCurrAIMess>1 ");
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
                        return;
                    }
                }else{
                    numOfCurrAIMess--;
                }
                chatList.add(new ChatItem(message, id));
                chatAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
            }
        });
    }

    ChatbotViewModel getViewModel(){
        return viewModel;
    }

    void setSelectedTime(String time){
        selectedTime = time;
    }
}
