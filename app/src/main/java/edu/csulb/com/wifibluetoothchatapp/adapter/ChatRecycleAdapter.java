package edu.csulb.com.wifibluetoothchatapp.adapter;

import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import edu.csulb.com.wifibluetoothchatapp.ChatMessage;
import edu.csulb.com.wifibluetoothchatapp.R;


public class ChatRecycleAdapter extends RecyclerView.Adapter<ChatRecycleAdapter.ViewHolder> {
    private List<ChatMessage> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMessage;
        public ImageView ivPhoto;
        public Button ivAudio;

        public ViewHolder(View v) {
            super(v);

            tvMessage = (TextView) v.findViewById(R.id.tvMessage);
            ivPhoto = (ImageView) v.findViewById(R.id.ivPhoto);
            ivAudio = (Button) v.findViewById(R.id.ivAudio);

        }
    }

    public ChatRecycleAdapter(List<ChatMessage> chatMessageList) {
        mDataset = chatMessageList;
    }

    @Override
    public ChatRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ChatMessage chatMessage = mDataset.get(position);
        if (chatMessage.getImage() != null) {
            holder.ivPhoto.setImageBitmap(chatMessage.getImage());
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.tvMessage.setText("");
        }
        else if(chatMessage.getFile() != null){
            holder.ivAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//Play Audio
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(chatMessage.getFile().getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            holder.ivAudio.setVisibility(View.VISIBLE);
            holder.tvMessage.setText("");
        }
        else {
            holder.ivPhoto.setVisibility(View.GONE);
            holder.ivAudio.setVisibility(View.GONE);
            holder.tvMessage.setText(chatMessage.getText());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
