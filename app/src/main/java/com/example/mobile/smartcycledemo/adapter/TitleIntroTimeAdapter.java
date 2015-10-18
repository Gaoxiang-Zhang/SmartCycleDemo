package com.example.mobile.smartcycledemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.R;
import com.example.mobile.smartcycledemo.utils.TitleIntroTime;

import java.util.ArrayList;

/**
 * TitleIntroTimeAdapter
 * used for Course ListView
 * Created by mobile on 15/10/17.
 */
public class TitleIntroTimeAdapter extends BaseAdapter {

    Context context;
    ArrayList<TitleIntroTime> list;

    public TitleIntroTimeAdapter(Context context, ArrayList<TitleIntroTime> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            view = inflater.inflate(R.layout.layout_course, null);
            TextView textView = (TextView)view.findViewById(R.id.title);
            textView.setText(list.get(position).title);
            textView = (TextView)view.findViewById(R.id.intro);
            textView.setText(list.get(position).intro);
            textView = (TextView)view.findViewById(R.id.time);
            textView.setText(list.get(position).time + context.getString(R.string.minutes));
        }
        else{
            TextView textView = (TextView)convertView.findViewById(R.id.title);
            textView.setText(list.get(position).title);
            textView = (TextView)convertView.findViewById(R.id.intro);
            textView.setText(list.get(position).intro);
            textView = (TextView)convertView.findViewById(R.id.time);
            textView.setText(list.get(position).time + context.getString(R.string.minutes));
            view = convertView;
        }
        return view;

    }
}
