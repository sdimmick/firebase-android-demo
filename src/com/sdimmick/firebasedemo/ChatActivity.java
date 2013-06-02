package com.sdimmick.firebasedemo;

import java.util.HashMap;
import java.util.Map;

import com.actionbarsherlock.app.SherlockActivity;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends SherlockActivity implements ChildEventListener {
    private static final String FIREBASE_URL = "https://android-sdk-demo.firebaseio.com/";
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String USERNAME_KEY = "username";
    private static final String NAME = "name";
    private static final String MESSAGE = "message";
    
    private String mUsername;
    private Firebase mFirebase;
    private ViewGroup mChatMessagesLayout;
    private EditText mChatMessageView;
    private ImageButton mSendButton;
    
    private String mMessageUsername;
    private String mMessageContents;
    
    public static Intent createIntent(Context context, String username) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(USERNAME_KEY, username);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle(R.string.chat);
        
        initializeFirebase();
        
        mUsername = getIntent().getStringExtra(USERNAME_KEY);
        
        mChatMessagesLayout = (ViewGroup) findViewById(R.id.activity_chat_message_layout);
        mChatMessageView = (EditText) findViewById(R.id.activity_chat_input);
        mSendButton = (ImageButton) findViewById(R.id.activity_chat_send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    
    private void initializeFirebase() {
        // Initialize the Firebase client
        try {
            mFirebase = new Firebase(FIREBASE_URL);
            mFirebase.addChildEventListener(this);
        } catch (FirebaseException e) {
            Log.e(TAG, "Error initializing Firebase client", e);
            Toast.makeText(this, "Error initializing Firebase Client", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendMessage() {
        String message = mChatMessageView.getText().toString().trim();
        
        Map<String, Object> firebaseMessage = new HashMap<String, Object>();
        firebaseMessage.put(NAME, mUsername);
        firebaseMessage.put(MESSAGE, message);
        
        try {
            mFirebase.updateChildren(firebaseMessage);
        } catch (FirebaseException e) {
            Log.e(TAG, "Error sending message", e);
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
        }
        
        mChatMessageView.setText("");
    }

    /* ChildEventListener callbacks */
    
    @Override
    public void onCancelled() {
        
    }

    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
        if (snapshot.getName().equals(NAME)) {
            mMessageUsername = snapshot.getValue().toString();
        } else if (snapshot.getName().equals(MESSAGE)) {
            mMessageContents = snapshot.getValue().toString();
        }
        
        if (mMessageUsername != null && mMessageContents != null) {
            // We have a complete message. Display it.
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView chatView = (TextView) inflater.inflate(R.layout.chat_message, null, false);
            chatView.setText(getString(R.string.chat_message, mMessageUsername, mMessageContents));
            mChatMessagesLayout.addView(chatView);
            
            mMessageUsername = null;
            mMessageContents = null;
        }
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
        
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
        
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        
    }

}
