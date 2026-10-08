package net.kdt.pojavlaunch.discord;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Method;

public final class DiscordRichPresence {
    private static final String TAG = "CryonixDiscord";
    private static final long APPLICATION_ID = 1557412253131743232L;
    private static final String PREFS = "cryonix_discord";
    private static final String KEY_CONNECTED = "connected";
    private static boolean initialized;

    private DiscordRichPresence() {}

    public static synchronized void initialize(Activity activity) {
        if (initialized || activity == null) return;
        try {
            Class<?> initClass = Class.forName("com.discord.socialsdk.DiscordSocialSdkInit");
            Method setEngineActivity = initClass.getMethod("setEngineActivity", Activity.class);
            setEngineActivity.invoke(null, activity);
            System.loadLibrary("cryonix_discord");
            nativeInitialize(APPLICATION_ID);
            initialized = true;
        } catch (Throwable e) {
            Log.w(TAG, "Discord SDK unavailable.", e);
        }
    }

    public static synchronized boolean connect(Context context) {
        if (context == null) return false;
        initialize(context instanceof Activity ? (Activity) context : null);
        if (!initialized) return false;
        try {
            nativeConnect();
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_CONNECTED, true).apply();
            return true;
        } catch (Throwable e) {
            Log.w(TAG, "Discord connection failed.", e);
            return false;
        }
    }

    public static boolean isConnected(Context context) {
        return context != null && context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONNECTED, false);
    }

    public static void update(String details, String state, String version) {
        if (!initialized) return;
        try { nativeUpdate(details, state, version); }
        catch (Throwable e) { Log.w(TAG, "Presence update failed.", e); }
    }

    public static void startGame(String instance, String version) {
        if (!initialized) return;
        try { nativeStartGame(instance, version); }
        catch (Throwable e) { Log.w(TAG, "Game presence update failed.", e); }
    }

    public static void clear() {
        if (!initialized) return;
        try { nativeClear(); } catch (Throwable ignored) {}
    }

    public static synchronized void shutdown() {
        if (!initialized) return;
        try { nativeShutdown(); }
        catch (Throwable e) { Log.w(TAG, "Discord shutdown failed.", e); }
        finally { initialized = false; }
    }

    private static native void nativeInitialize(long applicationId);
    private static native void nativeConnect();
    private static native void nativeUpdate(String details, String state, String version);
    private static native void nativeStartGame(String instance, String version);
    private static native void nativeClear();
    private static native void nativeShutdown();
}
