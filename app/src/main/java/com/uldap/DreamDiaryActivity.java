package com.uldap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uldap.model.DreamEntry;

public class DreamDiaryActivity extends Activity
        implements RealityCheckManager.RealityCheckHost {

    private EditText editTitle;
    private EditText editBody;
    private Button btnVoice;
    private Button btnSave;
    private Button btnViewPrevious;

    private SpeechHelper speechHelper;
    private RealityCheckManager realityCheck;
    private boolean totemActive;
    private boolean diaryActive;
    private String editingEntryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        editTitle = findViewById(R.id.editTitle);
        editBody = findViewById(R.id.editBody);
        btnVoice = findViewById(R.id.btnVoice);
        btnSave = findViewById(R.id.btnSave);
        btnViewPrevious = findViewById(R.id.btnViewPrevious);

        realityCheck = new RealityCheckManager(this, this);

        speechHelper = new SpeechHelper(this, new SpeechHelper.SpeechCallback() {
            @Override
            public void onPartialResult(String text) {
                appendText(text);
            }

            @Override
            public void onFinalResult(String text) {
                appendText(text);
                btnVoice.setText("VOICE");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DreamDiaryActivity.this, error, Toast.LENGTH_SHORT).show();
                btnVoice.setText("VOICE");
            }

            @Override
            public void onListeningChanged(boolean listening) {
                btnVoice.setText(listening ? "LISTENING..." : "VOICE");
            }
        });

        findViewById(R.id.rootLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && totemActive) {
                    realityCheck.onScreenTapped();
                    return true;
                }
                return false;
            }
        });

        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            new String[]{android.Manifest.permission.RECORD_AUDIO}, 100
                        );
                        return;
                    }
                }
                speechHelper.toggle();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString().trim();
                String body = editBody.getText().toString().trim();
                if (title.isEmpty() && body.isEmpty()) {
                    Toast.makeText(DreamDiaryActivity.this, "Write something first", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    if (editingEntryId != null) {
                        DreamEntry entry = new DreamEntry(
                            title.isEmpty() ? "Untitled" : title, body
                        );
                        entry.id = editingEntryId;
                        entry.date = editingEntryId.substring(0, 10);
                        DreamStorage.updateDream(DreamDiaryActivity.this, entry);
                        Toast.makeText(DreamDiaryActivity.this, "Dream updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        DreamEntry entry = new DreamEntry(
                            title.isEmpty() ? "Untitled" : title, body
                        );
                        DreamStorage.saveDream(DreamDiaryActivity.this, entry);
                        Toast.makeText(DreamDiaryActivity.this, "Dream saved!", Toast.LENGTH_SHORT).show();
                    }
                    editTitle.setText("");
                    editBody.setText("");
                    editingEntryId = null;
                    finish();
                } catch (Exception e) {
                    Toast.makeText(DreamDiaryActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnViewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final java.util.List<DreamEntry> dreams = DreamStorage.loadAllDreams(DreamDiaryActivity.this);
                if (dreams.isEmpty()) {
                    Toast.makeText(DreamDiaryActivity.this, "No previous dreams", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] items = new String[dreams.size()];
                for (int i = 0; i < dreams.size(); i++) {
                    DreamEntry e = dreams.get(i);
                    String preview = e.body.length() > 40 ? e.body.substring(0, 40) + "..." : e.body;
                    items[i] = e.date + "  " + e.title + "\n" + preview;
                }
                new AlertDialog.Builder(DreamDiaryActivity.this)
                    .setTitle("PREVIOUS DREAMS")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DreamEntry e = dreams.get(which);
                            editTitle.setText(e.title);
                            editBody.setText(e.body);
                            editingEntryId = e.id;
                            Toast.makeText(DreamDiaryActivity.this, "Editing dream from " + e.date, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechHelper.toggle();
            } else {
                Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void appendText(String text) {
        String current = editBody.getText().toString();
        if (current.isEmpty()) {
            editBody.setText(text);
        } else {
            editBody.setText(current + " " + text);
        }
        editBody.setSelection(editBody.getText().length());
    }

    @Override
    protected void onResume() {
        super.onResume();
        diaryActive = true;
        realityCheck.startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        diaryActive = false;
        realityCheck.cancelTimer();
        if (speechHelper != null) speechHelper.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        diaryActive = false;
        if (realityCheck != null) realityCheck.destroy();
        if (speechHelper != null) speechHelper.destroy();
    }

    @Override
    public void showDialog(String message, DialogInterface.OnDismissListener onDismiss) {
        new AlertDialog.Builder(this)
                .setTitle("REALITY CHECK")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .setOnDismissListener(onDismiss)
                .show();
    }

    @Override
    public void setTotemActive(boolean active) {
        totemActive = active;
    }

    @Override
    public boolean isDiaryActive() {
        return diaryActive && !isFinishing();
    }
}
