package com.uldap;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;

public class RealityCheckManager {

    public interface RealityCheckHost {
        void showDialog(String message, DialogInterface.OnDismissListener onDismiss);
        void setTotemActive(boolean active);
        boolean isDiaryActive();
    }

    private static final int CHECK_DELAY_MS = 25000;

    private final Context context;
    private final RealityCheckHost host;
    private final Handler handler = new Handler();
    private ToneGenerator toneGen;
    private boolean totemPlaying;

    private final Runnable startCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (host.isDiaryActive()) {
                showRealityCheck1();
            }
        }
    };

    public RealityCheckManager(Context context, RealityCheckHost host) {
        this.context = context;
        this.host = host;
    }

    public void startTimer() {
        handler.removeCallbacks(startCheckRunnable);
        handler.postDelayed(startCheckRunnable, CHECK_DELAY_MS);
    }

    public void cancelTimer() {
        handler.removeCallbacks(startCheckRunnable);
        stopTotem();
    }

    private void showRealityCheck1() {
        if (!host.isDiaryActive()) return;
        host.showDialog(
            "REALITY CHECK\n\nConsider your current conditions — how realistic are they?\n\nLook closely at the room, your hands, the text. Are you dreaming?",
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (host.isDiaryActive()) {
                        showRealityCheck2();
                    }
                }
            }
        );
    }

    private void showRealityCheck2() {
        if (!host.isDiaryActive()) return;
        host.showDialog(
            "REALITY CHECK\n\nWhat do you remember from the past few minutes? How did you get here?\n\nDoes it make sense?",
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (host.isDiaryActive()) {
                        playTotemSound();
                    }
                }
            }
        );
    }

    private void playTotemSound() {
        if (!host.isDiaryActive()) return;
        host.setTotemActive(true);
        totemPlaying = true;

        try {
            toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
        } catch (Exception ignored) {}

        handler.postDelayed(totemLoopRunnable, 3000);
    }

    private final Runnable totemLoopRunnable = new Runnable() {
        @Override
        public void run() {
            if (totemPlaying && toneGen != null) {
                try {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
                } catch (Exception ignored) {}
                handler.postDelayed(this, 3000);
            }
        }
    };

    public void onScreenTapped() {
        if (totemPlaying) {
            stopTotem();
            host.setTotemActive(false);
            startTimer();
        }
    }

    private void stopTotem() {
        totemPlaying = false;
        handler.removeCallbacks(totemLoopRunnable);
        if (toneGen != null) {
            toneGen.stopTone();
            toneGen.release();
            toneGen = null;
        }
    }

    public void destroy() {
        cancelTimer();
        if (toneGen != null) {
            toneGen.release();
            toneGen = null;
        }
    }
}
