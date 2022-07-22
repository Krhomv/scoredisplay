package com.krhom.scoredisplay;

import android.app.Activity;

import androidx.fragment.app.DialogFragment;

public abstract class BaseDialogFragment<T> extends DialogFragment
{
    private T m_activityInstance;

    public final T getActivityInstance() {
        return m_activityInstance;
    }

    @Override
    public void onAttach(Activity activity) {
        m_activityInstance = (T) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        m_activityInstance = null;
    }
}