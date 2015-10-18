package com.example.mobile.smartcycledemo.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.R;

/**
 * Created by mobile on 15/8/12.
 */
public class BirthdayDialog extends DialogFragment {

    DatePicker datePicker = null;
    TextView confirmText, cancelText;
    int old_year, old_month, old_day;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_birthday, container);
        datePicker = (DatePicker)view.findViewById(R.id.date_picker);
        confirmText = (TextView)view.findViewById(R.id.confirm);
        cancelText = (TextView)view.findViewById(R.id.cancel);


        old_year = getArguments().getInt("year");
        old_month = getArguments().getInt("month");
        old_day = getArguments().getInt("day");
        if(old_year != 0){
            datePicker.init(old_year,old_month-1,old_day,null);
        }
        datePicker.setMaxDate(System.currentTimeMillis() - 1000);

        confirmText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth()+1;
                int day = datePicker.getDayOfMonth();
                mListener.onTimeComplete(new int[]{year,month,day});
                getDialog().dismiss();
            }
        });
        cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        datePicker.setCalendarViewShown(false);

        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            TextView textView = (TextView)view.findViewById(R.id.dialog_title);
            textView.setVisibility(View.GONE);
        }

        return view;
    }

    public static interface OnTimeCompleteListener{
        public abstract void onTimeComplete(int[] param);
    }

    private OnTimeCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnTimeCompleteListener)activity;
        } catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
