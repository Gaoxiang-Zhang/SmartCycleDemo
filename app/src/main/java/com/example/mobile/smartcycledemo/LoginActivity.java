package com.example.mobile.smartcycledemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.GlobalType;
import com.example.mobile.smartcycledemo.utils.MyDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity {

    Context mContext;
    MyDatabase mDatabase;
    MaterialEditText userAccount, userPassword;
    Button loginButton;
    TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;

        initUserInterface();
    }

    private void initUserInterface(){
        userAccount = (MaterialEditText)findViewById(R.id.account);
        userPassword = (MaterialEditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.next);
        registerText = (TextView)findViewById(R.id.register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                toggleButton();

            }
        };
        userAccount.addTextChangedListener(textWatcher);
        userPassword.addTextChangedListener(textWatcher);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    private void login(){
        mDatabase = MyDatabase.getInstance(getBaseContext());
        try {
            mDatabase.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Cursor c = mDatabase.checkUserBasicPassword(userAccount.getText().toString(),
                userPassword.getText().toString());
        c.moveToFirst();
        // find matched account with correct password
        if (c.getCount() > 0) {
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(GlobalType.LOGIN_FLAG, 1);
            editor.putString(MyDatabase.UserProfile.KEY_ACCOUNT, c.getString(c.getColumnIndex(MyDatabase.UserProfile.KEY_ACCOUNT)));
            editor.putString(MyDatabase.UserProfile.KEY_NAME, c.getString(c.getColumnIndex(MyDatabase.UserProfile.KEY_NAME)));
            editor.putString(MyDatabase.UserProfile.KEY_PASSWORD, c.getString(c.getColumnIndex(MyDatabase.UserProfile.KEY_PASSWORD)));
            editor.putString(MyDatabase.UserProfile.KEY_AGE, c.getString(c.getColumnIndex(MyDatabase.UserProfile.KEY_AGE)));
            editor.putInt(MyDatabase.UserProfile.KEY_GENDER, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_GENDER)));
            editor.putInt(MyDatabase.UserProfile.KEY_HEIGHT, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_HEIGHT)));
            editor.putInt(MyDatabase.UserProfile.KEY_WEIGHT, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_WEIGHT)));
            editor.putInt(MyDatabase.UserProfile.KEY_LEVEL, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_LEVEL)));
            editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_TIMES, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_TOTAL_TIMES)));
            editor.putInt(MyDatabase.UserProfile.KEY_CONTINUE_DAYS, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_CONTINUE_DAYS)));
            editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_ENERGY, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_TOTAL_ENERGY)));
            editor.putInt(MyDatabase.UserProfile.KEY_TOTAL_TIME, c.getInt(c.getColumnIndex(MyDatabase.UserProfile.KEY_TOTAL_TIME)));
            editor.putString(MyDatabase.UserProfile.KEY_LAST_DATE, c.getString(c.getColumnIndex(MyDatabase.UserProfile.KEY_LAST_DATE)));
            editor.apply();
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            if (mDatabase.findUserBasicAccount(userAccount.getText().toString())) {
                userAccount.setError(getString(R.string.account_not_exist));
            } else {
                userPassword.setError(getString(R.string.password_incorrect));
            }
        }
        mDatabase.close();
    }

    /**
     * Check if all fields have content and enable/disable the login button
     */
    private void toggleButton(){
        if(userAccount.length() > 0 && userPassword.length() > 0){
            loginButton.setEnabled(true);
        }
        else{
            loginButton.setEnabled(false);
        }
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
