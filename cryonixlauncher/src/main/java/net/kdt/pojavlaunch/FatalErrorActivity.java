package net.kdt.pojavlaunch;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import git.artdeell.mojo.R;

/**
 * Crash hand-off activity.
 *
 * The old crash dialog is intentionally gone. A real fatal crash now opens the
 * dedicated Crash AI screen with the exact stack trace, so the user sees the
 * cause, log and actionable diagnostic steps in one place.
 */
public class FatalErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        Throwable throwable = (Throwable) extras.getSerializable("throwable");
        final String stackTrace = throwable != null ? Tools.printToString(throwable) : "<null crash throwable>";
        final String savePath = extras.getString("savePath");

        Intent intent = new Intent(this, LauncherActivity.class);
        intent.putExtra("open_ai_assist", true);
        intent.putExtra("ai_error_text", stackTrace);
        if (savePath != null) {
            intent.putExtra("ai_log_path", savePath);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } finally {
            finish();
        }
    }

    /**
     * Kept as the crash-handler entry point used by PojavApplication.
     */
    public static void showError(Context ctx, String savePath, boolean storageAllow, Throwable th) {
        Intent fatalErrorIntent = new Intent(ctx, FatalErrorActivity.class);
        fatalErrorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        fatalErrorIntent.putExtra("throwable", th);
        fatalErrorIntent.putExtra("savePath", savePath);
        fatalErrorIntent.putExtra("storageAllow", storageAllow);
        ctx.startActivity(fatalErrorIntent);
    }
}
