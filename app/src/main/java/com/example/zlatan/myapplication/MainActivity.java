package com.example.zlatan.myapplication;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

public class MainActivity extends ActionBarActivity {

    BluetoothConnector.BluetoothSocketWrapper mmSocket = null;
    BluetoothDevice mmDevice = null;
    BluetoothAdapter mmAdapter;
    BluetoothConnector btcon;
    OutputStream mmOs;
    InputStream mmIs;

    int readBufferPosition = 0;
    volatile boolean stopWorker;
    byte[] readBuffer;
    Thread workerThread;

    String command_left = "0";
    String command_right = "1";
    String command_pause = "2";
    String command_continue = "3";
    String command_start = "4";
    String command_restart = "5";
    String command_shoot = "6";

    public void sendBtMsg(String msg2send) {
        try {
            String msg = msg2send;
            mmOs.write(msg.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startRestartGame(View v)
    {
        if(mmSocket != null) {
            if(mmSocket.isConnected()) {
                Button startRestartBT = (Button)findViewById(R.id.startBT);
                String state = startRestartBT.getText().toString();
                if(state.equals("Start")) {
                    sendBtMsg(command_start);
                    startRestartBT.setText("Restart");
                    beginListenForData();
                }
                else {
                    sendBtMsg(command_restart);
                    startRestartBT.setText("Restart");
                    Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
                    pauseContinueBT.setText("Pause");
                }
            }
            else {
                lostConnection();
            }
        }
        else {
            lostConnection();
        }
    }

    public void pauseContinueGame(View v) {
        if(mmSocket != null) {
            if(mmSocket.isConnected()) {
                Button startRestartBT = (Button)findViewById(R.id.startBT);
                String state = startRestartBT.getText().toString();
                if(state.equals("Start")) {
                    new AlertDialog.Builder(this)
                            .setTitle("No active game")
                            .setMessage("You have to start the game")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
                else {
                    Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
                    String state1 = pauseContinueBT.getText().toString();
                    if(state1.equals("Pause")) {
                        sendBtMsg(command_pause);
                        pauseContinueBT.setText("Continue");
                    }
                    else {
                        sendBtMsg(command_continue);
                        pauseContinueBT.setText("Pause");
                    }
                }
            }
            else {
                lostConnection();
            }
        }
        else {
            lostConnection();
        }
    }

    public void goLeft(View v) {
        if(mmSocket != null) {
            if(mmSocket.isConnected()) {
                sendBtMsg(command_left);
            }
            else {
                lostConnection();
            }
        }
        else {
            lostConnection();
        }
    }

    public void goRight(View v) {
        if(mmSocket != null) {
            if(mmSocket.isConnected()) {
                sendBtMsg(command_right);
            }
            else {
                lostConnection();
            }
        }
        else {
            lostConnection();
        }
    }

    public void shoot(View v) {
        if(mmSocket != null) {
            if(mmSocket.isConnected()) {
                sendBtMsg(command_shoot);
            }
            else {
                lostConnection();
            }
        }
        else {
            lostConnection();
        }
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 33; //ASCII code for a exclamation mark character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmIs.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmIs.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            //TextView myLabel = (TextView) findViewById(R.id.textView3);
                                            //myLabel.setText(data);
                                            if(data.equals("G")) {
                                                finishGame();
                                            }
                                            else {
                                                try {
                                                    int a = Integer.parseInt(data);
                                                    TextView myLabel1 = (TextView) findViewById(R.id.scoreLB);
                                                    myLabel1.setText(data);
                                                }
                                                catch(NumberFormatException e) {
                                                    // do nothing
                                                }
                                            }
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void finishGame() {
        Button startRestartBT = (Button)findViewById(R.id.startBT);
        startRestartBT.setText("Start");
        Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
        pauseContinueBT.setText("Pause");

        TextView highscoreTV = (TextView) findViewById(R.id.highscoreLB);
        TextView scoreTV = (TextView) findViewById(R.id.scoreLB);
        int highscore = Integer.parseInt(highscoreTV.getText().toString());
        int score = Integer.parseInt(scoreTV.getText().toString());

        String toAdd = "";
        if(score > highscore) {
            highscoreTV.setText(scoreTV.getText());
            toAdd = ", a new highscore!";
        }

        new AlertDialog.Builder(this)
                .setTitle("Game over")
                .setMessage("You earned " + score + " points" + toAdd)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
        scoreTV.setText("0");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_connect) {
            connectArduino();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectArduino() {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if (bondedDevices.size() > 0) {
                    Iterator iter = bondedDevices.iterator();
                    mmDevice = (BluetoothDevice) iter.next();
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
                else {
                    new AlertDialog.Builder(this)
                            .setTitle("Connection problem")
                            .setMessage("You have to pair with Arduino first")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(R.drawable.ic_not_paired)
                            .show();
                }
            }
            else {
                new AlertDialog.Builder(this)
                        .setTitle("Connection problem")
                        .setMessage("You have to turn Bluetooth on")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_turned_off)
                        .show();
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Connection success!")
                .setMessage("Devices connected")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_connected)
                .show();

    }

    private void lostConnection() {
        Button startRestartBT = (Button)findViewById(R.id.startBT);
        startRestartBT.setText("Start");
        Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
        pauseContinueBT.setText("Pause");
        TextView scoreTV = (TextView) findViewById(R.id.scoreLB);
        scoreTV.setText("0");
        new AlertDialog.Builder(this)
                .setTitle("No connection!")
                .setMessage("You have to (re)connect with Arduino!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_turned_off)
                .show();
    }

}
