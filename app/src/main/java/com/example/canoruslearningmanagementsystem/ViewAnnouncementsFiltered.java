package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViewAnnouncementsFiltered extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    TwoRowAdapter adapter;

    String subId = "";
    String record_to_remove = "";

    EditText mTitle, mDetails;
    Button mSubmit;

    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcements_filtered);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mTitle = findViewById(R.id.viewAnnouncementsFilteredTitle);
        mDetails = findViewById(R.id.viewAnnouncementsFilteredDetails);
        mSubmit = findViewById(R.id.viewAnnouncementsFilteredSubmit);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Announcement/" + subId);

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();
        ArrayList<String> row2 = new ArrayList<>();

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("announceTitle").getValue().toString());
                    row2.add(item.child("announceDetail").getValue().toString() + " | " + item.child("datePublished").getValue().toString());
                    row3.add(item.getKey());

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.viewAnnouncementFilteredRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewAnnouncementsFiltered.this));
                adapter = new TwoRowAdapter (ViewAnnouncementsFiltered.this, row1, row2);
                adapter.setClickListener(ViewAnnouncementsFiltered.this);
                recyclerView.setAdapter(adapter);

                // Add the new announcement //
                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        // Get inputs from user //
                        String title = mTitle.getText().toString();
                        String details = mDetails.getText().toString();

                        if (TextUtils.isEmpty(title))
                        {

                            mTitle.setError("Title Cannot Be Empty!");
                            return;

                        }

                        if (TextUtils.isEmpty(details))
                        {

                            mDetails.setError("Details Cannot Be Empty!");
                            return;

                        }

                        // Get time stamp //
                        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
                        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
                        SimpleDateFormat logTime2 = new SimpleDateFormat("hh:mma");
                        Date date = new Date ();

                        String indexTime = logDate.format(date) + "-" + logTime.format(date);
                        String datePublished = logDate.format(date) + ", " + logTime2.format(date);

                        // Update the details in the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Announcement/" + subId + "/" + indexTime);

                        stage2.child("announceTitle").setValue(title);
                        stage2.child("announceDetail").setValue(details);
                        stage2.child("datePublished").setValue(datePublished);

                        // Send push notification //
                        sentPush(title);

                        Toast.makeText(ViewAnnouncementsFiltered.this, "Announcement Added", Toast.LENGTH_SHORT).show();

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(ViewAnnouncementsFiltered.this, ViewAnnouncementsFiltered.class);
                        intent.putExtra("subId", subId);
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

    }

    @Override
    public void onItemClick(View view, int position)
    {

        record_to_remove = row3.get(position);

        // Remove the announcement //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Announcement/" + subId + "/" + record_to_remove);

        stage1.removeValue();

        Toast.makeText(ViewAnnouncementsFiltered.this, "Announcement Removed", Toast.LENGTH_SHORT).show();

        finish();
        overridePendingTransition(0, 0);
        Intent intent = new Intent(ViewAnnouncementsFiltered.this, ViewAnnouncementsFiltered.class);
        intent.putExtra("subId", subId);
        startActivity(intent);
        overridePendingTransition(0, 0);

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

                startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(ViewAnnouncementsFiltered.this, ViewAnnouncementsFiltered.class);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void sentPush (String title)
    {

        RequestQueue mRequestQue = Volley.newRequestQueue(this);

        // Create the json object to store the notification details //
        JSONObject json = new JSONObject();
        try
        {
            // Set the topic, title and body of the notification //
            json.put("to", "/topics/" + subId);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "Announcement From " + subId);
            notificationObj.put("body", title);

            json.put("notification", notificationObj);

            // Set the credentials to send the notification API //
            String URL = "https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    response -> Log.d("MUR", "onResponse: " + response.toString()),
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