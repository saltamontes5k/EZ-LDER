package com.uldap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class SpeechHelper {

    public interface SpeechCallback {
        void onPartialResult(String text);
        void onFinalResult(String text);
        void onError(String error);
        void onListeningChanged(boolean listening);
    }

    private SpeechRecognizer recognizer;
    private SpeechCallback callback;
    private boolean listening;

    public SpeechHelper(Context ctx, SpeechCallback cb) {
        this.callback = cb;
        try {
            recognizer = SpeechRecognizer.createSpeechRecognizer(ctx);
            if (recognizer == null) {
                cb.onError("Speech recognition not available");
                return;
            }
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() { listening = false; cb.onListeningChanged(false); }
                @Override public void onEvent(int eventType, Bundle params) {}

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> data = partialResults
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (data != null && data.size() > 0) {
                        cb.onPartialResult(data.get(0));
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> data = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (data != null && data.size() > 0) {
                        cb.onFinalResult(data.get(0));
                    }
                    listening = false;
                    cb.onListeningChanged(false);
                }

                @Override
                public void onError(int error) {
                    String msg;
                    switch (error) {
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: msg = "Network timeout"; break;
                        case SpeechRecognizer.ERROR_NETWORK: msg = "Network error"; break;
                        case SpeechRecognizer.ERROR_AUDIO: msg = "Audio error"; break;
                        case SpeechRecognizer.ERROR_SERVER: msg = "Server error"; break;
                        case SpeechRecognizer.ERROR_CLIENT: msg = "Client error"; break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg = "No speech detected"; break;
                        case SpeechRecognizer.ERROR_NO_MATCH: msg = "No match found"; break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: msg = "Recognizer busy"; break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: msg = "Permission denied"; break;
                        default: msg = "Unknown error"; break;
                    }
                    listening = false;
                    cb.onListeningChanged(false);
                    cb.onError(msg);
                }
            });
        } catch (Exception e) {
            cb.onError("Failed to initialize: " + e.getMessage());
        }
    }

    public void toggle() {
        if (recognizer == null) return;
        if (listening) {
            stop();
        } else {
            start();
        }
    }

    public void start() {
        if (recognizer == null) return;
        listening = true;
        callback.onListeningChanged(true);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizer.startListening(intent);
    }

    public void stop() {
        if (recognizer != null && listening) {
            recognizer.stopListening();
        }
        listening = false;
        callback.onListeningChanged(false);
    }

    public boolean isListening() {
        return listening;
    }

    public void destroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
    }
}
