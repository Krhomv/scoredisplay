package com.krhom.scoredisplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;

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
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int MAX_SCORE = 99;
    private static final int RESET_TIME_REQUIRED_MS = 1575;
    private static final int TEAM1_PREFERENCE_ACTIVITY = 1;
    private static final int TEAM2_PREFERENCE_ACTIVITY = 2;
    private static final int BT_PERMISSION_REQUEST = 14;
    private static final String[] BT_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private int m_team1Score = 0;
    private int m_team2Score = 0;

    private ImageButton m_bluetoothButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_bluetoothButton = (ImageButton) findViewById(R.id.bluetooth);

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

        m_bluetoothButton.setOnClickListener(this);

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

        getSupportActionBar().hide();

        applyStoredPreferences();

    }

    @Override
    public void onResume()
    {
        super.onResume();

        applyStoredPreferences();
    }

    @Override
    public void onClick(View v)
    {
        int oldTeam1Score = m_team1Score;
        int oldTeam2Score = m_team2Score;
        
        switch (v.getId())
        {
            case R.id.bluetooth:
                handleBluetoothButtonClicked();
                break;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BT_PERMISSION_REQUEST:
                if (areAllBTPermissionsGranted())
                {
                    handleBluetoothButtonClicked();
                }
                break;
        }
    }

    private boolean areAllBTPermissionsGranted()
    {
        for (String permission : BT_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    private void handleBluetoothButtonClicked()
    {
        if (areAllBTPermissionsGranted())
        {
            BluetoothAdapter bluetoothAdapter = BluetoothManager.getInstance().getBluetoothAdapter();
            if (bluetoothAdapter.isEnabled())
            {
                Intent bluetoothIntent = new Intent(this, BluetoothScanActivity.class);
                startActivity(bluetoothIntent);
            }
            else
            {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(turnOn);
            }
        }
        else
        {
            ActivityCompat.requestPermissions(this, BT_PERMISSIONS, BT_PERMISSION_REQUEST);
        }
    }

    private void showTeamSetupDialog(Team team)
    {
        Intent settingsIntent = new Intent(this, TeamSettingsActivity.class);
        settingsIntent.putExtra("team", team);

        startActivity(settingsIntent);
    }

    private void applyStoredPreferences()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ColorPickerPreferenceManager colorPreferences = ColorPickerPreferenceManager.getInstance(this);

        String team1Name = preferences.getString("team1Name", getResources().getString(R.string.TEAM_1));
        m_team1Name.setText(team1Name);
        int team1Color = colorPreferences.getColor("team1Color", getResources().getColor(R.color.team1Color, null));
        setTeam1Color(team1Color);

        String team2Name = preferences.getString("team2Name", getResources().getString(R.string.TEAM_2));
        m_team2Name.setText(team2Name);
        int team2Color = colorPreferences.getColor("team2Color", getResources().getColor(R.color.team2Color, null));
        setTeam2Color(team2Color);
    }

    private void setTeam1Color(int color)
    {
        m_team1Name.setTextColor(color);
        m_team1Name.setShadowLayer(12, 0, 0, color);
        m_team1ScoreText.setTextColor(color);
        m_team1ScoreText.setShadowLayer(12, 0, 0, color);
        m_team1ScoreUp.setTextColor(color);
        m_team1ScoreUp.setShadowLayer(12, 0, 0, color);
        m_team1ScoreDown.setTextColor(color);
        m_team1ScoreDown.setShadowLayer(12, 0, 0, color);
        m_team1ScoreReset.setTextColor(color);
        m_team1ScoreReset.setShadowLayer(12, 0, 0, color);
        m_team1ScoreBackground.setTextColor(color);
    }

    private void setTeam2Color(int color)
    {
        m_team2Name.setTextColor(color);
        m_team2Name.setShadowLayer(12, 0, 0, color);
        m_team2ScoreText.setTextColor(color);
        m_team2ScoreText.setShadowLayer(12, 0, 0, color);
        m_team2ScoreUp.setTextColor(color);
        m_team2ScoreUp.setShadowLayer(12, 0, 0, color);
        m_team2ScoreDown.setTextColor(color);
        m_team2ScoreDown.setShadowLayer(12, 0, 0, color);
        m_team2ScoreReset.setTextColor(color);
        m_team2ScoreReset.setShadowLayer(12, 0, 0, color);
        m_team2ScoreBackground.setTextColor(color);
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