package com.sample.camerafeature.fragment;

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
    public final String NA = "N/A";
    public BaseCapabilities mCapabilities;

    @Override
    public final void onActivityCreated(Bundle paramBundle) {
        super.onActivityCreated(paramBundle);
        initCapabilities();
        setMyListAdapter();
    }

    public String getName() {
        return "";
    }

    public abstract void initCapabilities();
    
    public List<Map<String, Object>> getSimpleData() {
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

    public void onListItemClick(ListView paramListView, View paramView, int paramInt,
            long paramLong) {
//        super.onListItemClick(paramListView, paramView, paramInt, paramLong);
//        paramView = (BaseCapabilities.CapabilitiesItem) this.mCapabilities.getItems()
//                .get(paramInt);
//        localObject = new AlertDialog.Builder(getActivity());
//        if (paramView.getContentList() != null) {
//            int i = paramView.getContentList().size();
//            paramListView = new CharSequence[i];
//            for (paramInt = 0; paramInt < i; paramInt++) {
//                paramListView[paramInt] = paramView.getContentList().get(paramInt).toString();
//            }
//            ((AlertDialog.Builder) localObject).setTitle(paramView.getTitle())
//                    .setItems(paramListView, null);
//            ((AlertDialog.Builder) localObject).show();
//        }
    }

    private void setMyListAdapter() {
        setListAdapter(new SimpleAdapter(getActivity(), getSimpleData(),
                R.layout.simple_list_item, new String[] {
                        "title", "content"
                }, new int[] {
                        R.id.text1, R.id.text2
                }));
    }
}
