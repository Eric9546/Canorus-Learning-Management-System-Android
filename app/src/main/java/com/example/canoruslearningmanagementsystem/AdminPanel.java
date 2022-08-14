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

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        String spAccessLevel = mPreferences.getString(ACCESS_LEVEL_KEY, "");

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

                if (spAccessLevel.equalsIgnoreCase("Admin"))
                {

                    PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                    popup.setOnMenuItemClickListener(AdminPanel.this);
                    popup.inflate(R.menu.staff_popup);
                    popup.show();

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }


            }

        });

        mLecturer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin") || spAccessLevel.equalsIgnoreCase("Program Officer"))
                {

                    PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                    popup.setOnMenuItemClickListener(AdminPanel.this);
                    popup.inflate(R.menu.lecturer_popup);
                    popup.show();

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mSubject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin") || spAccessLevel.equalsIgnoreCase("Program Officer"))
                {

                    PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                    popup.setOnMenuItemClickListener(AdminPanel.this);
                    popup.inflate(R.menu.subject_popup);
                    popup.show();

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mResult.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin") || spAccessLevel.equalsIgnoreCase("Exam Unit"))
                {

                    startActivity (new Intent(getApplicationContext(), ViewResult.class));

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin") || spAccessLevel.equalsIgnoreCase("Finance"))
                {

                    startActivity (new Intent(getApplicationContext(), ViewPayment.class));

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mEnrolment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin") || spAccessLevel.equalsIgnoreCase("Program Officer"))
                {

                    PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                    popup.setOnMenuItemClickListener(AdminPanel.this);
                    popup.inflate(R.menu.enrolment_popup);
                    popup.show();

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mStudent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin") || spAccessLevel.equalsIgnoreCase("Registry"))
                {

                    PopupMenu popup = new PopupMenu(AdminPanel.this, view);
                    popup.setOnMenuItemClickListener(AdminPanel.this);
                    popup.inflate(R.menu.student_popup);
                    popup.show();

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mLog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (spAccessLevel.equalsIgnoreCase("Admin"))
                {

                    startActivity (new Intent(getApplicationContext(), ViewLog.class));

                }

                else
                {

                    Toast.makeText(AdminPanel.this, "You Do Not Have Access!", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    public boolean onMenuItemClick (MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.add_staff:
                startActivity (new Intent(getApplicationContext(), AddStaff.class));
                return true;

            case R.id.view_staff:
                startActivity (new Intent(getApplicationContext(), ViewStaff.class));
                return true;

            case R.id.add_lecturer:
                startActivity (new Intent(getApplicationContext(), AddLecturer.class));
                return true;

            case R.id.view_lecturer:
                Intent intent = new Intent(AdminPanel.this, ViewLecturer.class);
                intent.putExtra("search", "");
                startActivity(intent);
                return true;

            case R.id.view_lecturer_timetable:
                startActivity (new Intent(getApplicationContext(), ViewLecturerTimetable.class));
                return true;

            case R.id.add_subject:
                startActivity (new Intent(getApplicationContext(), AddSubject.class));
                return true;

            case R.id.view_subject:
                startActivity (new Intent(getApplicationContext(), ViewSubject.class));
                return true;

            case R.id.view_classes:
                startActivity (new Intent(getApplicationContext(), ViewClasses.class));
                return true;

            case R.id.view_enrolment:
                startActivity (new Intent(getApplicationContext(), ViewEnrolment.class));
                return true;

            case R.id.add_program_session:
                startActivity (new Intent(getApplicationContext(), AddProgramSession.class));
                return true;

            case R.id.remove_program_session:
                startActivity (new Intent(getApplicationContext(), RemoveProgramSession.class));
                return true;

            case R.id.add_student:
                startActivity (new Intent(getApplicationContext(), AddStudent.class));
                return true;

            case R.id.view_student:
                startActivity (new Intent(getApplicationContext(), ViewStudent.class));
                return true;

            default:
                return false;

        }

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