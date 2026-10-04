package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.JellyAnimations;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.authenticator.accounts.Accounts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalLoginFragment extends Fragment {
    public static final String TAG = "LOCAL_LOGIN_FRAGMENT";

    private final Pattern mUsernameValidationPattern;
    private EditText mUsernameEditText;

    public LocalLoginFragment(){
        super(R.layout.fragment_local_login);
        mUsernameValidationPattern = Pattern.compile("^[a-zA-Z0-9_]*$");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        JellyAnimations.animateScreen(view);
        JellyAnimations.stagger((android.view.ViewGroup) view.findViewById(R.id.login_menu), 70L, 260L);
        JellyAnimations.stagger((android.view.ViewGroup) view.findViewById(R.id.login_header), 55L, 220L);
        JellyAnimations.stagger((android.view.ViewGroup) view.findViewById(R.id.login_form), 55L, 220L);
        mUsernameEditText = view.findViewById(R.id.login_edit_email);
        view.findViewById(R.id.login_button).setOnClickListener(v -> {
            if(!checkEditText()) {
                Context context = v.getContext();
                Tools.dialog(context, context.getString(R.string.local_login_bad_username_title), context.getString(R.string.local_login_bad_username_text));
                return;
            }

            final String username = mUsernameEditText.getText().toString().trim();
            try {
                // A local account must be persisted and selected before returning home.
                // The old flow only populated a temporary extra, so launching immediately
                // afterwards saw no saved account and opened account creation again.
                Accounts accountStore = Accounts.load();
                net.kdt.pojavlaunch.authenticator.accounts.Account account = null;
                for (net.kdt.pojavlaunch.authenticator.accounts.Account existing : accountStore.accounts) {
                    if (existing.isLocal() && username.equals(existing.username)) {
                        account = existing;
                        break;
                    }
                }

                if (account == null) {
                    final String finalUsername = username;
                    account = Accounts.create(created -> {
                        created.username = finalUsername;
                        created.accessToken = "0";
                        created.refreshToken = "0";
                        created.profileId = java.util.UUID.randomUUID().toString();
                        created.isMicrosoft = false;
                    });
                }

                Accounts.setCurrent(account);
                ExtraCore.setValue(ExtraConstants.MOJANG_LOGIN_TODO, new String[]{
                        username, "" });
                ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
                Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
            } catch (Exception e) {
                Tools.dialog(requireContext(),
                        "Account could not be saved",
                        "Cryonix could not save the local account. Please try again.");
            }
        });
    }


    /** @return Whether the mail (and password) text are eligible to make an auth request  */
    private boolean checkEditText(){

        String text = mUsernameEditText.getText().toString();

        Matcher matcher = mUsernameValidationPattern.matcher(text);
        return !(text.isEmpty()
                || text.length() < 3
                || text.length() > 16
                || !matcher.find()
        );
    }
}
