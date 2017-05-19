package edu.csulb.com.wifibluetoothchatapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class CustomList extends BaseAdapter {

    Context ctx;
    ArrayList<messagedata> mds;
    LayoutInflater layoutInflater = null;

    public CustomList(Context _ctx, ArrayList<messagedata> _mds)
    {
        ctx = _ctx;
        mds = _mds;
        layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mds.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        Holder mHolder;


        if(rowView==null){
            rowView = layoutInflater.inflate(R.layout.layout_customlist, null);
            mHolder = new Holder();
            mHolder.listaudio= (Button) rowView.findViewById(R.id.listaudio);
            mHolder.listimage=(ImageView) rowView.findViewById(R.id.listimage);
            mHolder.listmessage=(TextView) rowView.findViewById(R.id.listtext);
//            mHolder.emailbody_tv=(TextView) rowView.findViewById(R.id.emailbody);
            rowView.setTag(mHolder);
        }
        else
        {
            mHolder = (Holder)rowView.getTag();
        }


        final messagedata md = mds.get(position);

        if(md.file==null)
        {
            mHolder.listaudio.setVisibility(View.GONE);
        }
        else
        {
            mHolder.listaudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Play Audio
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(md.file.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        if(md.bitmap==null){
            mHolder.listimage.setVisibility(View.GONE);
        }
        else
        {
            mHolder.listimage.setImageBitmap(md.bitmap);
        }
        mHolder.listmessage.setText(md.message);
        return rowView;
    }

    static class Holder{
        TextView listmessage;
        ImageView listimage;
        Button listaudio;
    }

    public void clearData() {
        // clear the data
        mds.clear();
    }
}
