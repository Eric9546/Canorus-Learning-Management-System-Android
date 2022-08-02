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
import android.text.TextUtils;
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
import java.util.Locale;
import java.util.Map;

public class EditSubmission extends AppCompatActivity implements ThreeRowAdapter.ItemClickListener
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

    ThreeRowAdapter adapter;

    String subId = "";
    String record_to_view = "";
    String assignTitle = "";
    String filter = "";
    String search = "";
    String dbId = "";

    EditText mStuId;
    Button mSearch, mHide;

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
        setContentView(R.layout.activity_edit_submission);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        dbId = spId;

        mStuId = findViewById(R.id.editSubmissionId);
        mSearch = findViewById(R.id.editSubmissionSubmit);
        mHide = findViewById(R.id.editSubmissionFilter);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");
            record_to_view = extras.getString("record_to_view");
            assignTitle = extras.getString("assignTitle");
            filter = extras.getString("filter");
            search = extras.getString("search");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference(record_to_view);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("id").getValue().toString().contains(search))
                    {

                        if (item.child("grade").getValue().toString().contains(filter))
                        {

                            row1.add(item.child("id").getValue().toString());
                            row2.add("Grade: " + item.child("grade").getValue().toString());
                            row3.add("Comment: " + item.child("comment").getValue().toString());
                            row4.add(item.child("fileName").getValue().toString());

                        }

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editSubmissionRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditSubmission.this));
                adapter = new ThreeRowAdapter (EditSubmission.this, row1, row2, row3);
                adapter.setClickListener(EditSubmission.this);
                recyclerView.setAdapter(adapter);

                mSearch.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String search = mStuId.getText().toString().toUpperCase(Locale.ROOT);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditSubmission.this, EditSubmission.class);
                        intent.putExtra("subId", subId);
                        intent.putExtra("record_to_view", record_to_view);
                        intent.putExtra("assignTitle", assignTitle);
                        intent.putExtra("filter", "");
                        intent.putExtra("search", search);
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }

                });

                mHide.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditSubmission.this, EditSubmission.class);
                        intent.putExtra("subId", subId);
                        intent.putExtra("record_to_view", record_to_view);
                        intent.putExtra("assignTitle", assignTitle);
                        intent.putExtra("filter", "N/A");
                        intent.putExtra("search", "");
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

        String record_to_update = record_to_view + "/" + row1.get(position);

        EditText grade, comment;
        Button updateGrade, updateComment, mView, mDelete;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_submission, null);

        grade = popupView.findViewById(R.id.popup_edit_submission_grade);
        comment = popupView.findViewById(R.id.popup_edit_submission_comment);
        updateGrade = popupView.findViewById(R.id.popup_edit_submission_grade_submit);
        updateComment = popupView.findViewById(R.id.popup_edit_submission_comment_submit);
        mView = popupView.findViewById(R.id.popup_edit_submission_view);
        mDelete = popupView.findViewById(R.id.popup_edit_submission_delete);

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

        updateGrade.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                String finalGrade = grade.getText().toString();

                if (TextUtils.isEmpty(finalGrade))
                {

                    grade.setError("Grade Cannot Be Empty!");
                    return;

                }

                // Update the database //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_update);

                stage1.child("grade").setValue(finalGrade);

                // Log the grade update //
                logUpdateGrade(dbId, row1.get(position), finalGrade, subId, assignTitle);

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditSubmission.this, EditSubmission.class);
                intent.putExtra("subId", subId);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("assignTitle", assignTitle);
                intent.putExtra("filter", filter);
                intent.putExtra("search", search);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

        updateComment.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String finalComment = comment.getText().toString();

                if (TextUtils.isEmpty(finalComment))
                {

                    comment.setError("Comment Cannot Be Empty!");
                    return;

                }

                // Update the database //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_update);

                stage1.child("comment").setValue(finalComment);

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditSubmission.this, EditSubmission.class);
                intent.putExtra("subId", subId);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("assignTitle", assignTitle);
                intent.putExtra("filter", filter);
                intent.putExtra("search", search);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

        mView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Download the notes //
                String fileLink = "https://firebasestorage.googleapis.com/v0/b/canorus-18990.appspot.com/o/" + row4.get(position) + "?alt=media&";

                Uri urifile = Uri.parse(fileLink);

                DownloadManager downloadManager = (DownloadManager) EditSubmission.this.getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Request request = new DownloadManager.Request(urifile);

                request.setTitle(row4.get(position));
                request.setDescription(row4.get(position));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                Long reference = downloadManager.enqueue(request);

            }

        });

        mDelete.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Remove the file //
                mStorageReference = FirebaseStorage.getInstance().getReference(row4.get(position));
                mStorageReference.delete();

                // Query to delete the record from the database table //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_update);

                stage1.removeValue();

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditSubmission.this, EditSubmission.class);
                intent.putExtra("subId", subId);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("assignTitle", assignTitle);
                intent.putExtra("filter", filter);
                intent.putExtra("search", search);
                startActivity(intent);
                overridePendingTransition(0, 0);

                // Log the removed submission //
                logRemoveSubmission(dbId, row1.get(position), subId, assignTitle);

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

                startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditSubmission.this, EditSubmission.class);
                intent.putExtra("subId", subId);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("assignTitle", assignTitle);
                intent.putExtra("filter", "");
                intent.putExtra("search", "");
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logUpdateGrade (String dbId, String stuId, String grade, String subId, String assignTitle)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Lecturer with the ID: " + dbId.toUpperCase() + " updated the grade to " + grade + " for the student: " + stuId +
                " in " + subId +  " " + assignTitle +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-ASSIGNMENT")
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

    public void logRemoveSubmission (String dbId, String stuId, String subId, String assignTitle)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Lecturer with the ID: " + dbId.toUpperCase() + " removed the submission for the student: " + stuId + " in " + subId + " " + assignTitle +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-ASSIGNMENT")
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