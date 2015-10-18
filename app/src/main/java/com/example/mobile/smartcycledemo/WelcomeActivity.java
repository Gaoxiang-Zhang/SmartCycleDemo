package com.example.mobile.smartcycledemo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.mobile.smartcycledemo.fragment.WelcomeFragment;
import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.GlobalType;
import com.viewpagerindicator.LinePageIndicator;

public class WelcomeActivity extends AppCompatActivity {

    ViewPager mViewPager;
    LinePageIndicator mLinePageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        checkLogin();

        initViewPager();
    }

    private void checkLogin(){
        if(CommonFunction.checkLogin(this)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initViewPager(){
        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        FragmentManager manager = getFragmentManager();
        mViewPager.setAdapter(new WelcomeAdapter(manager));
        mLinePageIndicator = (LinePageIndicator)findViewById(R.id.indicator);
        mLinePageIndicator.setViewPager(mViewPager);
    }

    class WelcomeAdapter extends FragmentPagerAdapter{
        public WelcomeAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt(GlobalType.WELCOME_FLAG, position);
            WelcomeFragment fragment = new WelcomeFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount(){
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return null;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(CommonFunction.checkLogin(this)){
            finish();
        }
    }

}
