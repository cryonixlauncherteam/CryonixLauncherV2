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
            JellyAnimations.stagger((android.view.ViewGroup) content, 65L, 320L);
        }

        fadeText(view.findViewById(R.id.about_title), 80L);
        fadeText(view.findViewById(R.id.about_subtitle), 150L);
        fadeText(view.findViewById(R.id.about_fork_card), 230L);
        fadeText(view.findViewById(R.id.about_backend_card), 320L);
        fadeText(view.findViewById(R.id.about_team_card), 410L);
        fadeText(view.findViewById(R.id.about_notice), 500L);

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

    private void fadeText(View target, long delay) {
        if (target == null) return;
        target.setAlpha(0f);
        target.animate()
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(420L)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }
}
