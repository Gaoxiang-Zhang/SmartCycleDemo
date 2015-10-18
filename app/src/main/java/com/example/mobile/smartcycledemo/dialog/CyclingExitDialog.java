package com.example.mobile.smartcycledemo.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.R;

/**
 * ExitDialog: used in Cycling Activity when quit training
 * Created by mobile on 15/8/23.
 */
public class CyclingExitDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_exit, null);

        TextView textView = (TextView)view.findViewById(R.id.confirm);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onExitComplete();
                getDialog().dismiss();
            }
        });
        textView = (TextView)view.findViewById(R.id.cancel);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    public static interface OnExitCompleteListener{
        public abstract void onExitComplete();
    }

    private OnExitCompleteListener listener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.listener = (OnExitCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
