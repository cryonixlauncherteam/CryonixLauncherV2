package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.JellyAnimations;

public class AboutCryonixFragment extends Fragment {
    public static final String TAG = "AboutCryonixFragment";

    public AboutCryonixFragment() {
        super(R.layout.fragment_about_cryonix);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View content = view.findViewById(R.id.about_content);
        if (content instanceof android.view.ViewGroup) {
            JellyAnimations.stagger((android.view.ViewGroup) content, 55L, 280L);
        } else {
            JellyAnimations.animateScreen(view);
        }

        ImageButton back = view.findViewById(R.id.about_back);
        if (back != null) {
            JellyAnimations.pressFeedback(back);
            back.setOnClickListener(v -> {
                JellyAnimations.popIn(v, 0L, 180L);
                Tools.backToMainMenu(requireActivity());
            });
        }

        View root = view.findViewById(R.id.about_root);
        if (root != null) JellyAnimations.attachTouchFeedback(root);
    }
}
