package net.kdt.pojavlaunch;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import git.artdeell.mojo.R;

/**
 * Non-recursive fatal crash screen.
 *
 * A previous implementation relaunched LauncherActivity after a crash. If the
 * same startup problem happened again, that created an endless open -> crash ->
 * relaunch loop. This screen now stays isolated from LauncherActivity.
 */
public class FatalErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView view = new TextView(this);
        view.setTextIsSelectable(true);
        view.setTextSize(14);
        view.setPadding(32, 32, 32, 32);

        Bundle extras = getIntent().getExtras();
        Throwable throwable = extras == null ? null : (Throwable) extras.getSerializable("throwable");
        String stackTrace = throwable != null
                ? Tools.printToString(throwable)
                : "<unknown launcher crash>";

        view.setText("Cryonix Launcher crashed during startup.\n\n" + stackTrace);
        setContentView(view);
    }

    /**
     * Kept as the crash-handler entry point used by PojavApplication.
     */
    public static void showError(android.content.Context ctx, String savePath,
                                 boolean storageAllow, Throwable th) {
        android.content.Intent intent = new android.content.Intent(ctx, FatalErrorActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("throwable", th);
        intent.putExtra("savePath", savePath);
        intent.putExtra("storageAllow", storageAllow);
        ctx.startActivity(intent);
    }
}
