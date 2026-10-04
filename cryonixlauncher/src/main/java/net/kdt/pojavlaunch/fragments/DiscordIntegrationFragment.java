package net.kdt.pojavlaunch.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DiscordRichPresenceManager;
import net.kdt.pojavlaunch.utils.JellyAnimations;

public class DiscordIntegrationFragment extends Fragment {
    public static final String TAG = "DiscordIntegrationFragment";

    public DiscordIntegrationFragment() {
        super(R.layout.fragment_discord_integration);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView status = view.findViewById(R.id.discord_status);
        TextView account = view.findViewById(R.id.discord_account);
        Button connect = view.findViewById(R.id.discord_connect);
        Button disconnect = view.findViewById(R.id.discord_disconnect);
        Button presence = view.findViewById(R.id.discord_presence);

        View back = view.findViewById(R.id.discord_back);
        if (back != null) {
            JellyAnimations.pressFeedback(back);
            back.setOnClickListener(v -> Tools.backToMainMenu(requireActivity()));
        }

        refreshStatus(status, account, connect, disconnect, presence);

        if (connect != null) {
            JellyAnimations.pressFeedback(connect);
            connect.setOnClickListener(v -> {
                JellyAnimations.popIn(v, 0L, 180L);
                showSetupMessage();
            });
        }

        if (disconnect != null) {
            JellyAnimations.pressFeedback(disconnect);
            disconnect.setOnClickListener(v -> {
                DiscordRichPresenceManager.disconnect(requireContext());
                DiscordRichPresenceManager.clearPresence(requireContext());
                refreshStatus(status, account, connect, disconnect, presence);
            });
        }

        if (presence != null) {
            JellyAnimations.pressFeedback(presence);
            presence.setOnClickListener(v -> {
                boolean enabled = !DiscordRichPresenceManager.isEnabled(requireContext());
                DiscordRichPresenceManager.setEnabled(requireContext(), enabled);
                if (enabled) {
                    DiscordRichPresenceManager.updatePresence(
                            requireContext(), "Using Cryonix Launcher", "Launcher");
                } else {
                    DiscordRichPresenceManager.clearPresence(requireContext());
                }
                refreshStatus(status, account, connect, disconnect, presence);
            });
        }

        JellyAnimations.animateScreen(view);
        JellyAnimations.attachTouchFeedback(view);
    }

    private void refreshStatus(TextView status, TextView account, Button connect,
                               Button disconnect, Button presence) {
        Context context = requireContext();
        boolean connected = DiscordRichPresenceManager.isConnected(context);

        if (connected) {
            status.setText("Connected");
            status.setTextColor(Color.rgb(120, 210, 150));
            String username = DiscordRichPresenceManager.getUsername(context);
            account.setText(username.isEmpty() ? "Discord account linked" : username);
            if (connect != null) connect.setVisibility(View.GONE);
            if (disconnect != null) disconnect.setVisibility(View.VISIBLE);
            if (presence != null) {
                presence.setText(DiscordRichPresenceManager.isEnabled(context)
                        ? "Rich Presence: On" : "Rich Presence: Off");
            }
        } else {
            status.setText("Not connected");
            status.setTextColor(Color.rgb(155, 164, 174));
            account.setText("Connect your Discord account to Cryonix Launcher");
            if (connect != null) connect.setVisibility(View.VISIBLE);
            if (disconnect != null) disconnect.setVisibility(View.GONE);
            if (presence != null) presence.setText("Rich Presence: Off");
        }
    }

    private void showSetupMessage() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Connect Discord")
                .setMessage(
                        "Cryonix will use Discord's official mobile account-linking and Rich Presence integration. " +
                        "The project still needs the Cryonix Discord Application ID and the official Discord Social SDK AAR. " +
                        "No Discord password or user token should ever be entered into the launcher.")
                .setPositiveButton("OK", null)
                .show();
    }
}
