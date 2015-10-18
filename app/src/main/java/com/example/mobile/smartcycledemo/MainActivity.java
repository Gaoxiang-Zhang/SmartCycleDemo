package com.example.mobile.smartcycledemo;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.smartcycledemo.fragment.CourseFragment;
import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.MyDatabase;
import com.viewpagerindicator.LinePageIndicator;

import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    private Toolbar mToolbar;

    NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    String userAccount, userName;

    int currentId = 0;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initToolbar();

        initNavigation();
    }

    private void initToolbar(){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        TextView textView = (TextView)mToolbar.findViewById(R.id.title);
        textView.setText(getString(R.string.app_name));
        setSupportActionBar(mToolbar);
    }


    private void initNavigation(){
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        // initialize navigation header
        initNavigationHeader();
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.fixed_exercise:
                        loadFragment(new CourseFragment(), menuItem);
                        break;
                    case R.id.dynamic_exercise:
                        Toast.makeText(getApplicationContext(), "dynamic_course", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.exercise_circle:
                        Toast.makeText(getApplicationContext(), "exercise_circle", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.health_data:
                        Toast.makeText(getApplicationContext(), "health_data", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.health_mall:
                        Toast.makeText(getApplicationContext(), "health_mall", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.change_theme:
                        Toast.makeText(getApplicationContext(), "change_theme", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.my_message:
                        Toast.makeText(getApplicationContext(), "my_message", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.my_settings:
                        Toast.makeText(getApplicationContext(), "my_settings", Toast.LENGTH_SHORT).show();
                        CommonFunction.clearUserInfo(mContext);
                        Intent start_intent = new Intent(mContext, LoginActivity.class);
                        start_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        start_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(start_intent);
                        finish();
                        return true;
                    default:
                        return true;
                }
                return false;
            }
        });

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.open_drawer, R.string.close_drawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        loadFragment(new CourseFragment(), null);

    }

    private void initNavigationHeader(){
        View headerView = mNavigationView.inflateHeaderView(R.layout.header);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        userAccount = sharedPreferences.getString(MyDatabase.UserProfile.KEY_ACCOUNT, "");
        userName = sharedPreferences.getString(MyDatabase.UserProfile.KEY_NAME, "");
        TextView textView = (TextView)headerView.findViewById(R.id.user_name);
        textView.setText(userName);
        textView = (TextView)headerView.findViewById(R.id.user_account);
        textView.setText(userAccount);
    }

    private void loadFragment(Fragment fragment, MenuItem item){
        if(item == null || currentId != item.getItemId()){
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment);
            fragmentTransaction.commit();
            if(item != null){
                currentId = item.getItemId();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }



}
