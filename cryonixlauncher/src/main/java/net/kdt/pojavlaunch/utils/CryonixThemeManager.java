package net.kdt.pojavlaunch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

public final class CryonixThemeManager {
    private static final String PREFS = "cryonix_theme";
    private static final String KEY_THEME = "theme";
    // Cryonix Launcher uses one stable visual palette. Do not let an old/stale
    // theme preference change colours after the app is recreated.
    private static final int DEFAULT_THEME = 0;
    public static final int BLUE = 0;
    public static final int GRAPHITE = 1;
    public static final int EMERALD = 2;
    public static final int VIOLET = 3;

    private static final Theme[] THEMES = {
        new Theme("Cryonix Blue", "#050B16", "#0E1C38", "#14243D", "#3D93FF", "#FFFFFF", "#9FB5D8", "#243D6B"),
        new Theme("Graphite", "#0A0C0F", "#171A1F", "#24282E", "#A9B1BC", "#F4F5F6", "#AEB5BE", "#3A414A"),
        new Theme("Emerald", "#06100D", "#0C201A", "#12352B", "#35C78A", "#F4FFFA", "#9AC7B5", "#285847"),
        new Theme("Violet", "#0C0814", "#1A1128", "#291A3D", "#9B70FF", "#FBF9FF", "#B9A9D8", "#49346C")
    };

    private CryonixThemeManager() {}

    public static Theme current(Context context) {
        // Keep the launcher palette deterministic across process/activity restarts.
        // Older builds stored selectable theme indexes; those values must not alter
        // the current Cryonix Launcher UI unless a future theme picker explicitly
        // opts back into them.
        return THEMES[DEFAULT_THEME];
    }

    public static int currentIndex(Context context) {
        return DEFAULT_THEME;
    }

    public static String currentName(Context context) {
        return current(context).name;
    }

    public static void setTheme(Context context, int index) {
        // Kept for API compatibility. Cryonix currently has a single fixed palette.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME, DEFAULT_THEME).commit();
    }

    public static String[] names() {
        String[] names = new String[THEMES.length];
        for (int i = 0; i < THEMES.length; i++) names[i] = THEMES[i].name;
        return names;
    }

    public static void applyDialog(android.app.Dialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        Theme theme = current(dialog.getContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(theme.card));
        applyView(dialog.getWindow().getDecorView(), theme, true);
    }

    public static void apply(Activity activity) {
        if (activity == null) return;
        Theme theme = current(activity);
        activity.getWindow().setStatusBarColor(theme.statusBar);
        activity.getWindow().setNavigationBarColor(theme.background);
        applyView(activity.getWindow().getDecorView(), theme, true);
    }

    private static void applyView(View view, Theme theme, boolean root) {
        if (view == null) return;

        if (root) {
            view.setBackgroundColor(theme.background);
        } else if (!(view instanceof ImageButton) && !(view instanceof android.widget.ImageView)
                && !(view instanceof ProgressBar)) {
            Drawable bg = view.getBackground();
            if (bg instanceof ColorDrawable) {
                view.setBackgroundTintList(ColorStateList.valueOf(theme.card));
            } else if (bg != null) {
                ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(theme.card));
            }
        }

        if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextColor(theme.primary);
            if (view instanceof Button) {
                Button button = (Button) view;
                button.setTextColor(theme.accent);
                boolean primaryButton = button.getClass().getSimpleName().contains("MineButton");
                if (button.getBackground() != null) {
                    ViewCompat.setBackgroundTintList(button,
                            ColorStateList.valueOf(primaryButton ? theme.accent : theme.card));
                }
            } else if (view instanceof EditText) {
                text.setTextColor(theme.primary);
                ((EditText) view).setHintTextColor(theme.secondary);
                ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(theme.field));
            }
        }

        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            checkBox.setTextColor(theme.primary);
            checkBox.setButtonTintList(new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{theme.accent, theme.secondary}
            ));
        }

        if (view instanceof ImageButton) {
            ((ImageButton) view).setColorFilter(theme.accent);
        } else if (view instanceof android.widget.ImageView) {
            android.widget.ImageView imageView = (android.widget.ImageView) view;
            if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
                imageView.setImageTintList(ColorStateList.valueOf(theme.accent));
            }
        }

        if (view instanceof ProgressBar) {
            ((ProgressBar) view).setProgressTintList(ColorStateList.valueOf(theme.accent));
            ((ProgressBar) view).setIndeterminateTintList(ColorStateList.valueOf(theme.accent));
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyView(group.getChildAt(i), theme, false);
            }
        }
    }

    public static final class Theme {
        public final String name;
        public final int background;
        public final int card;
        public final int field;
        public final int accent;
        public final int primary;
        public final int secondary;
        public final int divider;
        public final int statusBar;

        Theme(String name, String background, String card, String field,
              String accent, String primary, String secondary, String divider) {
            this.name = name;
            this.background = Color.parseColor(background);
            this.card = Color.parseColor(card);
            this.field = Color.parseColor(field);
            this.accent = Color.parseColor(accent);
            this.primary = Color.parseColor(primary);
            this.secondary = Color.parseColor(secondary);
            this.divider = Color.parseColor(divider);
            this.statusBar = darken(this.background, 0.65f);
        }

        private static int darken(int color, float factor) {
            return Color.rgb(
                    (int)(Color.red(color) * factor),
                    (int)(Color.green(color) * factor),
                    (int)(Color.blue(color) * factor)
            );
        }
    }
}