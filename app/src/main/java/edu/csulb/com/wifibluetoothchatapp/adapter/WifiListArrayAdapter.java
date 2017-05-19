package edu.csulb.com.wifibluetoothchatapp.adapter;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;


public class WifiListArrayAdapter extends ArrayAdapter<WifiP2pDevice> implements Serializable {
    private List<WifiP2pDevice> list;
    private Activity context;

    public WifiListArrayAdapter(@NonNull Activity context, @NonNull List<WifiP2pDevice> list) {
        super(context, android.R.layout.simple_list_item_1, list);

        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(android.R.id.text1);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.text.setText(list.get(position).deviceName);

        return rowView;
    }

    static class ViewHolder {
        public TextView text;
    }
}
