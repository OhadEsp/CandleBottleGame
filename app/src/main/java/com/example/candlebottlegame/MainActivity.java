package com.example.candlebottlegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference gamesRef;
    private EditText gameCodeInput;
    private Button createGameButton;
    private Button joinGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        gamesRef = FirebaseDatabase.getInstance().getReference("games");
        
        gameCodeInput = findViewById(R.id.gameCodeInput);
        createGameButton = findViewById(R.id.createGameButton);
        joinGameButton = findViewById(R.id.joinGameButton);

        setupButtons();
    }

    private void setupButtons() {
        createGameButton.setOnClickListener(v -> createNewGame());
        joinGameButton.setOnClickListener(v -> joinExistingGame());
    }

    private void createNewGame() {
        String gameId = UUID.randomUUID().toString();
        String playerId = mAuth.getCurrentUser().getUid();
        
        gamesRef.child(gameId).child("players").child(playerId).child("position").setValue(0f);
        
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("playerId", playerId);
        startActivity(intent);
    }

    private void joinExistingGame() {
        String gameId = gameCodeInput.getText().toString().trim();
        if (gameId.isEmpty()) {
            gameCodeInput.setError("Please enter a game code");
            return;
        }

        String playerId = mAuth.getCurrentUser().getUid();
        
        gamesRef.child(gameId).child("players").child(playerId).child("position").setValue(0f);
        
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("playerId", playerId);
        startActivity(intent);
    }
} 