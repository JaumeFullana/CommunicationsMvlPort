package controller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import communications.ConnectionInterface;
import communications.ProtocolDataPacket;
import communications.R;

public class ControllerFragment extends Fragment implements ConnectionInterface {

    private Joystick joystick;
    private ControllerActivity activity;
    private String mac;
    private int lastAngle;
    private int lastStrength;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        this.activity = ((ControllerActivity)this.getActivity());
        joystick = view.findViewById(R.id.joystickView);
        this.activity.getController().addAllListeners(this);
        joystick.setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                Log.d("angle", ""+angle);
                Log.d("strength", ""+strength);
                if (mac != null) {
                    if (lastAngle!=angle || lastStrength!=strength) {
                        int[] movement = {angle, strength};
                        ProtocolDataPacket datos = activity.getController().createPacket(mac, 777, movement);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.getController().sendMessage(datos);
                            }
                        }).start();
                        lastStrength=strength;
                        lastAngle=angle;
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {

    }

    @Override
    public void onConnectionAccept(String mac) {
        this.mac=mac;
    }

    @Override
    public void onConnectionClosed(String mac) {

    }
}