package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeUtils;
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

public class StudentOnlinePayment extends AppCompatActivity
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    FirebaseFirestore log = FirebaseFirestore.getInstance();

    Button mSubmit;
    Spinner mType, mMonth, mYear;

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_online_payment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        String spProgram = mPreferences.getString(PROGRAM_KEY, "");

        mSubmit = findViewById(R.id.studentOnlinePaymentSubmit);
        mType = findViewById(R.id.studentOnlinePaymentType);
        mMonth = findViewById(R.id.studentOnlinePaymentMonth);
        mYear = findViewById(R.id.studentOnlinePaymentYear);

        // Set up spinners //
        row1.add("Visa");
        row1.add("Mastercard");
        row1.add("American Express");
        row1.add("Discover");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentOnlinePayment.this, android.R.layout.simple_spinner_item, row1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapter);

        row2.add("01");
        row2.add("02");
        row2.add("03");
        row2.add("04");
        row2.add("05");
        row2.add("06");
        row2.add("07");
        row2.add("08");
        row2.add("09");
        row2.add("10");
        row2.add("11");
        row2.add("12");

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(StudentOnlinePayment.this, android.R.layout.simple_spinner_item, row2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonth.setAdapter(adapter2);

        row3.add("2022");
        row3.add("2023");
        row3.add("2024");
        row3.add("2025");
        row3.add("2026");
        row3.add("2027");
        row3.add("2028");
        row3.add("2029");
        row3.add("2030");

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(StudentOnlinePayment.this, android.R.layout.simple_spinner_item, row3);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYear.setAdapter(adapter3);

        mSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Gather student data //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Registration/" + spId);

                stage1.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        String name = snapshot.child("name").getValue().toString();
                        String program = snapshot.child("program").getValue().toString();
                        String payStatus = "Approved";

                        // Update the details in the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Payment/" + spSession + "/" + spId);

                        stage2.child("id").setValue(spId);
                        stage2.child("name").setValue(name);
                        stage2.child("program").setValue(program);
                        stage2.child("session").setValue(spSession);
                        stage2.child("filename").setValue("PAYMENT_PLACEHOLDER.pdf");
                        stage2.child("payStatus").setValue(payStatus);
                        stage2.child("payMode").setValue("Online");

                        // Updating the data in the enrolment table //
                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference("Enrolment/" + spId);

                        stage3.addValueEventListener(new ValueEventListener()
                        {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {

                                for (DataSnapshot item:snapshot.getChildren())
                                {

                                    if (item.child("session").getValue().toString().equalsIgnoreCase(spSession))
                                    {

                                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                        DatabaseReference stage3 = database3.getReference("Enrolment/" + spId + "/" + item.getKey());

                                        stage3.child("payStatus").setValue("Paid");

                                    }

                                }

                                // Log the payment details //
                                logPayment (spId, program, spSession, "PAYMENT_PLACEHOLDER.pdf");

                                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                                finish();

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

    public void logPayment (String dbId, String program, String session, String newfilename)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Student with the ID: " + dbId.toUpperCase() + " enrolled under " + program + " " + session +
                " submitted their payment file titled " + newfilename + " at " +
                logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-PAYMENT")
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