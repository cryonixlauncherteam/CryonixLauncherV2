package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.ProgressLayout;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.discord.DiscordRichPresence;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.JellyAnimations;

public class SelectAuthFragment extends Fragment {
    public static final String TAG = "AUTH_SELECT_FRAGMENT";

    public SelectAuthFragment() {
        super(R.layout.fragment_select_auth_method);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        JellyAnimations.animateScreen(view);
        JellyAnimations.stagger((android.view.ViewGroup) view.findViewById(R.id.auth_options), 80L, 260L);
        JellyAnimations.stagger((android.view.ViewGroup) view.findViewById(R.id.auth_header), 55L, 220L);

        Button mMicrosoftButton = view.findViewById(R.id.button_microsoft_authentication);
        Button mLocalButton = view.findViewById(R.id.button_local_authentication);
        Button mElyByButton = view.findViewById(R.id.button_elyby_authentication);
        Button mDiscordButton = view.findViewById(R.id.button_discord_authentication);

        mMicrosoftButton.setOnClickListener(v ->
                launchAuthFragment(MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG));
        mLocalButton.setOnClickListener(v ->
                launchAuthFragment(LocalLoginFragment.class, LocalLoginFragment.TAG));
        mElyByButton.setOnClickListener(v ->
                launchAuthFragment(ElyByLoginFragment.class, ElyByLoginFragment.TAG));

        mDiscordButton.setOnClickListener(v -> {
            boolean connected = DiscordRichPresence.connect(requireActivity());
            if (connected) {
                Toast.makeText(requireContext(),
                        "Discord connected. Rich Presence is enabled.",
                        Toast.LENGTH_SHORT).show();
                Tools.backToMainMenu(requireActivity());
            } else {
                Toast.makeText(requireContext(),
                        "Discord SDK is not available in this build.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchAuthFragment(Class<? extends Fragment> fragmentClass, String fragmentTag) {
        if (ProgressKeeper.hasProgressKey(ProgressLayout.AUTHENTICATE)) {
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_SHORT).show();
            return;
        }
        Tools.swapFragment(requireActivity(), fragmentClass, fragmentTag, null);
    }
}
