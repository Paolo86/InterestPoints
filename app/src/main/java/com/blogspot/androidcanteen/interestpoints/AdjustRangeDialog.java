package com.blogspot.androidcanteen.interestpoints;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntegerRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Paolo on 20/04/2017.
 */

public class AdjustRangeDialog {



    Dialog dialog;

    Activity act;
   TextView textValuel;
    SeekBar slider;
    int originalValue;



    public AdjustRangeDialog(final Activity act)
    {

        this.act = act;



        LayoutInflater inflater = (LayoutInflater)act.getLayoutInflater();
        View view = inflater.inflate(R.layout.adjust_range_dialog, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(act)
                .create();

        alertDialog.setTitle("Adjust range");

        alertDialog.setView(view);
        alertDialog.setCancelable(false);

        textValuel = (TextView)view.findViewById(R.id.sliderValue);
        slider = (SeekBar)view.findViewById(R.id.rangeSlider);



        SetText();

        originalValue = MyOptions.meterRange;


        slider.setMax(MyOptions.MAX_RANGE - MyOptions.MIN_RANGE);
        slider.setProgress(originalValue - MyOptions.MIN_RANGE);

        //slider.setMax(MyOptions.MAX_RANGE );

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {



                MyOptions.meterRange = progress + MyOptions.MIN_RANGE;
                if(MyOptions.meterRange >= MyOptions.MAX_RANGE)
                    MyOptions.meterRange = MyOptions.MAX_RANGE;

                SetText();

                GlobalVariables.LogWithTag("Progress changed " + MyOptions.meterRange);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {

               //Save to shared preferences

                        SharedPreferences sp = act.getSharedPreferences("Range", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();

                        edit.putInt("Range",MyOptions.meterRange);
                        edit.commit();
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MyOptions.meterRange = originalValue;
                SetText();
                dialog.dismiss();
            }
        });


            alertDialog.show();




    }

    private void SetText() {
        textValuel.setText(String.valueOf(MyOptions.meterRange) + " (meters)");
    }
}
