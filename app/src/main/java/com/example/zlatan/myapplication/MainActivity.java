package com.example.zlatan.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startRestartGame(View v)
    {
        //check bluetooth connection - to implement
        Button startRestartBT = (Button)findViewById(R.id.startBT);
        String state = startRestartBT.getText().toString();
        if(state.equals("Start"))
            startRestartBT.setText("Restart");
        else
            startRestartBT.setText("Start");
    }

    public void pauseContinueGame(View v)
    {
        //check game state - tom implement
        Button pauseContinueBT = (Button)findViewById(R.id.pauseBT);
        String state = pauseContinueBT.getText().toString();
        if(state.equals("Pause"))
            pauseContinueBT.setText("Continue");
        else
            pauseContinueBT.setText("Pause");
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
