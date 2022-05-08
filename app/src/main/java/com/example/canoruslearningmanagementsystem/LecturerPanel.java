package com.example.canoruslearningmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class LecturerPanel extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    ImageView mTimetable, mNotes, mAssignment, mAnnouncement, mAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_panel);

        mTimetable = findViewById(R.id.lecTimetableImg);
        mNotes = findViewById(R.id.lecNotesImg);
        mAssignment = findViewById(R.id.lecAssignmentImg);
        mAnnouncement = findViewById(R.id.lecAnnouncementImg);
        mAttendance = findViewById(R.id.lecAttendanceImg);

        // Set onclick listeners for all lecturer panel functions //
        mTimetable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(LecturerPanel.this, "TIMETABLE", Toast.LENGTH_SHORT).show();
            }
        });

        mNotes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(LecturerPanel.this, "NOTES", Toast.LENGTH_SHORT).show();
            }
        });

        mAssignment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                PopupMenu popup = new PopupMenu(LecturerPanel.this, view);
                popup.setOnMenuItemClickListener(LecturerPanel.this);
                popup.inflate(R.menu.assignment_popup);
                popup.show();
            }
        });

        mAnnouncement.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(LecturerPanel.this, "ANNOUCEMENT", Toast.LENGTH_SHORT).show();
            }
        });

        mAttendance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(LecturerPanel.this, "ATTENDANCE", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onMenuItemClick (MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.edit_assignment:
                Toast.makeText(LecturerPanel.this, "edit_assignment", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_assignment:
                Toast.makeText(LecturerPanel.this, "view_assignment", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return false;

        }

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

                startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
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

}