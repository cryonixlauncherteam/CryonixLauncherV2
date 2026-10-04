package net.kdt.pojavlaunch.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class DiscordRichPresenceManager {
    private static final String PREFS = "cryonix_discord";
    public static final String APPLICATION_ID = "1556312152409903177";
    public static final String REDIRECT_URI = "discord-" + APPLICATION_ID + ":/authorize/callback";

    private static final String KEY_ENABLED = "rich_presence_enabled";
    private static final String KEY_CONNECTED = "account_connected";
    private static final String KEY_USERNAME = "discord_username";
    private static final String KEY_ACCESS_TOKEN = "discord_access_token";
    private static final String KEY_REFRESH_TOKEN = "discord_refresh_token";
    private static final String KEY_VERIFIER = "oauth_verifier";
    private static final String KEY_STATE = "oauth_state";

    private DiscordRichPresenceManager() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static boolean isConnected(Context context) {
        return prefs(context).getBoolean(KEY_CONNECTED, false);
    }

    public static String getUsername(Context context) {
        return prefs(context).getString(KEY_USERNAME, "");
    }

    public static String getApplicationId() {
        return APPLICATION_ID;
    }

    public static void startAuthorization(Context context) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String verifier = base64Url(bytes);
            String challenge = base64Url(MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII)));

            byte[] stateBytes = new byte[24];
            random.nextBytes(stateBytes);
            String state = base64Url(stateBytes);

            prefs(context).edit()
                    .putString(KEY_VERIFIER, verifier)
                    .putString(KEY_STATE, state)
                    .apply();

            String url = "https://discord.com/oauth2/authorize"
                    + "?response_type=code"
                    + "&client_id=" + enc(APPLICATION_ID)
                    + "&redirect_uri=" + enc(REDIRECT_URI)
                    + "&scope=" + enc("identify")
                    + "&state=" + enc(state)
                    + "&code_challenge=" + enc(challenge)
                    + "&code_challenge_method=S256";

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start Discord authorization", e);
        }
    }

    public static void handleAuthorizationCallback(Context context, Uri uri,
                                                   AuthorizationCallback callback) {
        new Thread(() -> {
            try {
                String error = uri.getQueryParameter("error");
                if (error != null) {
                    callback.onComplete(false, "Discord authorization was cancelled or denied.");
                    return;
                }

                String code = uri.getQueryParameter("code");
                String returnedState = uri.getQueryParameter("state");
                String expectedState = prefs(context).getString(KEY_STATE, "");
                String verifier = prefs(context).getString(KEY_VERIFIER, "");

                if (code == null || code.isEmpty()
                        || expectedState.isEmpty()
                        || !expectedState.equals(returnedState)
                        || verifier.isEmpty()) {
                    callback.onComplete(false, "Invalid Discord authorization response.");
                    return;
                }

                String form = "client_id=" + enc(APPLICATION_ID)
                        + "&grant_type=authorization_code"
                        + "&code=" + enc(code)
                        + "&redirect_uri=" + enc(REDIRECT_URI)
                        + "&code_verifier=" + enc(verifier);

                JSONObject token = postForm(
                        "https://discord.com/api/v10/oauth2/token", form);
                String accessToken = token.optString("access_token", "");
                if (accessToken.isEmpty()) {
                    callback.onComplete(false, "Discord did not return an access token.");
                    return;
                }

                JSONObject user = getUser(accessToken);
                String username = user.optString("global_name",
                        user.optString("username", "Discord User"));

                prefs(context).edit()
                        .putBoolean(KEY_CONNECTED, true)
                        .putString(KEY_USERNAME, username)
                        .putString(KEY_ACCESS_TOKEN, accessToken)
                        .putString(KEY_REFRESH_TOKEN, token.optString("refresh_token", ""))
                        .remove(KEY_VERIFIER)
                        .remove(KEY_STATE)
                        .apply();

                callback.onComplete(true, username);
            } catch (Exception e) {
                callback.onComplete(false, "Discord connection failed: " + e.getMessage());
            }
        }).start();
    }

    public static void setConnected(Context context, String username) {
        prefs(context).edit()
                .putBoolean(KEY_CONNECTED, true)
                .putString(KEY_USERNAME, username == null ? "" : username)
                .apply();
    }

    public static void disconnect(Context context) {
        String token = prefs(context).getString(KEY_ACCESS_TOKEN, "");
        if (!token.isEmpty()) {
            new Thread(() -> {
                try {
                    String form = "token=" + enc(token);
                    postForm("https://discord.com/api/v10/oauth2/token/revoke", form);
                } catch (Exception ignored) {
                }
            }).start();
        }

        prefs(context).edit()
                .remove(KEY_CONNECTED)
                .remove(KEY_USERNAME)
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_VERIFIER)
                .remove(KEY_STATE)
                .apply();
    }

    public static void updatePresence(Context context, String details, String state) {
        if (!isEnabled(context)) return;
        // Live Android Rich Presence transport is provided by Discord Social SDK 1.10+.
    }

    public static void clearPresence(Context context) {
        // Live Android Rich Presence transport is provided by Discord Social SDK 1.10+.
    }

    private static JSONObject postForm(String endpoint, String form) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        byte[] body = form.getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body);
        }
        return new JSONObject(read(connection));
    }

    private static JSONObject getUser(String accessToken) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                new URL("https://discord.com/api/v10/users/@me").openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        return new JSONObject(read(connection));
    }

    private static String read(HttpURLConnection connection) throws Exception {
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) throw new IllegalStateException("HTTP " + code);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
            if (code >= 400) throw new IllegalStateException(result.toString());
            return result.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static String enc(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    private static String base64Url(byte[] value) {
        return android.util.Base64.encodeToString(value,
                android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING
                        | android.util.Base64.NO_WRAP);
    }

    public interface AuthorizationCallback {
        void onComplete(boolean success, String message);
    }
}
