package com.krhom.scoredisplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.skydoves.colorpickerpreference.ColorPickerPreference;
import com.skydoves.colorpickerview.listeners.ColorPickerViewListener;

public class TeamSettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_settings_activity);

        Intent intent = getIntent();
        Team team = (Team) intent.getSerializableExtra("team");

        PreferenceFragmentCompat fragment = team == Team.TEAM1 ? new Team1SettingsFragment() : new Team2SettingsFragment();

        if (savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, fragment)
                    .commit();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            switch (team)
            {
                case TEAM1:
                    actionBar.setTitle(getResources().getString(R.string.team1_preferences_header));
                    break;
                case TEAM2:
                    actionBar.setTitle(getResources().getString(R.string.team2_preferences_header));
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static void addTeamNameInputFilters(EditTextPreference preference)
    {
        preference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener()
        {
            @Override
            public void onBindEditText(@NonNull EditText editText)
            {
                editText.selectAll(); // select all text
                int maxLength = 6;

                InputFilter digitFilter = new InputFilter()
                {
                    public CharSequence filter(CharSequence src, int start,
                                               int end, Spanned dst, int dstart, int dend)
                    {
                        if (src.toString().matches("[a-zA-Z0-9 ]+"))
                        {
                            return src;
                        }
                        return "";
                    }
                };

                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength), new InputFilter.AllCaps(), digitFilter}); // set maxLength to 2
            }
        });
    }

    public static class Team1SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.team1_preferences, rootKey);

            EditTextPreference preference = findPreference("team1Name");
            addTeamNameInputFilters(preference);
        }
    }

    public static class Team2SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.team2_preferences, rootKey);

            EditTextPreference preference = findPreference("team2Name");
            addTeamNameInputFilters(preference);
        }
    }
}