package com.example.samue.login;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Friends> listItems;

    public FriendsAdapter(Context context, ArrayList<Friends> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friends friend = (Friends) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.listview_row, null);
        ImageView img = (ImageView) convertView.findViewById(R.id.img_user);
        TextView fn = (TextView) convertView.findViewById(R.id.friend_name);

        img.setImageResource(friend.getImg());
        fn.setText(friend.getNombre());

        return convertView;
    }
}
