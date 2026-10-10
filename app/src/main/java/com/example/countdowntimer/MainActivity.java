package com.example.countdowntimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private EditText minutesInput, secondsInput;
    private TextView timeView, statusView;
    private Button startButton, resetButton;
    private CountDownTimer timer;
    private long remainingMs = 60_000L;
    private boolean running = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        updateTime(remainingMs);
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String text, int size) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(size);
        v.setGravity(Gravity.CENTER);
        return v;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        return b;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(16,16,24));

        TextView title = label("ТАЙМЕР", 24);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(60)));

        timeView = label("01:00", 56);
        timeView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        root.addView(timeView, new LinearLayout.LayoutParams(-1, dp(120)));

        statusView = label("Установи время и нажми «Старт»", 14);
        statusView.setTextColor(Color.LTGRAY);
        root.addView(statusView, new LinearLayout.LayoutParams(-1, dp(44)));

        LinearLayout inputs = new LinearLayout(this);
        inputs.setGravity(Gravity.CENTER);
        inputs.setOrientation(LinearLayout.HORIZONTAL);
        minutesInput = new EditText(this);
        minutesInput.setHint("Мин");
        minutesInput.setInputType(2);
        minutesInput.setSingleLine(true);
        minutesInput.setTextColor(Color.WHITE);
        minutesInput.setHintTextColor(Color.GRAY);
        minutesInput.setGravity(Gravity.CENTER);
        secondsInput = new EditText(this);
        secondsInput.setHint("Сек");
        secondsInput.setInputType(2);
        secondsInput.setSingleLine(true);
        secondsInput.setTextColor(Color.WHITE);
        secondsInput.setHintTextColor(Color.GRAY);
        secondsInput.setGravity(Gravity.CENTER);
        inputs.addView(minutesInput, new LinearLayout.LayoutParams(0, dp(56), 1));
        TextView colon = label(":", 24);
        inputs.addView(colon, new LinearLayout.LayoutParams(dp(30), dp(56)));
        inputs.addView(secondsInput, new LinearLayout.LayoutParams(0, dp(56), 1));
        root.addView(inputs, new LinearLayout.LayoutParams(-1, dp(64)));

        startButton = button("▶  Старт");
        resetButton = button("↺  Сброс");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(56));
        bp.setMargins(0, dp(12), 0, 0);
        root.addView(startButton, bp);
        root.addView(resetButton, bp);

        startButton.setOnClickListener(v -> {
            if (running) pauseTimer(); else startTimer();
        });
        resetButton.setOnClickListener(v -> resetTimer());
        setContentView(root);
    }

    private long inputDuration() {
        String m = minutesInput.getText().toString().trim();
        String s = secondsInput.getText().toString().trim();
        long mins = m.isEmpty() ? 0 : Long.parseLong(m);
        long secs = s.isEmpty() ? 0 : Long.parseLong(s);
        if (secs > 59) throw new IllegalArgumentException("Секунд должно быть от 0 до 59");
        long total = mins * 60 + secs;
        if (total <= 0) throw new IllegalArgumentException("Укажи время больше нуля");
        if (total > 359999) throw new IllegalArgumentException("Максимум 99:59:59");
        return total * 1000L;
    }

    private void startTimer() {
        if (timer == null || remainingMs <= 0) {
            try { remainingMs = inputDuration(); }
            catch (Exception e) {
                Toast.makeText(this, e.getMessage() == null ? "Проверь время" : e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (timer != null) timer.cancel();
        running = true;
        startButton.setText("Ⅱ  Пауза");
        statusView.setText("Отсчёт идёт…");
        timer = new CountDownTimer(remainingMs, 100) {
            public void onTick(long ms) { remainingMs = ms; updateTime(ms); }
            public void onFinish() {
                remainingMs = 0; running = false; timer = null;
                updateTime(0); startButton.setText("▶  Старт");
                statusView.setText("Время вышло!");
                Toast.makeText(MainActivity.this, "Время вышло!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();
        timer = null; running = false;
        startButton.setText("▶  Продолжить");
        statusView.setText("Пауза");
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        timer = null; running = false; remainingMs = 60_000L;
        startButton.setText("▶  Старт");
        statusView.setText("Установи время и нажми «Старт»");
        minutesInput.setText(""); secondsInput.setText("");
        updateTime(remainingMs);
    }

    private void updateTime(long ms) {
        long total = Math.max(0, ms / 1000);
        long h = total / 3600, m = (total % 3600) / 60, s = total % 60;
        timeView.setText(h > 0
            ? String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
            : String.format(Locale.getDefault(), "%02d:%02d", m, s));
    }

    @Override protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
