package com.mad.spycameradetection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Math.sqrt;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Magnetometer extends AppCompatActivity implements SensorEventListener {


    private TextView magR, show_conditions, x_cor, y_cor, z_cor ;
    SpeedometerView Speed;

    MediaPlayer mediaPlayer;


    private double magD;

    private Sensor magnetometer ;

    private SensorManager sensorManager; 
    Boolean flag = false;
    double prevx = 0f;

    String prevStarted = "yesMagnet";
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!sharedpreferences.getBoolean(prevStarted, false)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(prevStarted, Boolean.TRUE);
            editor.apply();

            final AlertDialog.Builder alert = new AlertDialog.Builder(Magnetometer.this);
            View mView = getLayoutInflater().inflate(R.layout.custom_dialog,null);

            Button btn_okay = (Button)mView.findViewById(R.id.btn_okay);
            TextView textView=mView.findViewById (R.id.textFormodal);
            textView.setText ("\n" +
                    "Move the phone near all the suspected areas to detect the hidden camera \n" +
                    "\n" +
                    "Read detailed instructions by tapping on the button located at the bottom right corner of the screen");
            alert.setView(mView);
            final AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            btn_okay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer);

        FloatingActionButton floatingActionButton=findViewById (R.id.magnetoInst);
        floatingActionButton.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                startActivity( new Intent ( getApplicationContext(), MagBtnInst.class ) );

            }
        });
        Speed = (SpeedometerView)findViewById(R.id.speedometer);
        Speed.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        Speed.setMaxSpeed(100);
        Speed.setMajorTickStep(10);
        Speed.setMinorTicks(0);

        Speed.addColoredRange(0, 10, Color.GREEN);
        Speed.addColoredRange(10, 20, Color.YELLOW);
        Speed.addColoredRange(20, 30, Color.RED);
        Speed.addColoredRange(30, 40, Color.GREEN);
        Speed.addColoredRange(40, 50, Color.YELLOW);
        Speed.addColoredRange(50, 60, Color.RED);
        Speed.addColoredRange(60, 70, Color.GREEN);
        Speed.addColoredRange(70, 80, Color.YELLOW);
        Speed.addColoredRange(80, 90, Color.RED);
        Speed.addColoredRange(90, 100, Color.GREEN);

        magR = (TextView) findViewById(R.id.value);
        show_conditions = findViewById( R.id.show_conditions );
        x_cor = findViewById( R.id.x_cor );
        y_cor = findViewById( R.id.y_cor );
        z_cor = findViewById( R.id.z_cor );
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer != null){
            sensorManager.registerListener(Magnetometer.this,magnetometer,SensorManager.SENSOR_DELAY_NORMAL);
        }else {
            magR.setText("Magnetometer not Supported");
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;


        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            double x;

            x = sqrt (event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]);

            BigDecimal bd = new BigDecimal(x).setScale(0, RoundingMode.HALF_UP);
            double newx = bd.doubleValue();

            if(newx>0 && newx<100){
                Speed.setSpeed(newx, 1, 1);

            }else if(newx>=100){
                Speed.setSpeed (100,1,1);
            }
            magR.setText(Double.toString(newx) + " μT");

            BigDecimal bdx = new BigDecimal(event.values[0]).setScale(0, RoundingMode.HALF_UP);
            double new_x = bdx.doubleValue();
            x_cor.setText(Double.toString(new_x));
            x_cor.setBackgroundColor( Color.GREEN );

            BigDecimal bdy = new BigDecimal(event.values[1]).setScale(0, RoundingMode.HALF_UP);
            double new_y = bdy.doubleValue();
            y_cor.setText(Double.toString(new_y));
            y_cor.setBackgroundColor( Color.YELLOW );


            BigDecimal bdz = new BigDecimal(event.values[2]).setScale(0, RoundingMode.HALF_UP);
            double new_z = bdz.doubleValue();
            z_cor.setText(Double.toString(new_z));
            z_cor.setBackgroundColor( Color.RED );


            double Pd=70d;
            double Fd=90d;
            if(Double.compare (x,Pd)>0 && Double.compare (x,Fd)<0) {
                mediaPlayer=null;
                mediaPlayer = MediaPlayer.create(this, R.raw.beep);
                mediaPlayer.start();
                show_conditions.setText( "Electronic device detected" );

            }
            else if(Double.compare (x,Fd)>0){
                mediaPlayer=null;
                mediaPlayer = MediaPlayer.create(this, R.raw.beepd);
                mediaPlayer.start();
                show_conditions.setText( " Device detected" );

            }else {
                mediaPlayer = null;
                show_conditions.setText ("No electronic device detected");
            }



        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}