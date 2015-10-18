package com.example.mobile.smartcycledemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.mobile.smartcycledemo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by mobile on 15/10/15.
 */
public class CommonFunction {

    /**
     * checkLogin: check if any user has been logged in
     */
    public static boolean checkLogin(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(GlobalType.LOGIN_FLAG, 0) != 0;
    }

    /**
     * clearUserInfo: clear the user information in sharedPreferences when logged out
     */
    public static void clearUserInfo(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("login_flag", 0);
        editor.apply();
    }

    /**
     * setListViewHeightBasedOnChildren: fix the issue that ListView height is not as planned in scrollView
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * calculateAge: calculate user's age by given birthday
     */
    public static int calculateAge(String birthday) {
        if (birthday.length() == 0) {
            return 0;
        }
        // birthday
        String[] separated = birthday.split("-");
        int year1 = Integer.parseInt(separated[0]), month1 = Integer.parseInt(separated[1]), day1 = Integer.parseInt(separated[2]);
        Calendar calendar = Calendar.getInstance();
        // current date
        int year2 = calendar.get(Calendar.YEAR), month2 = calendar.get(Calendar.MONTH) + 1, day2 = calendar.get(Calendar.DAY_OF_MONTH);
        // have not reached birthday this year
        if (month1 > month2 || month1 == month2 && day1 > day2) {
            return year2 - year1 - 1;
        }
        return year2 - year1;
    }

    /**
     * calculateHRmax: calculate the lower and uppper bound of heart rate by given age and level
     */
    public static double[] calculateHRmax(int age, int level){
        double[] result = new double[2];
        double[] upper_bound = new double[]{0.67, 0.74, 0.84, 0.91, 0.94},
                lower_bound = new double[]{0.57, 0.64, 0.74, 0.80, 0.84};
        // fixed formula
        double hrmax = 206.9 - 0.67 * age;
        result[0] = lower_bound[level] * hrmax;
        result[1] = upper_bound[level] * hrmax;
        return result;
    }

    /**
     * getCurrentDate: return current time with year-month-day format
     * @return
     */
    public static String getCurrentDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new java.util.Date());
    }

    /**
     * calculateContinueDays: calculate the date divider of 2 yyyy-mm-dd format string
     */
    public static int calculateContinueDays(int current, String date1, String date2){
        int[] days_month = new int[]{
                0,31,28,31,30,31,30,31,31,30,31,30,31
        };
        // a new training
        if(date1.equals("0")){
            return 1;
        }
        // train at the same day
        else if(date1.equals(date2)){
            return current;
        }
        else{
            int year1 = Integer.parseInt(date1.substring(0,4)), year2 = Integer.parseInt(date2.substring(0,4));
            int month1 = Integer.parseInt(date1.substring(5,7)), month2 = Integer.parseInt(date2.substring(5,7));
            int day1 = Integer.parseInt(date1.substring(8,10)), day2 = Integer.parseInt(date2.substring(8,10));
            if(year1 != year2){
                // pass a year
                if(month1 == 12 && month2 == 1 && day1 == 31 && day2 == 1){
                    return current+1;
                }
                return 1;
            }
            else if(month1 != month2){
                // if leap year
                if(!(year1 % 4 != 0 || year1 % 100 == 0 && year1 % 400 != 0)){
                    days_month[2] = 29;
                }
                if( month1 + 1 == month2 && day1 == days_month[month1] && day2 == 1){
                    return current+1;
                }
                return 1;
            }
            else{
                if( day1 + 1 == day2){
                    return current+1;
                }
                return 1;
            }
        }
    }
}
