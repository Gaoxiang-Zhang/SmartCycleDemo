package com.example.mobile.smartcycledemo.dialog;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.rey.material.widget.RadioButton;
import com.example.mobile.smartcycledemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * LevelDialog: used for level selection in DetailFillActivity
 * Created by mobile on 15/8/25.
 */
public class LevelDialog extends DialogFragment {

    View dialogView;

    ListView listView;
    List<Level> levelList;
    LevelListAdapter levelListAdapter;

    int selected = 0;
    boolean[] item_clicked = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialogView = inflater.inflate(R.layout.dialog_level, container, false);

        selected = getArguments().getInt("level");

        initInterface();

        // Inflate the layout for this fragment
        return dialogView;
    }

    private void initInterface(){

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        TextView confirmButton = (TextView)dialogView.findViewById(R.id.confirm),
                cancelButton = (TextView)dialogView.findViewById(R.id.cancel);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLevelComplete(selected);
                getDialog().dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        initListView();
    }

    private void initListView(){
        listView = (ListView)dialogView.findViewById(R.id.list_view);

        String[] titles = getResources().getStringArray(R.array.user_level_name);
        String[] intros = getResources().getStringArray(R.array.user_level_content);
        item_clicked = new boolean[titles.length];
        if(selected > 0){
            item_clicked[selected] = true;
        }
        levelList = new ArrayList<>();
        for(int i = 0 ; i < titles.length ; i++){
            levelList.add(new Level(titles[i], intros[i], item_clicked[i]));
        }
        levelListAdapter = new LevelListAdapter(getActivity(), levelList);
        listView.setAdapter(levelListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0 ; i < item_clicked.length ; i++){
                    levelList.get(i).state = false;
                }
                levelList.get(position).state = true;
                selected = position;
                levelListAdapter.notifyDataSetChanged();
            }
        });
    }

    private class Level{
        String title;
        String intro;
        boolean state;

        Level(String title, String intro, boolean state){
            this.title = title;
            this.intro = intro;
            this.state = state;
        }
    }

    private class LevelListAdapter extends BaseAdapter{

        Context context;
        List<Level> list;

        LevelListAdapter(Context context, List<Level> list){
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                v = inflater.inflate(R.layout.layout_radio_layout, null);
                TextView textView = (TextView)v.findViewById(R.id.title);
                textView.setText(list.get(position).title);
                textView = (TextView)v.findViewById(R.id.intro);
                textView.setText(list.get(position).intro);
                RadioButton button = (RadioButton)v.findViewById(R.id.radio_button);
                button.setClickable(false);
                button.setChecked(list.get(position).state);
            }
            else{
                TextView textView = (TextView)convertView.findViewById(R.id.title);
                textView.setText(list.get(position).title);
                textView = (TextView)convertView.findViewById(R.id.intro);
                textView.setText(list.get(position).intro);
                RadioButton button = (RadioButton)convertView.findViewById(R.id.radio_button);
                button.setClickable(false);
                button.setChecked(list.get(position).state);
                v = convertView;
            }
            return v;
        }
    }

    public static interface OnLevelCompleteListener{
        public abstract void onLevelComplete(int param);
    }

    private OnLevelCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnLevelCompleteListener)activity;
        } catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
