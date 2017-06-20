package com.sample.camera.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sample.camera.settings.CameraSettings;
import com.sample.camerafeature.R;

import java.util.ArrayList;
import java.util.Map;

public class LiveInfoLayout extends FrameLayout
        implements CameraSettings.CameraSettingListener {
    private ListView mInfoList;
    private ListViewAdapter mListViewAdapter;
    public LiveInfoLayout(@NonNull Context context) {
        this(context, null);
    }

    public LiveInfoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveInfoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(getContext());
    }

    private void init(Context context) {
        mListViewAdapter = new ListViewAdapter(context);
        mInfoList = (ListView) findViewById(R.id.live_info_list);
        mInfoList.setAdapter(mListViewAdapter);
    }

    @Override
    public void onSettingApplied(Map<String, String> settings) {
        mListViewAdapter.setData(settings);
        mListViewAdapter.notifyDataSetChanged();
    }



    public class ListViewAdapter extends BaseAdapter {
        private ArrayList<KVWrapper> listItems = new ArrayList<>();
        private final LayoutInflater mLayoutInflater;

        public ListViewAdapter(Context context) {
            mLayoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(Map<String, String> items) {
            listItems.clear();
            for (Map.Entry<String, String> entry : items.entrySet()) {
                listItems.add(new KVWrapper(entry.getKey(), entry.getValue()));
            }
        }

        @Override
        public int getCount() {
            return listItems == null ? 0 : listItems.size();
        }

        @Override
        public KVWrapper getItem(int position) {
            return listItems == null ? null : listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder viewHolder;
            if (convertView == null) {
                v = mLayoutInflater.inflate(R.layout.live_info_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) v.findViewById(R.id.text1);
                viewHolder.value = (TextView) v.findViewById(R.id.text2);
                v.setTag(viewHolder);
            } else {
                v = convertView;
                viewHolder = (ViewHolder) convertView.getTag();
            }

            KVWrapper kv = getItem(position);

            if (kv != null) {
                viewHolder.title.setText(kv.key);
                viewHolder.value.setText(kv.value);
            }
            return v;
        }

        @Override
        public boolean isEnabled(int position) {
            // make all items can't click.
            return false;
        }

        private class ViewHolder {
            TextView title;
            TextView value;
        }

        private class KVWrapper {
            final String key;
            final String value;

            public KVWrapper(String key, String value) {
                this.key = key;
                this.value = value;
            }
        }
    }
}
