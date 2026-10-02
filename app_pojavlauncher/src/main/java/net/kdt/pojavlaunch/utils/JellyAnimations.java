package net.kdt.pojavlaunch.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ScrollView;

import java.util.concurrent.atomic.AtomicInteger;

public final class JellyAnimations {

    private static final long STAGGER_MS = 28L;
    private static final long DURATION_MS = 420L;
    private static final float START_SCALE = 0.94f;
    private static final float BOUNCE_SCALE = 1.035f;

    private JellyAnimations() {}

    /**
     * Applies a lightweight jelly-style entrance animation to the visible
     * content of every screen using BaseActivity.
     */
    public static void animateScreen(final View root) {
        if (root == null) return;

        root.post(() -> {
            root.setAlpha(0f);
            root.animate()
                    .alpha(1f)
                    .setDuration(260L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            if (root instanceof ViewGroup) {
                animateChildren((ViewGroup) root, new AtomicInteger(0));
            }
        });
    }

    private static void animateChildren(ViewGroup parent, AtomicInteger index) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child == null || child.getVisibility() != View.VISIBLE) continue;
            if (child instanceof ScrollView || child instanceof AdapterView) continue;

            final long delay = index.getAndIncrement() * STAGGER_MS;

            child.animate().cancel();
            child.setAlpha(0f);
            child.setTranslationY(18f);
            child.setScaleX(START_SCALE);
            child.setScaleY(START_SCALE);

            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(BOUNCE_SCALE)
                    .scaleY(BOUNCE_SCALE)
                    .setStartDelay(delay)
                    .setDuration(DURATION_MS)
                    .setInterpolator(new OvershootInterpolator(1.45f))
                    .withEndAction(() -> child.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150L)
                            .setInterpolator(new OvershootInterpolator(2.0f))
                            .start())
                    .start();

            if (child instanceof ViewGroup) {
                animateChildren((ViewGroup) child, index);
            }
        }
    }
}
