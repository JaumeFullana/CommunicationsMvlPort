package vista;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import communications.CommunicationController;
import communications.R;


public class InputFragment extends Fragment {

    private EditText ipEditText;
    private Button connectButton;
    private Thread backEndThread;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_input, container, false);

        this.ipEditText=(EditText)view.findViewById(R.id.editTextIP);
        this.connectButton=(Button)view.findViewById(R.id.connectButton);
        this.mainActivity=(MainActivity)getActivity();

        backEndThread=new Thread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setController(new CommunicationController(mainActivity.getApplicationContext()));
            }
        });
        backEndThread.start();

        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip=ipEditText.getText().toString();
                if (ip!=null && !ip.isEmpty()){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.getController().connectToIp(ip);
                        }
                    }).start();
                }
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, ChatFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }
}