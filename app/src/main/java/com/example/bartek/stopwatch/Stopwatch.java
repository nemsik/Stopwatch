package com.example.bartek.stopwatch;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class Stopwatch extends Activity implements Serializable {

    private TextView FullTimeTextView, CurrentTimeTextView;
    private Button StartPause, LapReset;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private boolean setStart = false;

    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L,
            MillisecondTimeCurrent, StartTimeCurrent, TimeBuffCurrent, UpdateTimeCurrent = 0L;
    private int Seconds, Minutes, MilliSeconds, SecondsCurrent, MinutesCurrent, MilliSecondsCurrent;
    private int position;

    private Handler handler;

    private List<String> lapList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch_activity);



        FullTimeTextView = (TextView) findViewById(R.id.textView3);
        CurrentTimeTextView = (TextView) findViewById(R.id.textView2);

        listView = (ListView) findViewById(R.id.listview);

        StartPause = (Button) findViewById(R.id.button);
        LapReset = (Button) findViewById(R.id.button2);


        handler = new Handler();


        try {
            FileInputStream fis = openFileInput("stopwatch.time");
            ObjectInputStream in = new ObjectInputStream(fis);
            lapList = (ArrayList<String>) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, lapList);
        listView.setAdapter(adapter);
        listView.setClickable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("listview: ", "click");
                Log.d("item: ", ""+i);
                position = i;

                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), listView);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Log.d("Item click", (String) menuItem.getTitle());

                        switch ((String) menuItem.getTitle()) {
                            case "Clear All":
                                lapList.clear();
                                break;
                            case "Remove":
                                lapList.remove(position);
                        }
                        save();
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                });
                popupMenu.show();
            }
        });


        StartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setStart == false){
                    setStart = true;
                    StartPause.setText("Pause");
                    LapReset.setText("Lap");

                    //handler start

                    StartTimeCurrent = StartTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 0);
                }else{
                    setStart = false;
                    StartPause.setText("Start");
                    LapReset.setText("Reset");

                    //handler pause

                    TimeBuff += MillisecondTime;
                    TimeBuffCurrent += MillisecondTimeCurrent;
                    handler.removeCallbacks(runnable);
                }
            }
        });

        LapReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setStart == false){
                    // clear
                    MillisecondTimeCurrent = MillisecondTime = 0L ;
                    StartTimeCurrent = StartTime = 0L ;
                    TimeBuffCurrent = TimeBuff = 0L ;
                    UpdateTimeCurrent = UpdateTime = 0L ;
                    SecondsCurrent = Seconds = 0 ;
                    MinutesCurrent = Minutes = 0 ;
                    MilliSecondsCurrent = MilliSeconds = 0 ;

                    FullTimeTextView.setText("0:00:000");
                    CurrentTimeTextView.setText("0:00:000");
                }else{
                    //save lap
                    lapList.add((String) FullTimeTextView.getText());

                    //clear current time
                    StartTimeCurrent = SystemClock.uptimeMillis();
                    TimeBuffCurrent = 0L;
                    adapter.notifyDataSetChanged();
                    listView.setSelection(adapter.getCount()-1);

                    save();

                }
            }
        });

    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            MillisecondTimeCurrent = SystemClock.uptimeMillis() - StartTimeCurrent;

            UpdateTime = TimeBuff + MillisecondTime;
            UpdateTimeCurrent = TimeBuffCurrent + MillisecondTimeCurrent;

            Seconds = (int) (UpdateTime / 1000);
            SecondsCurrent = (int) (UpdateTimeCurrent / 1000);

            Minutes = Seconds / 60;
            MinutesCurrent = SecondsCurrent / 60;

            Seconds = Seconds % 60;
            SecondsCurrent = SecondsCurrent % 60;

            MilliSeconds = (int) (UpdateTime % 1000);
            MilliSecondsCurrent = (int) (UpdateTimeCurrent % 1000);

            FullTimeTextView.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            CurrentTimeTextView.setText("" + MinutesCurrent + ":"
                    + String.format("%02d", SecondsCurrent) + ":"
                    + String.format("%03d", MilliSecondsCurrent));



            handler.postDelayed(this, 0);
        }
    };

    private void save(){
        try {
            FileOutputStream fos = openFileOutput("stopwatch.time", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(lapList);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
