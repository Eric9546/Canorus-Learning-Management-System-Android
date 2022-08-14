package com.example.canoruslearningmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;

public class ViewLog extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    private DatePickerDialog mDatePickerDialog;
    private Button dateButton;

    Spinner mSpinner;
    Button mSubmit;

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();

    String logType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        initDatePicker();
        dateButton = findViewById(R.id.viewLogDate);
        dateButton.setText(getTodaysDate());

        mSubmit = findViewById(R.id.viewLecturerTimetableSubmit);
        mSpinner = findViewById(R.id.viewLecturerTimetableSpinner);
        mSpinner.setOnItemSelectedListener(this);

        // Set up the drop down menu //
        row1.add("Login");
        row1.add("Payment");
        row1.add("Staff");
        row1.add("Lecturer");
        row1.add("Subject");
        row1.add("Class");
        row1.add("Result");
        row1.add("Program");
        row1.add("Session");
        row1.add("Student Details");
        row1.add("Student Assignment");
        row1.add("Student Attendance");

        row2.add("LOGIN");
        row2.add("PAYMENT");
        row2.add("STAFF");
        row2.add("LECTURER");
        row2.add("SUBJECT");
        row2.add("CLASS");
        row2.add("RESULT");
        row2.add("PROGRAM");
        row2.add("SESSION");
        row2.add("DETAILS");
        row2.add("ASSIGNMENT");
        row2.add("ATTENDANCE");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewLog.this, android.R.layout.simple_spinner_item, row1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(ViewLog.this, ViewLogFiltered.class);
                intent.putExtra("date", dateButton.getText().toString());
                intent.putExtra("type", logType);
                startActivity(intent);

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {

        getMenuInflater().inflate(R.menu.action_bar_function, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.home:

                startActivity (new Intent(getApplicationContext(), AdminPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                startActivity (new Intent(getApplicationContext(), ViewLog.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        logType = row2.get(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    private String getTodaysDate()
    {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return makeDateString(day, month, year);

    }

    private void initDatePicker()
    {

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {

                month = month + 1;
                String date =   makeDateString (day, month, year);
                dateButton.setText(date);

            }

        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        mDatePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }

    private String makeDateString (int day, int month, int year)
    {

        String finalDay = "";
        String finalMonth = "";

        if (day < 10)
        {
            finalDay = "0" + day;

        }

        else
        {

            finalDay = Integer.toString(day);

        }

        if (month < 10)
        {

            finalMonth = "0" + month;

        }

        else
        {

            finalMonth = Integer.toString(month);

        }

        return finalDay + "-" + finalMonth + "-" + year;

    }

    public void openDatePicker(View view)
    {

        mDatePickerDialog.show();

    }

}