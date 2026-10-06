package net.kdt.pojavlaunch;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.concurrent.atomic.AtomicInteger;
import android.view.animation.OvershootInterpolator;

public final class BounceAnimation {
    private static final int TAG_KEY = 0x7f0b0c01;
    private BounceAnimation() {}

    public static void applyToViewTree(View root) {
        if (root == null) return;
        apply(root);
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyToViewTree(group.getChildAt(i));
            }
        }
    }

    private static void apply(View view) {
        if (!view.isClickable() || view.getTag(TAG_KEY) != null) return;
        view.setTag(R.id.cryonix_bounce_tag, Boolean.TRUE);
        view.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(70L).start();
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                       event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                bounce(v);
            }
            return false;
        });
    }

    private static void bounce(View view) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.94f, 1.06f, 0.98f, 1.0f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.94f, 1.06f, 0.98f, 1.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy);
        set.setDuration(260L);
        set.setInterpolator(new OvershootInterpolator(1.2f));
        set.start();
    }
}