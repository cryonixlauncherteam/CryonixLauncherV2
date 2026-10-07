package net.kdt.pojavlaunch.discord;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Small, optional Discord Social SDK bridge for Cryonix Launcher.
 * The launcher remains functional when the SDK is not packaged.
 */
public final class DiscordRichPresence {
    private static final String TAG = "CryonixDiscord";
    private static final long APPLICATION_ID = 1557412253131743232L;
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
            Log.w(TAG, "Discord Social SDK unavailable; continuing without Rich Presence.", e);
        }
    }

    public static void update(String details, String state, String version) {
        if (!initialized) return;
        try {
            nativeUpdate(details, state, version);
        } catch (Throwable e) {
            Log.w(TAG, "Failed to update Discord Rich Presence.", e);
        }
    }

    public static void clear() {
        if (!initialized) return;
        try {
            nativeClear();
        } catch (Throwable e) {
            Log.w(TAG, "Failed to clear Discord Rich Presence.", e);
        }
    }

    public static synchronized void shutdown() {
        if (!initialized) return;
        try {
            nativeShutdown();
        } catch (Throwable e) {
            Log.w(TAG, "Failed to shut down Discord Rich Presence.", e);
        } finally {
            initialized = false;
        }
    }

    private static native void nativeInitialize(long applicationId);
    private static native void nativeUpdate(String details, String state, String version);
    private static native void nativeClear();
    private static native void nativeShutdown();
}
