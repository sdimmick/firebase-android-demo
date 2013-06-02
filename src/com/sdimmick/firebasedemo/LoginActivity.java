package com.sdimmick.firebasedemo;

import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends SherlockActivity {
    private EditText mUsernameView;
    private Button mContinueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);
        
        mUsernameView = (EditText) findViewById(R.id.activity_login_username);
        mContinueButton = (Button) findViewById(R.id.activity_login_continue);
        
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameView.getText().toString().trim();
                if (username.length() > 0) {
                    // We have a username. Start the chat activity.
                    Intent intent = ChatActivity.createIntent(LoginActivity.this, username);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

}
