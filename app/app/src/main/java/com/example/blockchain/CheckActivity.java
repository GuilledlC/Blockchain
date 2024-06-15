package com.example.blockchain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blockchain.users.User;

public class CheckActivity extends AppCompatActivity {

	User user;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		this.user = (User) getIntent().getSerializableExtra("user");

		initializeViews();
		setListeners();

		try {
			boolean check = user.checkVote();
			if(check)
				textView.setText("Voto contado en la Blockchain");
			else
				textView.setText("Voto no contado en la Blockchain.\nPor favor vote de nuevo.");

		} catch (NullPointerException e) {
			textView.setText("No se ha podido conectar al nodo");
		}

	}

	private void initializeViews() {
		textView = findViewById(R.id.voteStatus);
		buttonBack = findViewById(R.id.buttonBack);
	}

	private void setListeners() {
		buttonBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckActivity.this, MainMenuActivity.class);
				intent.putExtra("user", user);
				startActivity(intent);
				finish();
			}
		});
	}


	TextView textView;
	Button buttonBack;

}
