package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AddStudent extends AppCompatActivity
{

    FirebaseFirestore log = FirebaseFirestore.getInstance();

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

    String exit = "false";

    EditText mName, mEmail, mPhone, mIc, mAddress;
    Spinner mProgram, mSession;
    Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mName = findViewById(R.id.addStudentName);
        mEmail = findViewById(R.id.addStudentEmail);
        mPhone = findViewById(R.id.addStudentPhone);
        mIc = findViewById(R.id.addStudentIc);
        mAddress = findViewById(R.id.addStudentAddress);
        mProgram = findViewById(R.id.addStudentProgram);
        mSession = findViewById(R.id.addStudentSession);
        mSubmit = findViewById(R.id.addStudentSubmit);

        // Set up the drop down menu //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Program/");

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("progCode").getValue().toString());

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddStudent.this, android.R.layout.simple_spinner_item, row1);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mProgram.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        // Set up the drop down menu //
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference stage2 = database2.getReference("Session/");

        stage2.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row2.add(item.child("session").getValue().toString());

                }

                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(AddStudent.this, android.R.layout.simple_spinner_item, row2);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSession.setAdapter(adapter2);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        mSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String phone = mPhone.getText().toString();
                String ic = mIc.getText().toString();
                String address = mAddress.getText().toString();
                String program = mProgram.getSelectedItem().toString();
                String session = mSession.getSelectedItem().toString();

                if (TextUtils.isEmpty(name))
                {

                    mName.setError("Name Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(email))
                {

                    mEmail.setError("Email Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(phone))
                {

                    mPhone.setError("Phone Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(ic))
                {

                    mIc.setError("IC/Passport Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(address))
                {

                    mAddress.setError("Address Cannot Be Empty!");
                    return;

                }

                // Generate ID and password //
                SimpleDateFormat logDate = new SimpleDateFormat("yyMMdd");
                SimpleDateFormat logTime = new SimpleDateFormat("mmss");
                Date date = new Date ();

                String stuId = "S" + logDate.format(date) + logTime.format(date);
                String password = "S" + ic;

                // Query to check if user ID already exists //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage = database.getReference("Registration/" + stuId.toUpperCase());

                stage.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            stage.child("id").setValue(stuId);
                            stage.child("password").setValue(password);
                            stage.child("access_level").setValue("Student");
                            stage.child("name").setValue(name);
                            stage.child("email").setValue(email);
                            stage.child("telno").setValue(phone);
                            stage.child("ic").setValue(ic);
                            stage.child("address").setValue(address);
                            stage.child("program").setValue(program);
                            stage.child("session").setValue(session);

                            // Send email to the student //
                            String stringSenderEmail = "postmaster@sandbox9a189234a5e64ef0a823c2cf47daaeba.mailgun.org";
                            String stringReceiverEmail = email;
                            String stringPasswordSenderEmail = "87d79c294275468fec083d7839d383d4-1b237f8b-d24d52dc";

                            String stringHost = "smtp.mailgun.org";

                            Properties properties = System.getProperties();

                            properties.put("mail.smtp.host", stringHost);
                            properties.put("mail.smtp.port", "587");
                            properties.put("mail.smtp.tls.enable", "true");
                            properties.put("mail.smtp.auth", "true");
                            properties.put("mail.smtp.from", "donotreply@canorus.epizy.com");

                            javax.mail.Session session1 = Session.getInstance(properties, new Authenticator()
                            {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication()
                                {
                                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                                }
                            });

                            MimeMessage mimeMessage = new MimeMessage(session1);
                            try
                            {
                                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

                                mimeMessage.setSubject("Student Registration Successful");
                                mimeMessage.setText("This Is To Inform You That The You Have Been Successfully Registered On The Canorus Learning Management System.\n" +
                                        "Student Login ID: " + stuId + "\n" +
                                        "Student Login Password: " + password + "\n" +
                                        "Student Name: " + name + "\n" +
                                        "Student Program: " + program + "\n" +
                                        "Student Session: " + session + "\n" +
                                        "You May Access Your Student Account on https://canorus.epizy.com/login.php or the Canorus Android Mobile Application");

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

                            // Log the details //
                            logAddStudent(spId, stuId, program, session);

                            exit = "true";

                            SystemClock.sleep(3000);

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity (new Intent(getApplicationContext(), AddStudent.class));
                            overridePendingTransition(0, 0);

                            Toast.makeText(AddStudent.this, "Student Added!", Toast.LENGTH_SHORT).show();


                        }

                        else
                        {

                            if (exit.equalsIgnoreCase("true"))
                            {



                            }

                            else
                            {

                                Toast.makeText(AddStudent.this, "Student ID Already Exists!", Toast.LENGTH_SHORT).show();

                            }

                        }

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
                startActivity (new Intent(getApplicationContext(), AddStudent.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logAddStudent (String dbId, String stuId, String program, String session)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " added student with ID: " + stuId.toUpperCase() + " enrolled under " + program + " " + session +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-DETAILS")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                    }
                });

    }

}