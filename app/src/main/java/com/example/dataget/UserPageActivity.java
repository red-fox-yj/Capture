package com.example.dataget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        TextView username=(TextView)findViewById(R.id.textView18);
        TextView email=(TextView)findViewById(R.id.textView17);
        Intent intent = getIntent();
        username.setText(intent.getStringExtra("username"));
        email.setText(intent.getStringExtra("email"));
    }
}
