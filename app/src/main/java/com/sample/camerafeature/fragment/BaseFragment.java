package com.sample.camerafeature.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.camerafeature.R;
import com.sample.camerafeature.fragment.BaseCapabilities.CapabilitiesItem;

public abstract class BaseFragment extends ListFragment {
    public BaseCapabilities mCapabilities;
    protected boolean mUseApi2 = false;

    protected void init(boolean api2) {
        mUseApi2 = api2;
    }

    @Override
    public final void onActivityCreated(Bundle paramBundle) {
        super.onActivityCreated(paramBundle);
        initCapabilities();
        setMyListAdapter();
    }

    public String getName(Resources res) {
        return "";
    }

    public abstract void initCapabilities();
    
    private List<Map<String, Object>> getSimpleData() {
        ArrayList resultList = new ArrayList();
        if (mCapabilities == null) return resultList;
        List localList = mCapabilities.getItems();
        int size = localList.size();
        if (this.mCapabilities != null) {
            for (int i = 0; i < size; i++) {
                CapabilitiesItem item = (CapabilitiesItem) localList.get(i);
                HashMap localHashMap = new HashMap();
                localHashMap.put("title", i + 1 + ". " + item.getTitle());
                localHashMap.put("content", item.getContent());
                resultList.add(localHashMap);
            }
        }
        return resultList;
    }

    public void onListItemClick(ListView listView, View v, int position, long id) {
        super.onListItemClick(listView, v, position, id);

        CapabilitiesItem item = mCapabilities.getItems().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        List contentList = item.getContentList();
        // if content just has 1 element, ignore it.
        if (contentList != null && contentList.size() > 1) {
            int size = contentList.size();
            CharSequence[] items = new CharSequence[size];
            for (position = 0; position < size; position++) {
                items[position] = contentList.get(position).toString();
            }
            builder.setTitle(item.getTitle());
            builder.setItems(items, null);
            builder.show();
        }
    }

    private void setMyListAdapter() {
        Context context = getContext();
        if (context == null) return;
        setListAdapter(new SimpleAdapter(context, getSimpleData(),
                R.layout.simple_list_item, new String[] {
                        "title", "content"
                }, new int[] {
                        R.id.text1, R.id.text2
                }));
    }

    public void onApiLevelChanged() {
        initCapabilities();
        setMyListAdapter();
    }

    public final void setApi2(boolean api2) {
        if (mUseApi2 != api2) {
            mUseApi2 = api2;
            onApiLevelChanged();
        }
    }
}
