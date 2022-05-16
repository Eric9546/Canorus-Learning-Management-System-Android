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
import java.util.Map;

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
                Toast.makeText(StudentPanel.this, "ENROL", Toast.LENGTH_SHORT).show();
            }
        });

        mResults.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "RESULTS", Toast.LENGTH_SHORT).show();
            }
        });

        mTimetable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "TIMETABLE", Toast.LENGTH_SHORT).show();
            }
        });

        mPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "PAYMENT", Toast.LENGTH_SHORT).show();
            }
        });

        mContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "CONTACT", Toast.LENGTH_SHORT).show();
            }
        });

        mChange.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "CHANGE", Toast.LENGTH_SHORT).show();

            }
        });

        mNotes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "NOTES", Toast.LENGTH_SHORT).show();

            }
        });

        mAssignment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "ASSIGNMENT", Toast.LENGTH_SHORT).show();

            }
        });

        mAnnouncements.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "ANNOUNCEMENTS", Toast.LENGTH_SHORT).show();
                sentPush();

            }
        });

        mAttendance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(StudentPanel.this, "ATTENDANCE", Toast.LENGTH_SHORT).show();

                // Redirect user to respective panels //
                startActivity (new Intent(getApplicationContext(), StudentAttendanceQR.class));

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {

        getMenuInflater().inflate(R.menu.action_bar, menu);
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

                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                finish();

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

    public void sentPush ()
    {

        RequestQueue mRequestQue = Volley.newRequestQueue(this);

        // Create the json object to store the notification details //
        JSONObject json = new JSONObject();
        try
        {
            // Set the topic, title and body of the notification //
            json.put("to", "/topics/" + "default");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "New Notification!");
            notificationObj.put("body", "Sent From Android ");

            json.put("notification", notificationObj);

            // Set the credentials to send the notification API //
            String URL = "https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    response -> Log.d("MUR", "onResponse: "),
                    error -> Log.d("MUR", "onError: " + error.networkResponse)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AAAAnfSwSms:APA91bHzdrOgExVlwZM0Igsa-1wWKyzpL3b6QT1HDUMqpE2pPZRUfwbUwQz3EToo5PZYx_T3qrGZVWRJhJW3e8uIjTyatYtJhJ6753BwXJ-iBQLaT4NpqCG5Y8RLP5n4O_BnvtANTJas");
                    return header;
                }
            };

            mRequestQue.add(request);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

}