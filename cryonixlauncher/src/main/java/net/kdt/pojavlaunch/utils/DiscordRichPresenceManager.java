package net.kdt.pojavlaunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Cryonix Discord integration state.
 *
 * The actual Discord Rich Presence transport is intentionally kept behind this
 * small bridge. Discord's current Android Rich Presence implementation uses
 * the official Discord Social SDK (1.10+); the SDK package and Cryonix's
 * Discord Application ID must be supplied by the project owner.
 */
public final class DiscordRichPresenceManager {
    private static final String PREFS = "cryonix_discord";
    private static final String KEY_ENABLED = "rich_presence_enabled";
    private static final String KEY_CONNECTED = "account_connected";
    private static final String KEY_USERNAME = "discord_username";

    private DiscordRichPresenceManager() {}

    public static boolean isEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static boolean isConnected(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONNECTED, false);
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, "");
    }

    public static void setConnected(Context context, String username) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_CONNECTED, true);
        editor.putString(KEY_USERNAME, username == null ? "" : username);
        editor.apply();
    }

    public static void disconnect(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .remove(KEY_CONNECTED)
                .remove(KEY_USERNAME)
                .apply();
    }

    /**
     * Placeholder transport hook. Do not emulate Discord user RPC or self-bot
     * traffic here. Live Android Rich Presence must use Discord's official
     * Social SDK / RPC integration.
     */
    public static void updatePresence(Context context, String details, String state) {
        if (!isEnabled(context)) return;
        // Wired when the official Discord Social SDK AAR is supplied.
    }

    public static void clearPresence(Context context) {
        // Wired when the official Discord Social SDK AAR is supplied.
    }
}
