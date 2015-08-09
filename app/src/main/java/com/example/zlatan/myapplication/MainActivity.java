package com.example.zlatan.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    BluetoothAdapter mmAdapter;
    BluetoothConnector btcon;

    final byte delimiter = 33;
    int readBufferPosition = 0;

    public void sendBtMsg(String msg2send){
        try {

            BluetoothConnector.BluetoothSocketWrapper socket = btcon.connect();
            String msg = msg2send;
            OutputStream os = socket.getOutputStream();
            os.write(msg.getBytes());
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            os.flush();
            os.close();
            btcon.bluetoothSocket.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if (bondedDevices.size() > 0) {
                    Iterator iter = bondedDevices.iterator();
                    mmDevice = (BluetoothDevice) iter.next();
                }
            }
        }
        mmAdapter = blueAdapter;
        btcon = new BluetoothConnector(mmDevice, false, mmAdapter, null);

    }

    public void startRestartGame(View v)
    {
        //check bluetooth connection - to implement
        sendBtMsg("start");
        /*Button startRestartBT = (Button)findViewById(R.id.startBT);
        String state = startRestartBT.getText().toString();
        if(state.equals("Start"))
            startRestartBT.setText("Restart");
        else
            startRestartBT.setText("Start");*/
    }

    public void pauseContinueGame(View v)
    {
        //check game state - tom implement
        sendBtMsg("pause");
       /* Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
        String state = pauseContinueBT.getText().toString();
        if(state.equals("Pause"))
            pauseContinueBT.setText("Continue");
        else
            pauseContinueBT.setText("Pause");*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
