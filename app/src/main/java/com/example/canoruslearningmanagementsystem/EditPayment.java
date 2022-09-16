package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditPayment extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
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

    String search = "";
    String program = "";
    String session = "";
    String finalId = "";

    TwoRowAdapter adapter;

    EditText mSearch;
    Button mSubmit;

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();
    ArrayList<String> row4 = new ArrayList<>();

    StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_payment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        finalId = spId;

        mSearch = findViewById(R.id.editPaymentName);
        mSubmit = findViewById(R.id.editPaymentSubmit);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            program = extras.getString("program");
            session = extras.getString("session");
            search = extras.getString("search");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Payment/" + session);

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("program").getValue().toString().equalsIgnoreCase(program) && item.child("session").getValue().toString().equalsIgnoreCase(session))
                    {

                        if (item.child("name").getValue().toString().toUpperCase().contains(search))
                        {

                            row1.add(item.child("id").getValue().toString() + " - " + item.child("name").getValue().toString());
                            row2.add(item.child("payStatus").getValue().toString() + " - " + item.child("payMode").getValue().toString());
                            row3.add(item.child("id").getValue().toString());
                            row4.add(item.child("filename").getValue().toString());

                        }

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editPaymentRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditPayment.this));
                adapter = new TwoRowAdapter (EditPayment.this, row1, row2);
                adapter.setClickListener(EditPayment.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String search = mSearch.getText().toString().toUpperCase();

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditPayment.this, EditPayment.class);
                        intent.putExtra("program", program);
                        intent.putExtra("session", session);
                        intent.putExtra("search", search);
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
                Intent intent = new Intent(EditPayment.this, EditPayment.class);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("search", "");
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemClick(View view, int position)
    {

        String id = row3.get(position);
        String filename = row4.get(position);

        Button mView, mApprove, mRemove;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_payment, null);

        mView = popupView.findViewById(R.id.popup_edit_payment_view);
        mApprove = popupView.findViewById(R.id.popup_edit_payment_approve);
        mRemove = popupView.findViewById(R.id.popup_edit_payment_remove);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                popupWindow.dismiss();
                return true;
            }

        });

        mView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Download the payment file  //
                String fileLink = "https://firebasestorage.googleapis.com/v0/b/canorus-18990.appspot.com/o/" + filename + "?alt=media&";

                Uri urifile = Uri.parse(fileLink);

                DownloadManager downloadManager = (DownloadManager) EditPayment.this.getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Request request = new DownloadManager.Request(urifile);

                request.setTitle(filename);
                request.setDescription(filename);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                Long reference = downloadManager.enqueue(request);

            }

        });

        mApprove.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Updating the data in the payment table //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Payment/" + session + "/" + id);

                stage1.child("payStatus").setValue("Approved");

                // Updating the data in the enrolment table //
                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                DatabaseReference stage2 = database2.getReference("Enrolment/" + id);

                stage2.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        for (DataSnapshot item:snapshot.getChildren())
                        {

                            if (item.child("session").getValue().toString().equalsIgnoreCase(session))
                            {

                                FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                DatabaseReference stage3 = database3.getReference("Enrolment/" + id + "/" + item.getKey());

                                stage3.child("payStatus").setValue("Paid");

                            }

                        }

                        // Log the payment approval //
                        logApprovePayment(finalId, id, session, program);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditPayment.this, EditPayment.class);
                        intent.putExtra("program", program);
                        intent.putExtra("session", session);
                        intent.putExtra("search", "");
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });

            }

        });

        mRemove.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Query to delete the record from the database table //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Payment/" + session + "/" + id);

                stage1.removeValue();

                // Remove the file //
                mStorageReference = FirebaseStorage.getInstance().getReference(filename);
                mStorageReference.delete();

                // Updating the data in the enrolment table //
                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                DatabaseReference stage2 = database2.getReference("Enrolment/" + id);

                stage2.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        for (DataSnapshot item:snapshot.getChildren())
                        {

                            if (item.child("session").getValue().toString().equalsIgnoreCase(session))
                            {

                                FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                DatabaseReference stage3 = database3.getReference("Enrolment/" + id + "/" + item.getKey());

                                stage3.child("payStatus").setValue("Unpaid");

                            }

                        }

                        // Log the payment removal //
                        logRemovePayment(finalId, id, session, program);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditPayment.this, EditPayment.class);
                        intent.putExtra("program", program);
                        intent.putExtra("session", session);
                        intent.putExtra("search", "");
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });

            }

        });

    }

    public void logApprovePayment (String dbId, String stuId, String session, String program)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " approved the payment for student with ID: " + stuId.toUpperCase() +
                " enrolled under: " + program + " " + session +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
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

    public void logRemovePayment (String dbId, String stuId, String session, String program)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " removed the payment for student with ID: " + stuId.toUpperCase() +
                " enrolled under: " + program + " " + session +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
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