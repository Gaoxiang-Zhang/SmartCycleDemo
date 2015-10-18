package com.example.mobile.smartcycledemo.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.mobile.smartcycledemo.LoginActivity;
import com.example.mobile.smartcycledemo.R;
import com.example.mobile.smartcycledemo.utils.GlobalType;

/**
 * Created by mobile on 15/10/15.
 */
public class WelcomeFragment extends Fragment {

    private RelativeLayout mRelativeLayout = null;
    private Button mButton = null;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_welcome, container, false);
        initInterface();
        setView();
        return mView;
    }

    private void initInterface(){
        mRelativeLayout = (RelativeLayout)mView.findViewById(R.id.layout);
        mButton = (Button)mView.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setView(){
        Bundle bundle = getArguments();
        int type = bundle.getInt(GlobalType.WELCOME_FLAG);
        switch (type){
            case 0:
                mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.blue_primary));
                mButton.setVisibility(View.INVISIBLE);
                break;
            case 1:
                mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.yellow_primary));
                mButton.setVisibility(View.INVISIBLE);
                break;
            case 2:
                mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.green_primary));
                break;
        }
    }


}
