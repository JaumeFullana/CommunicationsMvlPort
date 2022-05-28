package controller;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import Audio.Audio;
import communications.CommunicationController;
import communications.R;


public class ControllerActivity extends AppCompatActivity {

    private CommunicationController controller;
    private String ip;
    private Audio audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_main);
    }

    public CommunicationController getController() {
        return controller;
    }

    public void setController(CommunicationController controller) {
        this.controller = controller;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void playSound(View view) {
        audio = new Audio(this, R.raw.audio);
    }

    public void pauseSound(View view) {
        audio.pause();
    }

    public void stopSound(View view) {
        audio.stop();
    }
}
