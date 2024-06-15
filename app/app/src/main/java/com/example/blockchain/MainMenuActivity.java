package com.example.blockchain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blockchain.users.User;

public class MainMenuActivity extends AppCompatActivity {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        this.user = (User) getIntent().getSerializableExtra("user");

        Button btnVote = findViewById(R.id.btnVote);
        Button btnCheckVote = findViewById(R.id.btnCheckVote);

        btnVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this,
                        VoteActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
            }
        });

        btnCheckVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, CheckActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
            }
        });
    }

    private void roastyToasty(Object o) {
        Toast.makeText(this, o.toString(),
                Toast.LENGTH_SHORT).show();
    }
}

