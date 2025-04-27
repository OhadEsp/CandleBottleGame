package com.example.candlebottlegame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView candle;
    private ImageView bottle;
    private TextView statusText;
    private DatabaseReference gameRef;
    private String gameId;
    private String playerId;
    private boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize views
        candle = findViewById(R.id.candle);
        bottle = findViewById(R.id.bottle);
        statusText = findViewById(R.id.statusText);

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Initialize Firebase
        gameId = getIntent().getStringExtra("gameId");
        playerId = getIntent().getStringExtra("playerId");
        gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId);

        setupGameListeners();
    }

    private void setupGameListeners() {
        gameRef.child("players").child(playerId).child("position").addValueEventListener((snapshot) -> {
            if (snapshot.exists()) {
                float position = snapshot.getValue(Float.class);
                updateCandlePosition(position);
            }
        });

        gameRef.child("winner").addValueEventListener((snapshot) -> {
            if (snapshot.exists() && !isGameOver) {
                String winner = snapshot.getValue(String.class);
                isGameOver = true;
                if (winner.equals(playerId)) {
                    statusText.setText("You Won!");
                } else {
                    statusText.setText("You Lost!");
                }
            }
        });
    }

    private void updateCandlePosition(float position) {
        candle.setY(position);
        checkWinCondition();
    }

    private void checkWinCondition() {
        float candleBottom = candle.getY() + candle.getHeight();
        float bottleTop = bottle.getY();
        float bottleBottom = bottleTop + bottle.getHeight();

        if (candleBottom >= bottleTop && candleBottom <= bottleBottom) {
            if (!isGameOver) {
                gameRef.child("winner").setValue(playerId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !isGameOver) {
            float y = event.values[1]; // Y-axis acceleration
            float currentY = candle.getY();
            float newY = currentY + (y * 5); // Adjust sensitivity
            
            // Keep candle within screen bounds
            newY = Math.max(0, Math.min(newY, getWindowManager().getDefaultDisplay().getHeight() - candle.getHeight()));
            
            gameRef.child("players").child(playerId).child("position").setValue(newY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }
} 