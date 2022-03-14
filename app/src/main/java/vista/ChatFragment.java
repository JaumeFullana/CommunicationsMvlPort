package vista;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import communications.ConnectionInterface;
import communications.ProtocolDataPacket;
import communications.R;


public class ChatFragment extends Fragment implements ConnectionInterface {

    private MainActivity mainActivity;
    private TextView textNick;
    private Button buttonEnviar;
    private Spinner spinnerMacs;
    private EditText editTextMessage;
    private TextView textChat;
    private ArrayList<String> macsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_chat, container, false);
        this.mainActivity=(MainActivity)getActivity();
        this.macsList=new ArrayList<>();
        this.textNick=view.findViewById(R.id.textNick);
        this.buttonEnviar=view.findViewById(R.id.buttonEnviar);
        this.spinnerMacs=view.findViewById(R.id.spinner);
        this.editTextMessage=view.findViewById(R.id.editTextMessage);
        this.textChat=view.findViewById(R.id.textChat);

        this.mainActivity.getController().addAllListeners(this);
        this.textNick.setText("Nick: "+this.mainActivity.getController().getLocalMAC());

        this.buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProtocolDataPacket datos=mainActivity.getController().createPacket(
                        spinnerMacs.getSelectedItem().toString(),666,editTextMessage.getText().toString());
                textChat.append("\n - "+editTextMessage.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.getController().sendMessage(datos);
                    }
                }).start();
            }
        });

        return view;
    }

    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {
        if (packet.getTargetID().equals(this.mainActivity.getController().getLocalMAC())){
            if (packet.getId()==666){
                this.textChat.append("\n"+packet.getSourceID()+": "+((String)packet.getObject()));
            }
        }
    }

    @Override
    public void onConnectionAccept(String mac) {
        boolean repeated=false;
        int i=0;
        while (!repeated && i<this.macsList.size()){
            if (mac.equals(this.macsList.get(i))){
                repeated=true;
            }
            i++;
        }

        if (!repeated){
            this.macsList.add(mac);
        }

        String [] macsArray=new String[this.macsList.size()];
        for (i=0; i<this.macsList.size();i++){
            macsArray[i]=this.macsList.get(i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item,macsArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                spinnerMacs.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onConnectionClosed(String mac) {
        boolean found=false;
        int i=0;
        while (!found && i<this.macsList.size()){
            if (mac.equals(this.macsList.get(i))){
                this.macsList.remove(i);
                found=true;
            }
            i++;
        }

        String [] macsArray=new String[this.macsList.size()];
        for (i=0; i<this.macsList.size();i++){
            macsArray[i]=this.macsList.get(i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item,macsArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                spinnerMacs.setAdapter(adapter);
            }
        });
    }

}