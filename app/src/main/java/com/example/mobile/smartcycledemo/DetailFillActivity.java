package com.example.mobile.smartcycledemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.dialog.BirthdayDialog;
import com.example.mobile.smartcycledemo.dialog.LevelDialog;
import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.GlobalType;
import com.example.mobile.smartcycledemo.utils.MyDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.sql.SQLException;

import javax.microedition.khronos.egl.EGLDisplay;

/**
 * DetailFillActivity:
 */
public class DetailFillActivity extends AppCompatActivity implements BirthdayDialog.OnTimeCompleteListener, LevelDialog.OnLevelCompleteListener{

    Context mContext;                              // Base Context

    MyDatabase mDatabase;                          // Base database
    SharedPreferences mSharedPreferences;          // SharedPreferences

    LinearLayout birthdayLayout;                   // layout holding the birthday area
    LinearLayout levelLayout;                      // layout holding the level area
    MaterialEditText userHeight, userWeight;       // Edit Text of height and weight
    ImageView maleImage, femaleImage;              // Image for gender selection
    TextView birthdayText, levelText;              // TextView for birthday and level
    Button confirmButton;                          // confirm button


    int year = 0, month = 0, day = 0;              // saved birthday information

    int gender;                                    // 1 = female, 0 = male

    int level = 1;                                 // saved level information

    int height, weight;                            // saved height and weight


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_fill);

        mContext = this;

        setToolbar();

        initView();
    }

    /**
     * setToolbar: initialize the toolbar
     */
    private void setToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView textView = (TextView)toolbar.findViewById(R.id.title);
        textView.setText(getString(R.string.info_fill_title));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * initView: initialize the user interface
     */
    private void initView(){
        birthdayLayout = (LinearLayout)findViewById(R.id.age_layout);
        userHeight = (MaterialEditText)findViewById(R.id.user_height);
        userWeight = (MaterialEditText)findViewById(R.id.user_weight);
        maleImage = (ImageView)findViewById(R.id.male);
        femaleImage = (ImageView)findViewById(R.id.female);
        birthdayText = (TextView)findViewById(R.id.user_age);
        levelLayout = (LinearLayout)findViewById(R.id.level_layout);
        levelText = (TextView)findViewById(R.id.user_level);
        confirmButton = (Button)findViewById(R.id.next_step);

        // set age selector
        birthdayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BirthdayDialog dialog = new BirthdayDialog();
                Bundle args = new Bundle();
                args.putInt("year",year);
                args.putInt("month", month);
                args.putInt("day",day);
                dialog.setArguments(args);
                dialog.show(getFragmentManager(),"birthday_dialog");
            }
        });

        // set level selector
        levelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LevelDialog dialog = new LevelDialog();
                Bundle args = new Bundle();
                args.putInt("level",level);
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), "level_dialog");
            }
        });


        // set gender selector
        View.OnClickListener imageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // current gender is male and press female button
                if(gender == 0 && v.getId() == R.id.female){
                    gender = 1;
                    maleImage.setImageResource(R.mipmap.male1);
                    femaleImage.setImageResource(R.mipmap.female2);
                }
                // current gender is female and press male button
                else if(gender == 1 && v.getId() == R.id.male){
                    gender = 0;
                    maleImage.setImageResource(R.mipmap.male2);
                    femaleImage.setImageResource(R.mipmap.female1);
                }
            }
        };
        maleImage.setOnClickListener(imageListener);
        femaleImage.setOnClickListener(imageListener);

        // text watcher to confirm no null text field
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputEnableButton();
            }
        };
        levelText.addTextChangedListener(textWatcher);
        birthdayText.addTextChangedListener(textWatcher);
        userHeight.addTextChangedListener(textWatcher);
        userWeight.addTextChangedListener(textWatcher);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAvailability()){
                    finishRegister();
                    Intent intent = new Intent(mContext,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    /**
     * inputEnableButton: check if all the fields have been filled and change the state of confirm button
     */
    private void inputEnableButton(){
        String content1 = birthdayText.getText().toString(), content2 = userHeight.getText().toString(),
                content3 = userWeight.getText().toString(), content4 = levelText.getText().toString();
        if(!content1.equals(getString(R.string.select_birthday)) && content2.length() > 0 &&
                content3.length() > 0 && !content4.equals(getString(R.string.user_level_hint))){
            confirmButton.setEnabled(true);
        }
        else{
            confirmButton.setEnabled(false);
        }
    }

    /**
     * checkAvailability: check if all the fields have reasonable value
     */
    private boolean checkAvailability(){
        boolean flag = true;
        height = Integer.parseInt(userHeight.getText().toString());
        weight = Integer.parseInt(userWeight.getText().toString());
        if( height < 120 || height > 240){
            userHeight.setError(getString(R.string.height_error));
            flag = false;
        }
        if( weight < 30 || weight > 200){
            userWeight.setError(getString(R.string.weight_error));
            flag = false;
        }
        return flag;
    }

    /**
     * finishRegister: finishRegister by writing to shared preferences, database, then jumping to MainActivity
     */
    private void finishRegister(){
        Bundle bundle = getIntent().getExtras();
        mDatabase = MyDatabase.getInstance(getBaseContext());
        String account = bundle.getString(MyDatabase.UserProfile.KEY_ACCOUNT), password = bundle.getString(MyDatabase.UserProfile.KEY_PASSWORD),
                nickname = bundle.getString(MyDatabase.UserProfile.KEY_NAME), age = birthdayText.getText().toString();
        try{
            mDatabase.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mDatabase.insertUserBasic(account, nickname, password, age, gender, height, weight, level,
                0, 0, "0", 0, 0);
        mDatabase.close();

        mSharedPreferences = getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(GlobalType.LOGIN_FLAG,1);
        editor.putString(MyDatabase.UserProfile.KEY_ACCOUNT, account);
        editor.putString(MyDatabase.UserProfile.KEY_PASSWORD, password);
        editor.putString(MyDatabase.UserProfile.KEY_NAME, nickname);
        editor.putString(MyDatabase.UserProfile.KEY_AGE, age);
        editor.putInt(MyDatabase.UserProfile.KEY_GENDER, gender);
        editor.putInt(MyDatabase.UserProfile.KEY_HEIGHT, height);
        editor.putInt(MyDatabase.UserProfile.KEY_WEIGHT, weight);
        editor.putInt(MyDatabase.UserProfile.KEY_LEVEL, level);
        editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_TIMES, 0);
        editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_TIME, 0);
        editor.putInt(MyDatabase.UserProfile.KEY_CONTINUE_DAYS, 0);
        editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_ENERGY, 0);
        editor.putString(MyDatabase.UserProfile.KEY_LAST_DATE, "0");
        editor.apply();
    }

    /**
     * onTimeComplete: complete the work when getting value from BirthdayDialog
     */
    public void onTimeComplete(int[] param){
        year = param[0];
        month = param[1];
        day = param[2];
        birthdayText.setText(year + "-" + month + "-" + day);
    }

    /**
     * onLevelComplete: complete the work when getting value from LevelDialog
     */
    public void onLevelComplete(int param){
        String[] level_instruction = getResources().getStringArray(R.array.user_level_name);
        level = param;
        levelText.setText(level_instruction[level]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        // has already login
        if(CommonFunction.checkLogin(mContext)){
            finish();
        }
    }

}
