package com.uldap;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;

public class RealityCheckManager {

    public interface RealityCheckHost {
        void showDialog(String message, DialogInterface.OnDismissListener onDismiss);
        boolean isDiaryActive();
    }

    private static final int CHECK_DELAY_MS = 25000;

    private final RealityCheckHost host;
    private final Handler handler = new Handler();

    private final Runnable startCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (host.isDiaryActive()) {
                showRealityCheck1();
            }
        }
    };

    public RealityCheckManager(RealityCheckHost host) {
        this.host = host;
    }

    public void startTimer() {
        handler.removeCallbacks(startCheckRunnable);
        handler.postDelayed(startCheckRunnable, CHECK_DELAY_MS);
    }

    public void cancelTimer() {
        handler.removeCallbacks(startCheckRunnable);
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
                        playTotem();
                    }
                }
            }
        );
    }

    private void playTotem() {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tg.release();
                }
            }, 2500);
        } catch (Exception ignored) {}
    }

    public void destroy() {
        cancelTimer();
    }
}
