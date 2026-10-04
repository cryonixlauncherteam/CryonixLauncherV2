package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import net.kdt.pojavlaunch.utils.DiscordRichPresenceManager;

public class DiscordAuthActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (uri == null || !DiscordRichPresenceManager.REDIRECT_URI.equals(
                uri.getScheme() + "://" + uri.getHost() + uri.getPath())) {
            finish();
            return;
        }

        DiscordRichPresenceManager.handleAuthorizationCallback(this, uri, (success, message) -> {
            runOnUiThread(() -> {
                Toast.makeText(this,
                        success ? "Discord connected: " + message : message,
                        Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}
