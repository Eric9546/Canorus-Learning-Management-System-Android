package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class StudentContact extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();

    String lecId = "";

    ArrayList<String> lec = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        EditText mMessage = findViewById(R.id.studentContactMessage);
        Button mSubmit = findViewById(R.id.studentContactButton);
        Spinner mSpinner = findViewById(R.id.studentContactSpinner);
        mSpinner.setOnItemSelectedListener(this);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Lecturer/");

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("name").getValue().toString());
                    row2.add(item.child("lecId").getValue().toString());

                }

                // Set up the drop down menu
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentContact.this, android.R.layout.simple_spinner_item, row1);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(adapter);

                // Send message button //
                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        // Retrieve all required data //
                        String message = mMessage.getText().toString();
                        lecId = lecId.replaceAll("\\s", "");

                        // Retrieve lecturer email //
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference stage1 = database.getReference("Lecturer/" + lecId);

                        stage1.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {

                                // Retrieve students details //
                                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                                DatabaseReference stage2 = database2.getReference("Registration/" + spId);

                                stage2.addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2)
                                    {

                                        // Store student details //
                                        String studentId = snapshot2.child("id").getValue().toString();
                                        String studentName = snapshot2.child("name").getValue().toString();
                                        String studentEmail = snapshot2.child("email").getValue().toString();
                                        String studentProgram = snapshot2.child("program").getValue().toString();

                                        // Send email to the lecturer //
                                        String stringSenderEmail = "postmaster@sandbox9a189234a5e64ef0a823c2cf47daaeba.mailgun.org";
                                        String stringReceiverEmail = snapshot.child("email").getValue().toString();
                                        String stringPasswordSenderEmail = "87d79c294275468fec083d7839d383d4-1b237f8b-d24d52dc";

                                        String stringHost = "smtp.mailgun.org";

                                        Properties properties = System.getProperties();

                                        properties.put("mail.smtp.host", stringHost);
                                        properties.put("mail.smtp.port", "587");
                                        properties.put("mail.smtp.tls.enable", "true");
                                        properties.put("mail.smtp.auth", "true");
                                        properties.put("mail.smtp.from", studentEmail);

                                        javax.mail.Session session = Session.getInstance(properties, new Authenticator()
                                        {
                                            @Override
                                            protected PasswordAuthentication getPasswordAuthentication()
                                            {
                                                return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                                            }
                                        });

                                        MimeMessage mimeMessage = new MimeMessage(session);
                                        try
                                        {
                                            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

                                            mimeMessage.setSubject("Student Message");
                                            mimeMessage.setText("This Is To Inform You That The Following Student Sent A Message To You\n" +
                                                    "Student ID: " + studentId + "\n" +
                                                    "Student Name: " + studentName + "\n" +
                                                    "Student Program: " + studentProgram + "\n" +
                                                    "Message: " + message + "\n" +
                                                    "You May Reply To This Email To Contact The Student");

                                            Thread thread = new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {

                                                    try
                                                    {
                                                        Transport.send(mimeMessage);
                                                    }

                                                    catch (MessagingException e)
                                                    {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            });

                                            thread.start();

                                        }

                                        catch (MessagingException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(StudentContact.this, "Message Sent!", Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {

                                    }

                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }
                        });

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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        lecId = row2.get(position).toString();
        //Toast.makeText(this, "YOUR SELECTION IS : " + parent.getItemAtPosition(position).toString() + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

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

                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                startActivity (new Intent(getApplicationContext(), StudentContact.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}