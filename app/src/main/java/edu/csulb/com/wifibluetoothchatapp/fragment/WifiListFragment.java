package edu.csulb.com.wifibluetoothchatapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import edu.csulb.com.wifibluetoothchatapp.adapter.WifiListArrayAdapter;


public class WifiListFragment extends ListFragment {
    private WifiListListener mActivity;
    private ListView listView;

    public interface WifiListListener {
        void onItemClick(int position);
        WifiListArrayAdapter getListAdapter();
        void discoverPeers();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mActivity = (WifiListListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (WifiListListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement WifiListListener");
        }
    }

    public static WifiListFragment newInstance(WifiListArrayAdapter wifiListArrayAdapter) {
        WifiListFragment fragment = new WifiListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("ITEMS", wifiListArrayAdapter);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(mActivity.getListAdapter());
        mActivity.discoverPeers();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mActivity.onItemClick(position);
    }
}
