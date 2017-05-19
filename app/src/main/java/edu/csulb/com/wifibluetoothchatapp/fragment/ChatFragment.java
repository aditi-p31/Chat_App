package edu.csulb.com.wifibluetoothchatapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.koushikdutta.async.http.WebSocket;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.csulb.com.wifibluetoothchatapp.ChatMessage;
import edu.csulb.com.wifibluetoothchatapp.R;
import edu.csulb.com.wifibluetoothchatapp.adapter.ChatRecycleAdapter;
import edu.csulb.com.wifibluetoothchatapp.services.SocketConnection;

public class ChatFragment extends Fragment {
    private static final int RESULT_LOAD_IMG = 10;
    private EditText editText;
    private RecyclerView recyclerView;
    private ImageButton bSend;
    private Button bRecord, bAttach;

    private ChatRecycleAdapter chatRecycleAdapter;
    private List<ChatMessage> chatMessages;

    //Recorder
    private MediaRecorder recorder = null;
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private boolean flag;
    private String audiofilePath;


    private OnFragmentInteractionListener mListener;
    private SocketConnection.OnSocketCallbackListener onSocketCallbackListener = new SocketConnection.OnSocketCallbackListener() {
        @Override
        public void onStringAvailable(String data, WebSocket webSocket) {
            onDataReceive(data);
        }
    };;

    public ChatFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setOnSocketCallbackListener(onSocketCallbackListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mListener.setOnSocketCallbackListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wi_fi_direct_chat, container, false);
        editText = (EditText) view.findViewById(R.id.etMessage);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvChatLog);
        bSend = (ImageButton) view.findViewById(R.id.bSend);
        bAttach = (Button) view.findViewById(R.id.bAttach);
        bRecord = (Button) view.findViewById(R.id.bRecord);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        chatMessages = new ArrayList<>();
        chatRecycleAdapter = new ChatRecycleAdapter(chatMessages);
        recyclerView.setAdapter(chatRecycleAdapter);

        //Audio flag
        flag = false;

        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = editText.getText().toString();
                editText.setText("");
                sendText(data);
            }
        });

        bAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
//                imageView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            }
        });

        bRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag==false)
                {
                    startRecording();
                    bRecord.setText("Stop Recording");
                    flag=true;
                }
                else
                {
                    stopRecording();
                    bRecord.setText("Record and Send");
                    flag=false;
                }

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onDataReceive(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            ChatMessage chatMessage=null;
            if(jsonObject.has("AUDIO"))
            {
                 chatMessage = new ChatMessage(false, jsonObject.optString("TEXT"), stringToFile(jsonObject.optString("AUDIO")));
            }
            else
            {
                 chatMessage = new ChatMessage(false, jsonObject.optString("TEXT"), stringToBitmap(jsonObject.optString("IMAGE")));
            }

//            switch (jsonObject.getInt("TYPE")) {
//                case 1:
//                    chatMessage = new ChatMessage(false, jsonObject.getString("DATA"), null);
//                    break;
//                case 2:
//            }
            if (chatMessages != null) {
                chatMessages.add(chatMessage);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatRecycleAdapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap stringToBitmap(String data) {
        if (data.isEmpty()) return null;
        byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public File stringToFile(String data){
        byte [] decoded= Base64.decode(data, Base64.DEFAULT);
        File f = new File(getFilename());
        try {
            FileOutputStream os = new FileOutputStream(f, true);
            os.write(decoded);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public void sendData(int type, String text, String imageText, String audioText) {
        try {
            chatRecycleAdapter.notifyDataSetChanged();
            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("TYPE", type);
            jsonObject.put("TEXT", text);
            jsonObject.put("IMAGE", imageText);
            jsonObject.put("AUDIO", audioText);
            mListener.sendData(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                sendPicture(selectedImageBitmap);
//                imageView.setImageBitmap(selectedImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getActivity(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public void sendText(String msg) {
        sendData(1, msg, null, null);
        chatMessages.add(new ChatMessage(true, msg, (Bitmap) null));
    }

    public void sendPicture(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);

        sendData(2, editText.getText().toString(), encodedImage, null);
        chatMessages.add(new ChatMessage(true, editText.getText().toString(), bitmap));
    }

    public void sendAudio(String audio) {
        sendData(3, editText.getText().toString(), null, audio);
        chatMessages.add(new ChatMessage(true, editText.getText().toString(), new File(audiofilePath)));
    }


    public interface OnFragmentInteractionListener {
        void sendData(String data);
        void setOnSocketCallbackListener(SocketConnection.OnSocketCallbackListener onSocketCallbackListener);
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(){
        if (recorder!=null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
            File f = new File(audiofilePath);
            try {
                byte[] bytes = FileUtils.readFileToByteArray(f);
                String data = Base64.encodeToString(bytes,Base64.DEFAULT);
                sendAudio(data);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        audiofilePath = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4";
        return (audiofilePath);
    }
}
