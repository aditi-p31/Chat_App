package edu.csulb.com.wifibluetoothchatapp;

import android.graphics.Bitmap;

import java.io.File;


public class ChatMessage {
    private boolean isSent;
    private String text;
    private Bitmap image;
    private File file;


    public ChatMessage(boolean isSent, String text, Bitmap image) {
        this.isSent = isSent;
        this.text = text;
        this.image = image;
    }

    public ChatMessage(boolean isSent, String text, File file) {
        this.isSent = isSent;
        this.text = text;
        this.file = file;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public void setFile(File file){
        this.file= file;
    }
    public File getFile(){
        return file;
    }
}
