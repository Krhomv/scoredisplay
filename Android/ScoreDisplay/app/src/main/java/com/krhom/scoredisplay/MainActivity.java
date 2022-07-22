package com.krhom.scoredisplay;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

enum Team
{
    TEAM1,
    TEAM2
}

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener,
                   View.OnFocusChangeListener,
                   TextView.OnEditorActionListener,
                   View.OnTouchListener

{


    private static final int MAX_SCORE = 99;
    private static final int RESET_TIME_REQUIRED_MS = 1575;
    private static final int TEAM1_PREFERENCE_ACTIVITY = 1;
    private static final int TEAM2_PREFERENCE_ACTIVITY = 2;

    private int m_team1Score = 0;
    private int m_team1Colour = Color.parseColor("#CF0000");
    private int m_team1Brightness = 255;

    private int m_team2Score = 0;
    private int m_team2Colour = Color.parseColor("#CF0000");
    private int m_team2Brightness = 255;

    private Button m_team1Name;
    private Button m_team1ScoreUp;
    private Button m_team1ScoreDown;
    private Button m_team1ScoreReset;
    private EditText m_team1ScoreText;
    private TextView m_team1ScoreBackground;

    private Button m_team2Name;
    private Button m_team2ScoreUp;
    private Button m_team2ScoreDown;
    private Button m_team2ScoreReset;
    private TextView m_team2ScoreBackground;

    private EditText m_team2ScoreText;

    private Timer m_team1Timer;
    private Timer m_team2Timer;

    private ActivityResultLauncher<TeamSettingsActivity.TeamSettingsIO> m_teamSettingsActivityLauncher;

    private SharedPreferences.OnSharedPreferenceChangeListener m_preferenceChangelistener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_team1Name = (Button) findViewById(R.id.team1Name);
        m_team1ScoreUp = (Button) findViewById(R.id.team1ScoreUp);
        m_team1ScoreDown = (Button) findViewById(R.id.team1ScoreDown);
        m_team1ScoreReset = (Button) findViewById(R.id.team1ScoreReset);
        m_team1ScoreText = (EditText) findViewById(R.id.team1Score);
        m_team1ScoreBackground = (TextView) findViewById(R.id.team1ScoreBackground);

        m_team2Name = (Button) findViewById(R.id.team2Name);
        m_team2ScoreUp = (Button) findViewById(R.id.team2ScoreUp);
        m_team2ScoreDown = (Button) findViewById(R.id.team2ScoreDown);
        m_team2ScoreReset = (Button) findViewById(R.id.team2ScoreReset);
        m_team2ScoreText = (EditText) findViewById(R.id.team2Score);
        m_team2ScoreBackground = (TextView) findViewById(R.id.team2ScoreBackground);

        m_team1Name.setOnClickListener(this);
        m_team1ScoreUp.setOnClickListener(this);
        m_team1ScoreDown.setOnClickListener(this);
        m_team1ScoreReset.setOnTouchListener(this);
        m_team2Name.setOnClickListener(this);
        m_team2ScoreUp.setOnClickListener(this);
        m_team2ScoreDown.setOnClickListener(this);
        m_team2ScoreReset.setOnTouchListener(this);

        m_team1ScoreText.setOnFocusChangeListener(this);
        m_team1ScoreText.setOnEditorActionListener(this);
        m_team2ScoreText.setOnFocusChangeListener(this);
        m_team2ScoreText.setOnEditorActionListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.team1_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.team2_preferences, false);

        m_teamSettingsActivityLauncher = registerForActivityResult(new TeamSettingsActivity.TeamSettingsContract(),
                new ActivityResultCallback<TeamSettingsActivity.TeamSettingsIO>() {
                    @Override
                    public void onActivityResult(TeamSettingsActivity.TeamSettingsIO result) {

                    }
                });

        getSupportActionBar().hide();

        //Loads Shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Setup a shared preference listener for hpwAddress and restart transport
        m_preferenceChangelistener = new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
            {
                // TODO
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(m_preferenceChangelistener);
    }

    @Override
    public void onClick(View v)
    {
        int oldTeam1Score = m_team1Score;
        int oldTeam2Score = m_team2Score;
        
        switch (v.getId())
        {
            case R.id.team1Name:
                showTeamSetupDialog(Team.TEAM1);
                break;
            case R.id.team1ScoreUp:
                m_team1Score = Math.min(m_team1Score + 1, MAX_SCORE);
                break;
            case R.id.team1ScoreDown:
                m_team1Score = Math.max(m_team1Score - 1, 0);
                break;
            case R.id.team2Name:
                showTeamSetupDialog(Team.TEAM2);
                break;
            case R.id.team2ScoreUp:
                m_team2Score = Math.min(m_team2Score + 1, MAX_SCORE);
                break;
            case R.id.team2ScoreDown:
                m_team2Score = Math.max(m_team2Score - 1, 0);
                break;
        }

        if (m_team1Score != oldTeam1Score)
        {
            refreshTeam1ScoreDisplay();
        }

        if (m_team2Score != oldTeam2Score)
        {
            refreshTeam2ScoreDisplay();
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            switch (view.getId())
            {
                case R.id.team1Score:
                {
                    String scoreString = m_team1ScoreText.getText().toString();
                    m_team1Score = Integer.parseInt(scoreString);
                    String scoreStringLeadingZeros = String.format("%02d", m_team1Score);

                    if (!scoreString.equals(scoreStringLeadingZeros))
                    {
                        m_team1ScoreText.setText(scoreStringLeadingZeros);
                    }
                }
                break;

                case R.id.team2Score:
                    String scoreString = m_team2ScoreText.getText().toString();
                    m_team2Score = Integer.parseInt(scoreString);
                    String scoreStringLeadingZeros = String.format("%02d", m_team2Score);

                    if (!scoreString.equals(scoreStringLeadingZeros))
                    {
                        m_team2ScoreText.setText(scoreStringLeadingZeros);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            View view = getCurrentFocus();
            if (view != null && view instanceof EditText)
            {
                Rect r = new Rect();
                view.getGlobalVisibleRect(r);
                int rawX = (int)ev.getRawX();
                int rawY = (int)ev.getRawY();
                if (!r.contains(rawX, rawY))
                {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
        // Defocus when the ok button is pressed when editing with the on-screen keyboard
        if (keyCode == KeyEvent.KEYCODE_ENDCALL)
        {
            textView.clearFocus();
        }
        return false;
    }

    public void showTeamSetupDialog(Team team)
    {
        TeamSettingsActivity.TeamSettingsIO args = new TeamSettingsActivity.TeamSettingsIO();
        args.team = team;
        switch (team)
        {
            case TEAM1:
                args.teamName = m_team1Name.getText().toString();
                args.teamColor = m_team1Colour;
                args.teamBrightness = m_team1Brightness;
                break;
            case TEAM2:
                args.teamName = m_team2Name.getText().toString();
                args.teamColor = m_team2Colour;
                args.teamBrightness = m_team2Brightness;
                break;
        }

        m_teamSettingsActivityLauncher.launch(args);

//        m_teamSetupDialogFragment.team = team;
//        switch (team)
//        {
//            case TEAM1:
//                m_teamSetupDialogFragment.title = "Setup Team 1";
//                m_teamSetupDialogFragment.teamName = m_team1Name.getText().toString();
//                m_teamSetupDialogFragment.teamColour = m_team1Colour;
//                break;
//            case TEAM2:
//                m_teamSetupDialogFragment.title = "Setup Team 2";
//                m_teamSetupDialogFragment.teamName = m_team2Name.getText().toString();
//                m_teamSetupDialogFragment.teamColour = m_team2Colour;
//                break;
//        }
//
//        m_teamSetupDialogFragment.show(getSupportFragmentManager(), TeamSetupDialogFragment.TAG);
    }

    private void setTeam1Colour(int colour)
    {
        m_team1Name.setTextColor(colour);
        m_team1Name.setShadowLayer(12, 0, 0, colour);
        m_team1ScoreText.setTextColor(colour);
        m_team1ScoreText.setShadowLayer(12, 0, 0, colour);
        m_team1ScoreUp.setTextColor(colour);
        m_team1ScoreUp.setShadowLayer(12, 0, 0, colour);
        m_team1ScoreDown.setTextColor(colour);
        m_team1ScoreDown.setShadowLayer(12, 0, 0, colour);
        m_team1ScoreReset.setTextColor(colour);
        m_team1ScoreReset.setShadowLayer(12, 0, 0, colour);

        int transparent = Color.argb(25, Color.red(colour), Color.green(colour), Color.blue(colour));
        m_team1ScoreBackground.setTextColor(transparent);
    }

    private void setTeam2Colour(int colour)
    {
        m_team2Name.setTextColor(colour);
        m_team2Name.setShadowLayer(12, 0, 0, colour);
        m_team2ScoreText.setTextColor(colour);
        m_team2ScoreText.setShadowLayer(12, 0, 0, colour);
        m_team2ScoreUp.setTextColor(colour);
        m_team2ScoreUp.setShadowLayer(12, 0, 0, colour);
        m_team2ScoreDown.setTextColor(colour);
        m_team2ScoreDown.setShadowLayer(12, 0, 0, colour);
        m_team2ScoreReset.setTextColor(colour);
        m_team2ScoreReset.setShadowLayer(12, 0, 0, colour);

        int transparent = Color.argb(25, Color.red(colour), Color.green(colour), Color.blue(colour));
        m_team2ScoreBackground.setTextColor(transparent);
    }

    private void refreshTeam1ScoreDisplay()
    {
        m_team1ScoreText.setText(String.format("%02d", m_team1Score));
    }

    private void refreshTeam2ScoreDisplay()
    {
        m_team2ScoreText.setText(String.format("%02d", m_team2Score));
    }

    private Animation getResetBlinkingAnimation()
    {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150);
        anim.setStartOffset(75);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        return anim;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (motionEvent == null)
            return false;

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                switch (view.getId())
                {
                    case R.id.team1ScoreReset:
                    {
                        Animation anim = getResetBlinkingAnimation();
                        m_team1ScoreText.startAnimation(anim);

                        m_team1Timer = new Timer();
                        m_team1Timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                m_team1Score = 0;
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        m_team1ScoreText.setAnimation(null);
                                        refreshTeam1ScoreDisplay();
                                    }
                                });
                            }
                        }, RESET_TIME_REQUIRED_MS);
                        break;
                    }
                    case R.id.team2ScoreReset:
                    {
                        Animation anim = getResetBlinkingAnimation();
                        m_team2ScoreText.startAnimation(anim);

                        m_team2Timer = new Timer();
                        m_team2Timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                m_team2Score = 0;
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        m_team2ScoreText.setAnimation(null);
                                        refreshTeam2ScoreDisplay();
                                    }
                                });
                            }
                        }, RESET_TIME_REQUIRED_MS);
                        break;
                    }
                }


                break;
            case MotionEvent.ACTION_UP:
            {
                switch (view.getId())
                {
                    case R.id.team1ScoreReset:
                        m_team1ScoreText.setAnimation(null);
                        m_team1Timer.cancel();
                        break;
                    case R.id.team2ScoreReset:
                        m_team2ScoreText.setAnimation(null);
                        m_team2Timer.cancel();
                        break;
                }
                break;
            }
        }
        return false;
    }
}