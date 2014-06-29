package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.io.IOException;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_settings, new SettingsFragment())
                    .commit();
        }
        getActionBar().setTitle(R.string.action_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference resetDataButton = findPreference(getActivity().getString(R.string.pref_key_reset_sample_data));
            resetDataButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder resetDataDialog = new AlertDialog.Builder(getActivity());
                    resetDataDialog.setMessage(R.string.reset_message);
                    resetDataDialog.setPositiveButton(R.string.reset_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                Recipe.writePreferences(getActivity(), Recipe.loadSampleData());
                                Toast.makeText(getActivity(), R.string.reset_data_confirmation, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    resetDataDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    resetDataDialog.create();
                    resetDataDialog.show();
                    return true;
                }
            });
        }
    }
}
