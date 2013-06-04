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
import android.graphics.Color;
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

public class ChatActivity extends SherlockActivity implements ChildEventListener {
    private static final String FIREBASE_URL = "https://android-sdk-demo.firebaseio.com/chat";
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String USERNAME_KEY = "username";
    private static final String NAME = "username";
    private static final String MESSAGE = "message";
    
    private String mUsername;
    private Firebase mFirebase;
    private ViewGroup mChatMessagesLayout;
    private EditText mChatMessageView;
    private ImageButton mSendButton;
    private ScrollView mScrollView;
    private LayoutInflater mLayoutInflater;
    
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
        mScrollView = (ScrollView) findViewById(R.id.activity_chat_scroll_view);
        mLayoutInflater = LayoutInflater.from(this);

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
            
            mChatMessageView.setText("");
        }
    }

    /* ChildEventListener callbacks */
    
    @Override
    public void onCancelled() {
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
        Log.d(TAG, "onChildAdded()");
        
        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
        String username = map.get(NAME).toString();
        String message = map.get(MESSAGE).toString();
        
        SpannableString span = new SpannableString(username);
        span.setSpan(new ForegroundColorSpan(Color.BLACK) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.username));
            }
        }, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        TextView chatView = (TextView) mLayoutInflater.inflate(R.layout.chat_message, null);
        chatView.setText(TextUtils.concat(span, " says ", message));
        
        mChatMessagesLayout.addView(chatView);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
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
