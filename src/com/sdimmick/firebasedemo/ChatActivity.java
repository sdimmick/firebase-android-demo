package com.sdimmick.firebasedemo;

import java.util.HashMap;
import java.util.Map;

import com.actionbarsherlock.app.SherlockActivity;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Posts and displays chat messages for all devices pointing to FIREBASE_URL
 * @author sdimmick
 */
public class ChatActivity extends SherlockActivity implements ChildEventListener {
    // Tag for the Android logger
    private static final String TAG = ChatActivity.class.getSimpleName();
    
    // The root Firebase URL for this demo app
    private static final String FIREBASE_URL = "https://android-sdk-demo.firebaseio.com/chat";
    
    // Key for the "logged in" user's name
    private static final String USERNAME_KEY = "username";
    
    // Username key for the chat payload
    private static final String NAME = "username";
    
    // Chat message key for the chat payload
    private static final String MESSAGE = "message";
    
    // The "logged in" user's name
    private String mUsername;
    
    // The Firebase instance, pointing to FIREBASE_URL
    private Firebase mFirebase;
    
    // Layout inflater instance for inflating chat message views
    private LayoutInflater mLayoutInflater;
    
    // References to views defined in activity_chat.xml
    private ViewGroup mChatMessagesLayout;
    private EditText mChatMessageView;
    private ImageButton mSendButton;
    private ScrollView mScrollView;
    
    /**
     * Creates an {@link Intent} to launch this {@link Activity}
     * 
     * @param context the caller's {@link Context}
     * @param username the "logged in" user's name
     * @return an {@link Intent} to launch this activity so we can begin chatting
     */
    public static Intent createIntent(Context context, String username) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(USERNAME_KEY, username);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the layout
        setContentView(R.layout.activity_chat);
        
        // Set the actionbar title
        setTitle(R.string.chat);
        
        // Initialize our Firebase instance
        initializeFirebase();
        
        // Grab the username that was passed in via the intent
        mUsername = getIntent().getStringExtra(USERNAME_KEY);
        
        // Save references to various views we'll be working with
        mChatMessagesLayout = (ViewGroup) findViewById(R.id.activity_chat_message_layout);
        mChatMessageView = (EditText) findViewById(R.id.activity_chat_input);
        mSendButton = (ImageButton) findViewById(R.id.activity_chat_send);
        mScrollView = (ScrollView) findViewById(R.id.activity_chat_scroll_view);
        
        // Grab a LayoutInflater instance so we can inflate chat message views as they arrive
        mLayoutInflater = LayoutInflater.from(this);

        // Send the chat message whenever the 'send' button is clicked
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    
    /**
     * Initiazes the Firebase client
     */
    private void initializeFirebase() {
        try {
            mFirebase = new Firebase(FIREBASE_URL);
            mFirebase.addChildEventListener(this);
        } catch (FirebaseException e) {
            Log.e(TAG, "Error initializing Firebase client", e);
            Toast.makeText(this, "Error initializing Firebase Client", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Creates a new chat message object in our Firebase
     */
    private void sendMessage() {
        String message = mChatMessageView.getText().toString().trim();
        
        if (message.length() > 0) {
            Map<String, Object> firebaseMessage = new HashMap<String, Object>();
            firebaseMessage.put(NAME, mUsername);
            firebaseMessage.put(MESSAGE, message);
            
            try {
                Firebase firebase = mFirebase.autoIdChild();
                firebase.setValue(firebaseMessage);
            } catch (FirebaseException e) {
                Log.e(TAG, "Error sending message", e);
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
            }
            
            // Clear the chat message edit text
            mChatMessageView.setText("");
        }
    }
    
    /**
     * Scrolls the chat message list view to the bottom
     */
    private void scrollToBottom() {
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /* ChildEventListener callbacks */
    
    @Override
    public void onCancelled() {
        Log.d(TAG, "onCancelled()");
    }

    /**
     * Displays new chat messages as they are added as children to our Firebase
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
        Log.d(TAG, "onChildAdded()");
        
        // Chat messages are serialized / deserialized String -> Object maps
        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
        String username = map.get(NAME).toString();
        String message = map.get(MESSAGE).toString();
        
        // Show the usernames in a different color
        SpannableString span = new SpannableString(username);
        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.username)) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.username));
            }
        }, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        TextView chatView = (TextView) mLayoutInflater.inflate(R.layout.chat_message, null);
        chatView.setText(TextUtils.concat(span, " says ", message));
        
        // Add the chat message view to the scroll view
        mChatMessagesLayout.addView(chatView);
        
        // Scroll the scroll view to the bottom
        scrollToBottom();
    }
    
    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
        Log.d(TAG, "onChildChanged()");
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
        Log.d(TAG, "onChildMoved()");
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        Log.d(TAG, "onChildRemoved()");
    }

}
