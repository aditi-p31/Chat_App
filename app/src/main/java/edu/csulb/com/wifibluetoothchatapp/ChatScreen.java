package edu.csulb.com.wifibluetoothchatapp;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class ChatScreen extends AppCompatActivity {

    private TextView status;
    private Button btnConnect, imagesend, audiosend;
    private ListView listView;
    private Dialog dialog;
    private TextInputLayout inputLayout;
//    private ArrayAdapter<String> chatAdapter;
    CustomList customList;
//    private ArrayList<String> chatMessages;
    private ArrayList<messagedata> chatMessages2;
    private BluetoothAdapter bluetoothAdapter;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;

    private JSONObject jsonObject;
    private static final int RESULT_LOAD_IMAGE=999;

    //Recorder
    private MediaRecorder recorder = null;
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private boolean flag;
    private String audiofilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        findViewsByIds();
        jsonObject = new JSONObject();
        //Audio flag
        flag = false;

        //check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //show bluetooth devices dialog when click connect button
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });

        //set chat adapter
//        chatMessages = new ArrayList<>();
//        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
//        listView.setAdapter(chatAdapter);

        chatMessages2 = new ArrayList<messagedata>();
        customList = new CustomList(getApplicationContext(),chatMessages2);
        listView.setAdapter(customList);
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
                sendMessage(data,3);
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

    private void sendMessage(String message, int type) {
        //Text Message
        if(type==1)
        {
            if (chatController.getState() != ChatController.STATE_CONNECTED) {
                Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (message.length() > 0) {
                try {
                    jsonObject.put("type",type);
                    jsonObject.put("data",message);
                    String send = jsonObject.toString();
                    chatController.write(send);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try {
                jsonObject.put("type",type);
                jsonObject.put("data",message);
                String send = jsonObject.toString();
                chatController.write(send);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            messagedata md = new messagedata();
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    try {
                        JSONObject jsonObject = new JSONObject(writeMessage);
                        int type = jsonObject.getInt("type");
                        if(type==1)
                        {
//                            chatMessages.add(connectingDevice.getName() + ":  " + jsonObject.get("data"));
                            md = new messagedata();
                            md.message="Me: " + jsonObject.get("data");
                            md.bitmap=null;
                            md.file=null;
                            chatMessages2.add(md);

                        }
                        else if(type==2)
                        {
                            String encodedString = jsonObject.getString("data");
                            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
                            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
//                            chatMessages.add(connectingDevice.getName() + ":  " + "Image Received");
                            md = new messagedata();
                            md.message="Me:";
                            md.bitmap=bitmap;
                            md.file=null;
                            chatMessages2.add(md);
                        }
                        else if(type==3)
                        {
                            String encodedString = jsonObject.getString("data");
                            byte [] decoded= Base64.decode(encodedString, Base64.DEFAULT);
                            File f = new File(getFilename());
                            try {
                                FileOutputStream os = new FileOutputStream(f, true);
                                os.write(decoded);
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            chatMessages.add(connectingDevice.getName() + ":  " + "Audio Received");
                            md = new messagedata();
                            md.message="Me:";
                            md.bitmap=null;
                            md.file=f;
                            chatMessages2.add(md);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                    chatMessages.add("Me: " + writeMessage);
//                    chatAdapter.notifyDataSetChanged();
                    customList.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    try {
                        JSONObject jsonObject = new JSONObject(readMessage);
                        int type = jsonObject.getInt("type");
                        if(type==1)
                        {
//                            chatMessages.add(connectingDevice.getName() + ":  " + jsonObject.get("data"));
                            md = new messagedata();
                            md.message=connectingDevice.getName() + ":  " + jsonObject.get("data");
                            md.bitmap=null;
                            md.file=null;
                            chatMessages2.add(md);

                        }
                        else if(type==2)
                        {
                            String encodedString = jsonObject.getString("data");
                            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
                            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
//                            chatMessages.add(connectingDevice.getName() + ":  " + "Image Received");
                            md = new messagedata();
                            md.message=connectingDevice.getName();
                            md.bitmap=bitmap;
                            md.file=null;
                            chatMessages2.add(md);
                        }
                        else if(type==3)
                        {
                            String encodedString = jsonObject.getString("data");
                            byte [] decoded= Base64.decode(encodedString, Base64.DEFAULT);
                            File f = new File(getFilename());
                            try {
                                FileOutputStream os = new FileOutputStream(f, true);
                                os.write(decoded);
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            chatMessages.add(connectingDevice.getName() + ":  " + "Audio Received");
                            md = new messagedata();
                            md.message=connectingDevice.getName();
                            md.bitmap=null;
                            md.file=f;
                            chatMessages2.add(md);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    customList.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setStatus(String s) {
        status.setText(s);
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds() {
        status = (TextView) findViewById(R.id.status);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        listView = (ListView) findViewById(R.id.list);
        inputLayout = (TextInputLayout) findViewById(R.id.input_layout);

        imagesend = (Button) findViewById(R.id.btn_send2);
        audiosend = (Button) findViewById(R.id.btn_send3);

        imagesend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sendImage();
                if (chatController.getState() != ChatController.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), "Connection was lost!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        audiosend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatController.getState() != ChatController.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), "Connection was lost!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(flag==false)
                {
                    startRecording();
                    audiosend.setText("Stop Recording");
                    flag=true;
                }
                else
                {
                    stopRecording();
                    audiosend.setText("Record and Send");
                    flag=false;
                }
            }
        });

        View btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputLayout.getEditText().getText().toString().equals("")) {
                    Toast.makeText(ChatScreen.this, "Please input some texts", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: here
                    sendMessage(inputLayout.getEditText().getText().toString(), 1);
                    inputLayout.getEditText().setText("");
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK && null != data) {

                    Uri pickedImage = data.getData();
                    // Let's read picked image path using content resolver
                    InputStream inputstream;
                    try {
                        inputstream = getContentResolver().openInputStream(pickedImage);
                        //the "image" received here it the image itself
                        Bitmap bitmap = BitmapFactory.decodeStream(inputstream);

                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);

                        byte [] b=baos.toByteArray();
                        String temp= Base64.encodeToString(b, Base64.DEFAULT);

                        sendMessage(temp,2);

                        //YOU CAN ASSIGN IT TO YOUR IMAGE BUTTON HERE IF YOU WANT
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };
}
