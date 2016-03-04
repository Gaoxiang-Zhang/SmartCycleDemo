package com.example.mobile.smartcycledemo.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.R;
import com.example.mobile.smartcycledemo.utils.GlobalType;

/**
 * CyclingFinishDialog: dialog used for Cycling Activity to finish training
 * Created by mobile on 15/8/25.
 */
public class CyclingFinishDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_finish, null);


        // set the button
        TextView textView = (TextView)view.findViewById(R.id.finish);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFinishComplete();
                getDialog().dismiss();
            }
        });

        setFinishValue(view);

        return view;
    }

    /**
     * setFinishValue: set the course title and course time for finish dialog
     */
    private void setFinishValue(View view){
        Bundle bundle = getArguments();
        String courseName = bundle.getString(GlobalType.COURSE_NAME, "");
        int courseTime = bundle.getInt(GlobalType.TOTAL_TIME, 0);
        TextView textView = (TextView)view.findViewById(R.id.course_value);
        textView.setText(courseName);
        textView = (TextView)view.findViewById(R.id.time_value);
        textView.setText(courseTime + getString(R.string.minutes));
    }

    public static interface OnFinishCompleteListener{
        public abstract void onFinishComplete();
    }

    private OnFinishCompleteListener listener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.listener = (OnFinishCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
