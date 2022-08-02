package com.example.canoruslearningmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StudentPanel extends AppCompatActivity
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    ImageView mEnrol, mResults, mTimetable, mPayment, mContact, mChange, mNotes, mAssignment, mAnnouncements, mAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_panel);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        mEnrol = findViewById(R.id.enrolmentImg);
        mResults = findViewById(R.id.resultsImg);
        mTimetable = findViewById(R.id.timetableImg);
        mPayment = findViewById(R.id.paymentImg);
        mContact = findViewById(R.id.contactImg);
        mChange = findViewById(R.id.detailsImg);
        mNotes = findViewById(R.id.notesImg);
        mAssignment = findViewById(R.id.assignmentImg);
        mAnnouncements = findViewById(R.id.announcementImg);
        mAttendance = findViewById(R.id.attendanceImg);

        // Set onclick listeners for all student panel functions //
        mEnrol.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentEnrolment.class));
            }
        });

        mResults.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentResultChoose.class));
            }
        });

        mTimetable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentTimetable.class));
            }
        });

        mPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentPayment.class));
            }
        });

        mContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentContact.class));
            }
        });

        mChange.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), ChangeDetails.class));
            }
        });

        mNotes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentNotesChoose.class));
            }
        });

        mAssignment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentAssignmentChoose.class));
            }
        });

        mAnnouncements.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentAnnouncementsChoose.class));
            }
        });

        mAttendance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity (new Intent(getApplicationContext(), StudentAttendanceChoose.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {

        getMenuInflater().inflate(R.menu.action_bar_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {

        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        switch (item.getItemId())
        {

            case R.id.home:

                finish();
                overridePendingTransition(0, 0);
                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                overridePendingTransition(0, 0);

                return true;

            case R.id.logout:

                // Update the session file //
                spEditor.putString(LOGIN_STATUS_KEY, "FALSE");
                spEditor.apply();

                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}