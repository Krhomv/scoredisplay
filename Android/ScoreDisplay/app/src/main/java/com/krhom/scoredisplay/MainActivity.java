package com.krhom.scoredisplay;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.krhom.scoredisplay.bluetooth.BluetoothManager;
import com.krhom.scoredisplay.bluetooth.BluetoothScanActivity;
import com.krhom.scoredisplay.util.ConnectPermission;
import com.krhom.scoredisplay.util.ScanPermission;
import com.krhom.scoredisplay.util.ScoreBackgroundTextWatcher;
import com.polidea.rxandroidble2.RxBleClient;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


enum Team
{
    TEAM1,
    TEAM2
}

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        View.OnLongClickListener,
        View.OnFocusChangeListener,
        TextView.OnEditorActionListener,
        View.OnTouchListener,
        BluetoothManager.StatusChangedListener

{
    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int MAX_SCORE = 99;
    private static final int RESET_TIME_REQUIRED_MS = 1575;

    private int m_team1Score = 0;
    private int m_team2Score = 0;

    @BindView(R.id.bluetooth)
    public ImageButton m_bluetoothButton;

    @BindView(R.id.team1Name)
    public Button m_team1Name;
    @BindView(R.id.team1ScoreUp)
    public Button m_team1ScoreUp;
    @BindView(R.id.team1ScoreDown)
    public Button m_team1ScoreDown;
    @BindView(R.id.team1ScoreReset)
    public Button m_team1ScoreReset;
    @BindView(R.id.team1Score)
    public EditText m_team1ScoreText;
    @BindView(R.id.team1ScoreBackground)
    public TextView m_team1ScoreBackground;

    @BindView(R.id.team2Name)
    public Button m_team2Name;
    @BindView(R.id.team2ScoreUp)
    public Button m_team2ScoreUp;
    @BindView(R.id.team2ScoreDown)
    public Button m_team2ScoreDown;
    @BindView(R.id.team2ScoreReset)
    public Button m_team2ScoreReset;
    @BindView(R.id.team2Score)
    public EditText m_team2ScoreText;
    @BindView(R.id.team2ScoreBackground)
    public TextView m_team2ScoreBackground;

    private Timer m_team1Timer;
    private Timer m_team2Timer;

    BluetoothManager m_bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BluetoothManager.getInstance() == null)
        {
            BluetoothManager.initialise(this);
        }
        m_bluetoothManager = BluetoothManager.getInstance();
        m_bluetoothManager.addStatusChangedListener(this);

        // Bind the views
        ButterKnife.bind(this);

        m_bluetoothButton.setOnClickListener(this);
        m_bluetoothButton.setOnLongClickListener(this);

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

        // Use this to make the background text adapt to the number of characters being typed
        m_team1ScoreText.addTextChangedListener(new ScoreBackgroundTextWatcher(m_team1ScoreBackground));
        m_team2ScoreText.addTextChangedListener(new ScoreBackgroundTextWatcher(m_team2ScoreBackground));

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
        sendAllValuesToBTDevice();

        setBluetoothButtonState(m_bluetoothManager.getStatus());
    }

    @Override
    public void onClick(View v)
    {
        int oldTeam1Score = m_team1Score;
        int oldTeam2Score = m_team2Score;

        switch (v.getId())
        {
            case R.id.bluetooth:
                handleBluetoothButtonClicked(false);
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
            updateTeam1ScoreDisplay();
            sendTeam1ScoreToBTDevice();
        }

        if (m_team2Score != oldTeam2Score)
        {
            updateTeam2ScoreDisplay();
            sendTeam2ScoreToBTDevice();
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        switch (v.getId())
        {
            case R.id.bluetooth:
                handleBluetoothButtonClicked(true);
                return true;
        }
        return false;
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
                    int oldScore = m_team1Score;
                    try
                    {
                        m_team1Score = Integer.parseInt(scoreString);
                    }
                    catch (Exception e)
                    {
                        m_team1Score = 0;
                    }
                    String scoreStringLeadingZeros = String.format("%02d", m_team1Score);

                    if (oldScore != m_team1Score)
                    {
                        sendTeam1ScoreToBTDevice();
                    }

                    if (!scoreString.equals(scoreStringLeadingZeros))
                    {
                        m_team1ScoreText.setText(scoreStringLeadingZeros);
                    }
                }
                break;

                case R.id.team2Score:
                    String scoreString = m_team2ScoreText.getText().toString();
                    int oldScore = m_team2Score;
                    try
                    {
                        m_team2Score = Integer.parseInt(scoreString);
                    }
                    catch (Exception e)
                    {
                        m_team2Score = 0;
                    }
                    String scoreStringLeadingZeros = String.format("%02d", m_team2Score);

                    if (oldScore != m_team2Score)
                    {
                        sendTeam1ScoreToBTDevice();
                    }

                    if (!scoreString.equals(scoreStringLeadingZeros))
                    {
                        m_team2ScoreText.setText(scoreStringLeadingZeros);
                        sendTeam2ScoreToBTDevice();
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
                int rawX = (int) ev.getRawX();
                int rawY = (int) ev.getRawY();
                if (!r.contains(rawX, rawY))
                {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent)
    {
        // Defocus when the ok button is pressed when editing with the on-screen keyboard
        if (keyCode == KeyEvent.KEYCODE_ENDCALL)
        {
            textView.clearFocus();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        RxBleClient rxBleClient = m_bluetoothManager.getRxBleClient();
        if (ScanPermission.isScanPermissionGranted(requestCode, permissions, grantResults, rxBleClient))
        {
            handleBluetoothButtonClicked(false);
        }
        else if (ConnectPermission.isRequestConnectionPermissionGranted(requestCode, permissions, grantResults, rxBleClient))
        {
            handleBluetoothButtonClicked(false);
        }
    }

    private boolean areAllBTPermissionsGranted()
    {
        RxBleClient rxBleClient = m_bluetoothManager.getRxBleClient();

        return rxBleClient.isConnectRuntimePermissionGranted()
                && rxBleClient.isScanRuntimePermissionGranted();
    }

    private void launchBTScanActivity()
    {
        Intent bluetoothIntent = new Intent(this, BluetoothScanActivity.class);
        startActivity(bluetoothIntent);
    }

    @SuppressLint("MissingPermission")
    private void handleBluetoothButtonClicked(boolean longClick)
    {
        if (areAllBTPermissionsGranted())
        {
            BluetoothAdapter bluetoothAdapter = m_bluetoothManager.getBluetoothAdapter();
            if (bluetoothAdapter.isEnabled())
            {
                if (longClick)
                {
                    launchBTScanActivity();
                }
                else
                {
                    switch (m_bluetoothManager.getStatus())
                    {
                        case ERROR:
                        case DISCONNECTED:
                            m_bluetoothManager.startScanAndConnect();
                            break;
                        case SCANNING:
                        case CONNECTING:
                        case CONNECTED:
                            m_bluetoothManager.stopScanAndDisconnect();
                            break;
                    }
                }
            }
            else
            {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(turnOn);
            }
        }
        else
        {
            RxBleClient rxBleClient = m_bluetoothManager.getRxBleClient();

            if (!rxBleClient.isScanRuntimePermissionGranted())
            {
                ScanPermission.requestScanPermission(this, rxBleClient);
            }
            if (!rxBleClient.isConnectRuntimePermissionGranted())
            {
                ConnectPermission.requestConnectionPermission(this, rxBleClient);
            }
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

    private void updateTeam1ScoreDisplay()
    {
        m_team1ScoreText.setText(String.format("%02d", m_team1Score));
    }

    private void updateTeam2ScoreDisplay()
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

    private void setBluetoothButtonState(BluetoothManager.Status status)
    {
        switch (status)
        {
            case DISCONNECTED:
                m_bluetoothButton.setColorFilter(ContextCompat.getColor(this, R.color.bluetoothDeviceDisconnected));
                m_bluetoothButton.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
                break;
            case SCANNING:
                m_bluetoothButton.setColorFilter(ContextCompat.getColor(this, R.color.bluetoothDeviceScanning));
                m_bluetoothButton.setImageResource(R.drawable.ic_baseline_bluetooth_searching_24);
                break;
            case CONNECTING:
                m_bluetoothButton.setColorFilter(ContextCompat.getColor(this, R.color.bluetoothDeviceConnecting));
                m_bluetoothButton.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                break;
            case CONNECTED:
                m_bluetoothButton.setColorFilter(ContextCompat.getColor(this, R.color.bluetoothDeviceConnected));
                m_bluetoothButton.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                break;
            case ERROR:
                m_bluetoothButton.setColorFilter(ContextCompat.getColor(this, R.color.bluetoothDeviceConnectionFailed));
                m_bluetoothButton.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
                break;
        }
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
                                        updateTeam1ScoreDisplay();
                                        sendTeam1ScoreToBTDevice();
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
                                        updateTeam2ScoreDisplay();
                                        sendTeam2ScoreToBTDevice();
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

    private void sendAllValuesToBTDevice()
    {
        if (m_bluetoothManager.getStatus() == BluetoothManager.Status.CONNECTED)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            ColorPickerPreferenceManager colorPreferences = ColorPickerPreferenceManager.getInstance(this);

            m_bluetoothManager.sendTeam1Score(m_team1Score);

            int team1Color = colorPreferences.getColor("team1Color", getResources().getColor(R.color.team1Color, null));
            m_bluetoothManager.sendTeam1Color(team1Color);

            int team1Brightness = preferences.getInt("team1Brightness", 16);
            m_bluetoothManager.sendTeam1Brightness(team1Brightness);

            m_bluetoothManager.sendTeam2Score(m_team2Score);

            int team2Color = colorPreferences.getColor("team2Color", getResources().getColor(R.color.team2Color, null));
            m_bluetoothManager.sendTeam2Color(team2Color);

            int team2Brightness = preferences.getInt("team2Brightness", 16);
            m_bluetoothManager.sendTeam2Brightness(team2Brightness);
        }
    }

    private void sendTeam1ScoreToBTDevice()
    {
        if (m_bluetoothManager.getStatus() == BluetoothManager.Status.CONNECTED)
        {
            m_bluetoothManager.sendTeam1Score(m_team1Score);
        }
    }

    private void sendTeam2ScoreToBTDevice()
    {
        if (m_bluetoothManager.getStatus() == BluetoothManager.Status.CONNECTED)
        {
            m_bluetoothManager.sendTeam2Score(m_team2Score);
        }
    }

    @Override
    public void onBluetoothManagerStatusChanged(BluetoothManager.Status newStatus)
    {
        switch (newStatus)
        {
            case DISCONNECTED:
                break;
            case SCANNING:
                break;
            case CONNECTING:
                break;
            case CONNECTED:
            {
                sendAllValuesToBTDevice();
                break;
            }
            case ERROR:
                break;
        }

        setBluetoothButtonState(newStatus);
    }


}