package com.example.admin.navisuber;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView tv_time = getActivity().findViewById(R.id.tv_time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String time = String.valueOf(hourOfDay)+":"+String.valueOf(minute);
        try {
            Date timePicker = timeFormat.parse(time);
            tv_time.setText(timeFormat.format(timePicker).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
