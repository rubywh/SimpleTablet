package ruby.simpletabletversion;

/**
 * Stripped back tablet version of accelerometer project
 */


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Display start button, on click start Service for sensing
 */

public class simpleActivity extends Activity implements SensorEventListener {
    Sensor senAccelerometer;
    private SensorManager senSensorManager;

    private FileWriter writer;
    private BufferedWriter bufferedWriter;
    ArrayList<String> arrayList;

    private static final String TAG = "simpleActivity";
    private Button mBtnView;
    private Button mBtnView2;
    private Button mBtnView3;

    Thread consThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sense_layout);
        mBtnView = (Button) findViewById(R.id.btn);
        mBtnView2 = (Button) findViewById(R.id.btn2);
        mBtnView3 = (Button) findViewById(R.id.btn3);
        mBtnView3.setVisibility(View.INVISIBLE);
        mBtnView2.setVisibility(View.INVISIBLE);

        arrayList = new ArrayList<>();

        //Set up the file
        try {
            makeFile("test", "test", "test", "test");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void stopSensing() throws IOException, InterruptedException {
        Log.d(TAG, "StopSensing");

        //Unregister listener
        if (senSensorManager != null) {
            senSensorManager.unregisterListener(this);
        }

        //Start file writing thread
        consThread = new Thread(new Consumer(bufferedWriter));
        consThread.start();
        Log.d(TAG, "start cons thread called");
        consThread.join();

        //Close writing resources
        bufferedWriter.close();
        Log.d(TAG, "buffered writer closed");
        writer.close();

        //Display Finish button
        mBtnView3.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            arrayList.add(event.timestamp + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + "\n");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void makeFile(String trial, String gender, String age, String height) throws IOException {
        Log.d(TAG, "MakeFile");

        writer = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/" + trial + "_" + gender + "_" +
                age + "_" + height + "_accelerometer.dat");
        bufferedWriter = new BufferedWriter(writer);
    }

    /*Register the sensor*/
    public void onStartClick(View view) {

        mBtnView2.setVisibility(View.VISIBLE);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        //100000 = 10HZ
        //Make Start button invisible
        mBtnView.setVisibility(View.INVISIBLE);
    }

    public void onStopClick(View view) throws IOException, InterruptedException {
        stopSensing();
        mBtnView2.setVisibility(View.INVISIBLE);
    }

    //End the application
    public void onFinishClick(View view) {
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    /* Consumer Thread to write to file post-sensing*/
    class Consumer implements Runnable {
        private static final String TAG = "ConsumerThread";
        private BufferedWriter bufferedWriter;

        public Consumer(BufferedWriter buf) {
            this.bufferedWriter = buf;

            Log.d(TAG, "Consumer Created");
        }

        @Override
        public void run() {
            Log.d(TAG, "run");
            for (String str : arrayList) {

                try {
                    bufferedWriter.append(str);
                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}





