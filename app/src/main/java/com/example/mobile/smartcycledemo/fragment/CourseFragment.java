package com.example.mobile.smartcycledemo.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.smartcycledemo.CyclingActivity;
import com.example.mobile.smartcycledemo.R;
import com.example.mobile.smartcycledemo.adapter.TitleIntroTimeAdapter;
import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.GlobalType;
import com.example.mobile.smartcycledemo.utils.MyDatabase;
import com.example.mobile.smartcycledemo.utils.TitleIntroTime;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CourseFragment extends Fragment {

    Context mContext;                    // base context

    CyclingTime[] times;                 // holds 4 period of training
    String[] titles, intros;             // holds the course title and introduction
    ArrayList<TitleIntroTime> courses;   // course information array list

    View mView;                          // base view

    int trainingTimes = 0, continueDays = 0, totalCalorie = 0, totalMinutes = 0;          // 4 value of user showing on CourseFragment, with the TextView holding them
    TextView trainingTimesText, continueDaysText, totalCalorieText, totalMinutesText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_course, container, false);

        mContext = getActivity();

        // Inflate the layout for this fragment
        initTopBar();

        initListView();
        return mView;
    }

    /**
     * initTopBar: initialize the top layout holding values and CollapsingToolbarLayout
     */
    private void initTopBar(){
        CollapsingToolbarLayout layout = (CollapsingToolbarLayout)mView.findViewById(R.id.collapsing_view);
        layout.setTitle(getString(R.string.courses));

        View view = mView.findViewById(R.id.user_data);

        View tmpView = view.findViewById(R.id.finish_training);
        TextView textView = (TextView)tmpView.findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.finish_training));
        textView = (TextView)tmpView.findViewById(R.id.unit);
        textView.setText(getResources().getString(R.string.times));
        trainingTimesText = (TextView)tmpView.findViewById(R.id.value);

        tmpView = view.findViewById(R.id.continue_days);
        textView = (TextView)tmpView.findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.continue_days));
        textView = (TextView)tmpView.findViewById(R.id.unit);
        textView.setText(getResources().getString(R.string.days));
        continueDaysText = (TextView)tmpView.findViewById(R.id.value);

        tmpView = view.findViewById(R.id.total_calorie);
        textView = (TextView)tmpView.findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.total_calorie));
        textView = (TextView)tmpView.findViewById(R.id.unit);
        textView.setText(getResources().getString(R.string.calorie));
        totalCalorieText = (TextView)tmpView.findViewById(R.id.value);

        textView = (TextView)view.findViewById(R.id.my_title);
        textView.setText(getResources().getString(R.string.training_time));
        textView = (TextView)view.findViewById(R.id.my_unit);
        textView.setText(getResources().getString(R.string.minutes));
        totalMinutesText = (TextView)view.findViewById(R.id.my_value);

        setUserData();

    }

    /**
     * setUserData: get data from SharedPreference and set them
     */
    private void setUserData(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        trainingTimes = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_TOTAL_TIMES, 0);
        continueDays = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_CONTINUE_DAYS, 0);
        totalCalorie = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_TOTAL_ENERGY, 0);
        totalMinutes = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_TOTAL_TIME, 0);
        trainingTimesText.setText(trainingTimes + "");
        continueDaysText.setText(continueDays + "");
        totalCalorieText.setText(totalCalorie + "");
        totalMinutesText.setText(totalMinutes +"");
    }

    /**
     * initListView: initialize the ListView
     */
    private void initListView(){
        courses = new ArrayList<>();
        generateFakeData();
        ListView listView = (ListView)mView.findViewById(R.id.content).findViewById(R.id.list_view);
        TitleIntroTimeAdapter adapter = new TitleIntroTimeAdapter(mContext, courses);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, CyclingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(GlobalType.COURSE_NAME, titles[position]);
                bundle.putInt(GlobalType.TIME_PERIOD1, times[position].time1);
                bundle.putInt(GlobalType.TIME_PERIOD2, times[position].time2);
                bundle.putInt(GlobalType.TIME_PERIOD3, times[position].time3);
                bundle.putInt(GlobalType.TIME_PERIOD4, times[position].time4);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });
        listView.setDivider(null);
        CommonFunction.setListViewHeightBasedOnChildren(listView);
    }

    /**
     * generateFakeData: generate random time value for each course
     * TODO: use database to holds the course content
     */
    private void generateFakeData(){
        titles = getResources().getStringArray(R.array.exercise_titles);
        intros = getResources().getStringArray(R.array.exercise_intros);

        times = new CyclingTime[titles.length];
        for(int i = 0; i < titles.length; i++){
            times[i] = new CyclingTime();
        }

        // the first data is for test
        courses.add(new TitleIntroTime(titles[0], intros[0], 4));
        times[0].time1 = times[0].time2 = times[0].time3 = times[0].time4 = 60;
        // for the rest of data, randomly generate the
        for(int i = 1; i < titles.length; i++){
            // time 1 ~ time 4 are for different period of cycling, and the unit is second
            times[i].time1 = (int) (Math.random() * 10 + 5) * 60;
            times[i].time2 = (int) (Math.random() * 10 + 5) * 60;
            times[i].time3 = (int) (Math.random() * 10 + 5) * 60;
            times[i].time4 = (int) (Math.random() * 10 + 5) * 60;
            courses.add(new TitleIntroTime(titles[i], intros[i], times[i].sum()));
        }
        // the last is designed for overflow bug of list view
        courses.add(new TitleIntroTime("", "", 0));
    }


    class CyclingTime{
        int time1, time2, time3, time4;
        int sum(){
            return (time1 + time2 + time3 + time4) / 60;
        }
    }

    /**
     * updateUserData: update data when getting finish a training ( return from CyclingFinishDialog )
     */
    private void updateUserData(int time){

        // get original data from shared preferences
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        String account = sharedPreferences.getString(MyDatabase.UserProfile.KEY_ACCOUNT, ""),
                name = sharedPreferences.getString(MyDatabase.UserProfile.KEY_NAME, ""),
                password = sharedPreferences.getString(MyDatabase.UserProfile.KEY_PASSWORD, ""),
                age = sharedPreferences.getString(MyDatabase.UserProfile.KEY_AGE, ""),
                lastDate = sharedPreferences.getString(MyDatabase.UserProfile.KEY_LAST_DATE, "0");
        int gender = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_GENDER, 0),
                height = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_HEIGHT, 0),
                weight = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_WEIGHT, 0),
                level  = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_LEVEL, 0),
                totalTimes = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_TOTAL_TIMES, 0),
                continueDays = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_CONTINUE_DAYS, 0),
                totalEnergy = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_TOTAL_ENERGY, 0),
                totalTime = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_TOTAL_TIME, 0);
        // update data
        //TODO: update calorie data
        totalTimes++;
        continueDays = CommonFunction.calculateContinueDays(continueDays, lastDate, CommonFunction.getCurrentDate());
        lastDate = CommonFunction.getCurrentDate();
        totalTime += time;

        // set new data for shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_TIME, totalTime);
        editor.putInt(MyDatabase.UserProfile.KEY_CONTINUE_DAYS, continueDays);
        editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_ENERGY, totalEnergy);
        editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_TIMES, totalTimes);
        editor.putString(MyDatabase.UserProfile.KEY_LAST_DATE, lastDate);
        editor.apply();

        // set new data for database
        MyDatabase mDatabase = MyDatabase.getInstance(mContext);
        try{
            mDatabase.open();
        } catch (SQLException e){
            e.printStackTrace();
        }
        Cursor c = mDatabase.checkUserBasicPassword(account, password);
        if(c.moveToFirst()){
            /*(String account, String username, String password,
                    String age, int gender, int height, int weight, int level, int total_time,
            int total_times, String last_date, int continue_days, int total_energy){*/
            mDatabase.updateUserBasic(account, name, password, age, gender, height, weight, level, totalTime, totalTimes, lastDate, continueDays, totalEnergy);

        }
        mDatabase.close();
    }

    /**
     * OnActivityResult: get the return value from Cycling Activity( startActivityForResult is in initListView())
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            return;
        }
        int time = data.getIntExtra(GlobalType.TOTAL_TIME, -1);
        if(time == -1){
            return;
        }
        updateUserData(time);
        // refresh the fragment
        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, new CourseFragment())
                .commit();

    }
}
