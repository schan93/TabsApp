package com.test.tabs.tabs;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.security.BasicPermission;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class FeedListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    //ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int position) {
        return feedItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.news_feed_item, null);

        //if (imageLoader == null)
        //    imageLoader = AppController.getInstance().getImageLoader();

        TextView name = (TextView) convertView.findViewById(R.id.txt_name);
        TextView timestamp = (TextView)convertView.findViewById(R.id.txt_timestamp);
        TextView statusMsg = (TextView)convertView.findViewById(R.id.txt_statusMsg);

        //Set the views
        FeedItem item = feedItems.get(position);
        name.setText(item.getName());
        timestamp.setText(item.getTimeStamp());
        statusMsg.setText((item.getStatus()));

        //TODO: set the other data fields in FeedItem

        return convertView;
    }
}
