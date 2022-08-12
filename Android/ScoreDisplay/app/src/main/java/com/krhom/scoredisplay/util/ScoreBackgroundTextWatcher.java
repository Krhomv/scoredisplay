package com.krhom.scoredisplay.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class ScoreBackgroundTextWatcher implements TextWatcher
{
    private TextView m_bgTextView;

    public ScoreBackgroundTextWatcher(TextView bgTextView)
    {
        m_bgTextView = bgTextView;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        String bgText = new String(new char[s.length()]).replace('\0', '8');
        m_bgTextView.setText(bgText);
    }

    @Override
    public void afterTextChanged(Editable editable)
    {

    }
}
