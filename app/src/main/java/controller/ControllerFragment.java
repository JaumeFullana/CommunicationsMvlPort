package controller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toolbar;

import communications.ConnectionInterface;
import communications.ProtocolDataPacket;
import communications.R;

public class ControllerFragment extends Fragment implements ConnectionInterface {

    private Joystick joystick;
    private Button fire;
    private ControllerActivity controllerActivity;
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

        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        this.controllerActivity = ((ControllerActivity)this.getActivity());
        this.controllerActivity.getController().addAllListeners(this);

        if (controllerActivity.getIp()!=null && !controllerActivity.getIp().isEmpty()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    controllerActivity.getController().connectToIp(controllerActivity.getIp());
                }
            }).start();
        }

        joystick = view.findViewById(R.id.joystickView);
        fire = view.findViewById(R.id.fire);
        this.connected = false;
        joystick.setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (mac != null) {
                    /*if (!connected){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                controllerActivity.getController().sendMessage(controllerActivity.getController().createPacket(mac, 51, null));
                            }
                        }).start();
                        connected = true;
                    }*/

                    if (lastAngle!=angle || lastStrength!=strength) {
                        //Log.d("move", "angle = "+angle+", strength = "+strength);
                        ProtocolDataPacket datos = controllerActivity.getController().createPacket(mac, 50, new int[] {strength, angle});

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                controllerActivity.getController().sendMessage(datos);
                            }
                        }).start();

                        lastStrength=strength;
                        lastAngle=angle;
                    }
                }
            }
        });

        fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProtocolDataPacket datos = controllerActivity.getController().createPacket(mac, 314, null);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        controllerActivity.getController().sendMessage(datos);
                    }
                }).start();
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