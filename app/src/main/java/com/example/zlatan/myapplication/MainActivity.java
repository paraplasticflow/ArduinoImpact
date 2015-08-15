package com.example.zlatan.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothConnector.BluetoothSocketWrapper mmSocket;
    BluetoothDevice mmDevice = null;
    BluetoothAdapter mmAdapter;
    BluetoothConnector btcon;
    OutputStream mmOs;
    InputStream mmIs;

    //final byte delimiter = 33;
    int readBufferPosition = 0;
    volatile boolean stopWorker;
    byte[] readBuffer;
    Thread workerThread;


    public void sendBtMsg(String msg2send){
        try {

            //mmSocket = btcon.connect();


            String msg = msg2send;
            //OutputStream os = mmSocket.getOutputStream();
            mmOs.write(msg.getBytes());
            /*try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            //mmOs.flush();
            //os.close();
            //btcon.bluetoothSocket.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pauseButton = (Button) findViewById(R.id.pauseBT);

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
        try {
            mmSocket = btcon.connect();
            mmOs = mmSocket.getOutputStream();
            mmIs = mmSocket.getInputStream();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public void startRestartGame(View v)
    {
        //check bluetooth connection - to implement
        sendBtMsg("1");
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

        //sendBtMsg("0");
        /*Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
        String state = pauseContinueBT.getText().toString();
        if(state.equals("Pause"))
            pauseContinueBT.setText("Continue");
        else
            pauseContinueBT.setText("Pause");*/
        beginListenForData();

    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 33; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmIs.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmIs.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            TextView myLabel = (TextView) findViewById(R.id.scoreLB);
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
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
