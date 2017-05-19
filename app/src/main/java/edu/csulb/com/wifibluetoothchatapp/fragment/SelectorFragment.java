package edu.csulb.com.wifibluetoothchatapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.csulb.com.wifibluetoothchatapp.R;

public class SelectorFragment extends Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;
    private Button text, bConnect;
//    private TextView tvStatus;

    public SelectorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selector, container, false);
        text = (Button) view.findViewById(R.id.send_text);
        bConnect = (Button) view.findViewById(R.id.bConnectWifi);

        text.setOnClickListener(this);
        bConnect.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        mListener.onSelectorItemClick(v);
    }

    public void setTvStatus(String status) {
//        tvStatus.setText(status);
    }

    public interface OnFragmentInteractionListener {
        void onSelectorItemClick(View v);
    }
}
