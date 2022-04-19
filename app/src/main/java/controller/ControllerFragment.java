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
    private boolean connected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.connected = false;
        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        this.activity = ((ControllerActivity)this.getActivity());
        joystick = view.findViewById(R.id.joystickView);
        this.activity.getController().addAllListeners(this);
        joystick.setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (mac != null) {

                    if (!connected){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.getController().sendMessage(activity.getController().createPacket(mac, 51, null));
                            }
                        }).start();
                        connected = true;
                    }

                    if (lastAngle!=angle || lastStrength!=strength) {
                        Log.d("move", "angle = "+angle+", strength = "+strength);
                        ProtocolDataPacket datos = activity.getController().createPacket(mac, 50, new int[] {strength, angle});
                        /*try {
                            activity.getPacketSender().packetsList.put(datos);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

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