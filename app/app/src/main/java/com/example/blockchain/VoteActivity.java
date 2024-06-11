package com.example.blockchain;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blockchain.users.User;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class VoteActivity extends AppCompatActivity {
    private User user;
    private MaterialAutoCompleteTextView autoCompleteTextView;
    private TextInputLayout partyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        this.user = (User) getIntent().getSerializableExtra("user");

        Button btnCancel = findViewById(R.id.buttonCancel);
        Button btnConfirm = findViewById(R.id.buttonConfirm);

        autoCompleteTextView = findViewById(R.id.autocompleteParties);
        partyLayout = findViewById(R.id.layoutParties);

        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(new VoteOnClickListener());
    }

    private void roastyToasty(Object o) {
        Toast.makeText(this, o.toString(),
                Toast.LENGTH_SHORT).show();
    }


    private class VoteOnClickListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View v) {
            String party;

            String partyAux = autoCompleteTextView.getText().toString();
            if(partyAux.isEmpty()) {
                partyLayout.setError("Por favor, elija partido");
                return;
            } else
                party = partyAux;
            try {
                user.vote(party);
                System.out.println(party);
                Intent intent = new Intent(VoteActivity.this,
                        MainMenuActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
                roastyToasty("Mal");
            }

        }
    }
}

