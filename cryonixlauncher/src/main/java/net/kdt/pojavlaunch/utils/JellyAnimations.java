package net.kdt.pojavlaunch.utils;

import android.animation.TimeInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ScrollView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Cryonix Launcher bounce-only motion helper.
 *
 * All previous jelly, spring, wobble, slide, fade and cascade motion has been
 * removed. Existing callers are kept source-compatible, but every motion now
 * resolves to the same short bounce interaction.
 */
public final class JellyAnimations {
    private static final TimeInterpolator BOUNCE = new OvershootInterpolator(1.35f);
    private static final Map<View, Boolean> PRESS_ATTACHED = new WeakHashMap<>();

    private JellyAnimations() {}

    public static void animateScreen(View root) { bounce(root, 260L); }
    public static void revealScreen(View root) { bounce(root, 260L); }
    public static void animateDialog(View root) { bounce(root, 260L); }
    public static void pressFeedback(View... views) {
        if (views == null) return;
        for (View view : views) attachPress(view);
    }
    public static void attachTouchFeedback(View root) {
        if (root == null) return;
        walkClickable(root);
    }
    public static void springScale(View view, float targetScale) { bounce(view, 260L); }
    public static void slideIn(View view, int direction, long startDelay) {
        bounce(view, 260L + Math.max(0L, startDelay));
    }
    public static void stagger(ViewGroup container) { stagger(container, 0L, 260L); }
    public static void stagger(ViewGroup container, long stepDelay, long duration) {
        if (container == null || shouldSkip(container)) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child != null && child.getVisibility() == View.VISIBLE) {
                bounce(child, Math.max(120L, duration));
            }
        }
    }
    public static void popIn(View view, long startDelay) { bounce(view, 260L + Math.max(0L, startDelay)); }
    public static void popIn(View view, long startDelay, long duration) {
        bounce(view, Math.max(120L, duration) + Math.max(0L, startDelay));
    }

    private static void bounce(View view, long duration) {
        if (view == null || view.getVisibility() != View.VISIBLE) return;
        view.animate().cancel();
        view.setScaleX(0.94f);
        view.setScaleY(0.94f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(Math.max(120L, duration))
                .setInterpolator(BOUNCE)
                .start();
    }

    private static void attachPress(final View view) {
        if (view == null || !view.isClickable()) return;
        synchronized (PRESS_ATTACHED) {
            if (PRESS_ATTACHED.containsKey(view)) return;
            PRESS_ATTACHED.put(view, Boolean.TRUE);
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().cancel();
                        v.setScaleX(0.94f);
                        v.setScaleY(0.94f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        bounce(v, 260L);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private static void walkClickable(View view) {
        if (view == null || shouldSkip(view)) return;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            if (group.isClickable() && group.hasOnClickListeners()) {
                attachPress(group);
                return;
            }
            for (int i = 0; i < group.getChildCount(); i++) {
                walkClickable(group.getChildAt(i));
            }
        } else if (view.isClickable() && view.hasOnClickListeners()) {
            attachPress(view);
        }
    }

    private static boolean shouldSkip(View view) {
        return view instanceof RecyclerView
                || view instanceof AdapterView
                || view instanceof ScrollView
                || view instanceof android.widget.HorizontalScrollView
                || view instanceof android.widget.SeekBar
                || view instanceof android.widget.EditText;
    }
}
