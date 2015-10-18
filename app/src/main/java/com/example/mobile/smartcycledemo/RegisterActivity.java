package com.example.mobile.smartcycledemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.MyDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    Context mContext;

    MyDatabase mDatabase;

    MaterialEditText userAccount, userName, userPassword, againPassword;

    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mContext = this;

        mDatabase = MyDatabase.getInstance(getBaseContext());

        setToolbar();

        initUserInterface();

    }

    private void setToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView textView = (TextView)toolbar.findViewById(R.id.title);
        textView.setText(getString(R.string.register));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initUserInterface(){
        userAccount = (MaterialEditText)findViewById(R.id.account);
        userName = (MaterialEditText)findViewById(R.id.name);
        userPassword = (MaterialEditText)findViewById(R.id.password);
        againPassword = (MaterialEditText)findViewById(R.id.password_again);
        confirmButton = (Button)findViewById(R.id.next);

        userName.setHelperText(getString(R.string.nickname_helper));
        userPassword.setHelperText(getString(R.string.password_helper));

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
        userName.addTextChangedListener(textWatcher);
        userPassword.addTextChangedListener(textWatcher);
        againPassword.addTextChangedListener(textWatcher);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAvailability()){
                    Intent intent = new Intent(mContext, DetailFillActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(MyDatabase.UserProfile.KEY_ACCOUNT, userAccount.getText().toString());
                    bundle.putString(MyDatabase.UserProfile.KEY_NAME, userName.getText().toString());
                    bundle.putString(MyDatabase.UserProfile.KEY_PASSWORD, userPassword.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
               }
            }
        });


    }

    /**
     * toggleButton: check whether the 4 edit text have been filled, in order to change button state
     */
    private void toggleButton(){
        String text1 = userAccount.getText().toString(), text2 = userName.getText().toString(),
                text3 = userPassword.getText().toString(), text4 = againPassword.getText().toString();
        if(text1.length() != 0 && text2.length() != 0 && text3.length() != 0 && text4.length() != 0){
            confirmButton.setEnabled(true);
        }
        else{
            confirmButton.setEnabled(false);
        }
    }

    private boolean checkAvailability(){
        boolean flag = true;
        // check the error of user_account
        String error1 = checkAccount();
        if( error1 != null ){
            userAccount.setError(error1);
            flag = false;
        }
        // check the error of user_nickname
        String error2 = checkNickname();
        if (error2 != null){
            userName.setError(error2);
            flag = false;
        }
        // check the error of user_password
        String error3 = checkPassword();
        if (error3 != null){
            userPassword.setError(error3);
            flag = false;
        }
        else{
            error3 = checkRepeatPassword();
            if(error3 != null) {
                againPassword.setError(error3);
                flag = false;
            }
        }
        return flag;
    }

    /**
     * checkAccount: check account availability ( 1. format 2. database )
     * @return null if no error, or the error text
     */
    private String checkAccount(){

        String content = userAccount.getText().toString();

        // if matches the phone number
        if (content.matches("[0-9]+") && content.length() == 11) {
            // if there is no match in the database
            try{
                mDatabase.open();
            } catch (SQLException e){
                e.printStackTrace();
            }
            if(mDatabase.findUserBasicAccount(content)){
                mDatabase.close();
                return null;
            }
            else {
                mDatabase.close();
                return getString(R.string.account_duplicated);
            }
        }

        // if matches the email address
        boolean check;
        Pattern p;
        Matcher m;

        String EMAIL_STRING = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        p = Pattern.compile(EMAIL_STRING);

        m = p.matcher(content);
        check = m.matches();

        if(check)
        {
            // if there is no match in the database
            try{
                mDatabase.open();
            } catch (SQLException e){
                e.printStackTrace();
            }
            if(mDatabase.findUserBasicAccount(content)){
                mDatabase.close();
                return null;
            }
            else {
                mDatabase.close();
                return getString(R.string.account_duplicated);
            }
        }
        return getString(R.string.account_error);
    }

    /**
     *
     */
    private String checkNickname(){

        String content = userName.getText().toString();

        if(content.length() > 12 || content.length() < 4){
            return getString(R.string.nickname_error);
        }

        return null;
    }

    private String checkPassword(){

        String content = userPassword.getText().toString();

        if(content.length() >= 8 && content.matches("^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]+$") ){
            return null;
        }
        return getString(R.string.password_error);
    }

    private String checkRepeatPassword(){
        String content1 = userPassword.getText().toString(), content2 = againPassword.getText().toString();
        if(content1.equals(content2)){
            return null;
        }
        return getString(R.string.repeat_error);
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
