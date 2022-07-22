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
    private static final String TEAM_PREFERENCE_TEAM = "com.krhom.scoredisplay.TEAM_PREFERENCE_TEAM";
    private static final String TEAM_PREFERENCE_NAME = "com.krhom.scoredisplay.TEAM_PREFERENCE_NAME";
    private static final String TEAM_PREFERENCE_COLOR = "com.krhom.scoredisplay.TEAM_PREFERENCE_COLOR";
    private static final String TEAM_PREFERENCE_BRIGHTNESS = "com.krhom.scoredisplay.TEAM_PREFERENCE_BRIGHTNESS";

    public static class TeamSettingsIO
    {
        public Team team;
        public String teamName;
        public int teamColor;
        public int teamBrightness;
    }

    public static class TeamSettingsContract extends ActivityResultContract<TeamSettingsIO, TeamSettingsIO>
    {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull TeamSettingsIO input) {
            Intent intent = new Intent(context, TeamSettingsActivity.class);
            setIOOnIntent(intent, input);
            return intent;
        }

        @Override
        public TeamSettingsIO parseResult(int resultCode, @Nullable Intent result) {
            if (resultCode != Activity.RESULT_OK || result == null) {
                return null;
            }

            return getIOFromIntent(result);
        }
    }

    private static TeamSettingsIO getIOFromIntent(@NonNull Intent intent)
    {
        TeamSettingsIO output = new TeamSettingsIO();
        output.team = (Team) intent.getSerializableExtra(TEAM_PREFERENCE_TEAM);
        output.teamName = intent.getStringExtra(TEAM_PREFERENCE_NAME);
        output.teamColor = intent.getIntExtra(TEAM_PREFERENCE_COLOR, Color.parseColor("#FF0000"));
        output.teamBrightness = intent.getIntExtra(TEAM_PREFERENCE_BRIGHTNESS, 255);
        return output;
    }

    private static void setIOOnIntent(@NonNull Intent intent, @NonNull TeamSettingsIO input)
    {
        intent.putExtra(TEAM_PREFERENCE_TEAM, input.team);
        intent.putExtra(TEAM_PREFERENCE_NAME, input.teamName);
        intent.putExtra(TEAM_PREFERENCE_COLOR, input.teamColor);
        intent.putExtra(TEAM_PREFERENCE_BRIGHTNESS, input.teamBrightness);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_settings_activity);

        Intent intent = getIntent();
        TeamSettingsIO input = getIOFromIntent(intent);

        PreferenceFragmentCompat fragment = input.team == Team.TEAM1 ? new Team1SettingsFragment() : new Team2SettingsFragment();

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
            switch (input.team)
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
        preference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
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