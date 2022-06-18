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

public class AdminPanel extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    ImageView mUser, mLecturer, mSubject, mResult, mPayment, mEnrolment, mStudent, mLog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        mUser = findViewById(R.id.userImg);
        mLecturer = findViewById(R.id.lecturerImg);
        mSubject = findViewById(R.id.subjectImg);
        mResult = findViewById(R.id.resultImg);
        mPayment = findViewById(R.id.paymentImg2);
        mEnrolment = findViewById(R.id.enrolmentImg2);
        mLog = findViewById(R.id.logImg);
        mStudent = findViewById(R.id.stuImg);

        // Set onclick listeners for all admin panel functions //
        mUser.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                popup.setOnMenuItemClickListener(AdminPanel.this);
                popup.inflate(R.menu.staff_popup);
                popup.show();

            }

        });

        mLecturer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                popup.setOnMenuItemClickListener(AdminPanel.this);
                popup.inflate(R.menu.lecturer_popup);
                popup.show();

            }
        });

        mSubject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                popup.setOnMenuItemClickListener(AdminPanel.this);
                popup.inflate(R.menu.subject_popup);
                popup.show();

            }
        });

        mResult.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Toast.makeText(AdminPanel.this, "RESULT", Toast.LENGTH_SHORT).show();

            }
        });

        mPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Toast.makeText(AdminPanel.this, "PAYMENT", Toast.LENGTH_SHORT).show();

            }
        });

        mEnrolment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                popup.setOnMenuItemClickListener(AdminPanel.this);
                popup.inflate(R.menu.enrolment_popup);
                popup.show();

            }
        });

        mStudent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                popup.setOnMenuItemClickListener(AdminPanel.this);
                popup.inflate(R.menu.student_popup);
                popup.show();

            }
        });

        mLog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Toast.makeText(AdminPanel.this, "LOG", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public boolean onMenuItemClick (MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.add_staff:
                Toast.makeText(AdminPanel.this, "add_user", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_staff:
                Toast.makeText(AdminPanel.this, "view_user", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.add_lecturer:
                Toast.makeText(AdminPanel.this, "add_lecturer", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_lecturer:
                Toast.makeText(AdminPanel.this, "view_lecturer", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_lecturer_timetable:
                Toast.makeText(AdminPanel.this, "view_lecturer_timetable", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.add_subject:
                Toast.makeText(AdminPanel.this, "add_subject", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_subject:
                Toast.makeText(AdminPanel.this, "view_subject", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_classes:
                Toast.makeText(AdminPanel.this, "view_classes", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_enrolment:
                Toast.makeText(AdminPanel.this, "view_enrolment", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.add_program_session:
                Toast.makeText(AdminPanel.this, "add_program_session", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.remove_program_session:
                Toast.makeText(AdminPanel.this, "remove_program_session", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.add_student:
                Toast.makeText(AdminPanel.this, "add_student", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.view_student:
                Toast.makeText(AdminPanel.this, "view_student", Toast.LENGTH_SHORT).show();
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

                startActivity (new Intent(getApplicationContext(), AdminPanel.class));
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