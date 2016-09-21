package com.tabs.activity;

import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.schan.tabs.R;

/**
 * Created by schan on 9/10/16.
 */
public class CreatePostDialog extends DialogFragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.create_post_dialog, container, false);
        getDialog().setTitle("Create Post");

        setupPrivacyToggle(rootView);
        return rootView;
    }

    private void setupPrivacyToggle(View rootView) {
        final RadioGroup privacyToggle = (RadioGroup) rootView.findViewById(R.id.privacy_toggle);
        final RadioButton publicToggle = (RadioButton) rootView.findViewById(R.id.public_toggle);
        final RadioButton followersToggle = (RadioButton) rootView.findViewById(R.id.followers_toggle);

        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.public_toggle) {
                    publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    followersToggle.setTypeface(Typeface.SANS_SERIF);
                } else {
                    followersToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    publicToggle.setTypeface(Typeface.SANS_SERIF);

                }
            }
        });
    }


}
