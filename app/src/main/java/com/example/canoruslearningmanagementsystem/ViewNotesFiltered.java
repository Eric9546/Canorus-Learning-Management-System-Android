package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ViewNotesFiltered extends AppCompatActivity implements OneRowAdapter.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    OneRowAdapter adapter;

    String subId = "";
    String contentName = "";
    String contentAdded = "false";

    EditText mContentName;
    Button mSubmit;

    StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes_filtered);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mContentName = findViewById(R.id.viewNotesFilteredContentName);
        mSubmit = findViewById(R.id.viewNotesFilteredSubmit);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Note/" + subId);

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("contentName").getValue().toString());

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.viewNotesFilteredRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewNotesFiltered.this));
                adapter = new OneRowAdapter(ViewNotesFiltered.this, row1);
                adapter.setClickListener(ViewNotesFiltered.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        // Get input from user //
                        String contentName = mContentName.getText().toString();

                        if(TextUtils.isEmpty(contentName))
                        {

                            mContentName.setError("Content Name Cannot Be Empty!");
                            return;

                        }

                        // Check if content name already exists //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Note/" + subId + "/" + contentName);

                        stage2.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {

                                if (snapshot.getValue() == null)
                                {

                                    // Inserting the data into the database table //
                                    FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                    DatabaseReference stage3 = database3.getReference("Note/" + subId + "/" + contentName);

                                    stage3.child("contentName").setValue(contentName);

                                    finish();
                                    overridePendingTransition(0, 0);
                                    Intent intent = new Intent(ViewNotesFiltered.this, ViewNotesFiltered.class);
                                    intent.putExtra("subId", subId);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);

                                    contentAdded = "true";

                                }

                                else
                                {

                                    if (contentAdded.equalsIgnoreCase("true"))
                                    {



                                    }

                                    else
                                    {

                                        Toast.makeText(ViewNotesFiltered.this, "Content Name Already Exists!", Toast.LENGTH_SHORT).show();

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
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });


    }

    @Override
    public void onItemClick(View view, int position)
    {
        contentName = adapter.getItem(position);

        Button mEdit, mDelete;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_view_notes_filtered, null);

        mEdit = popupView.findViewById(R.id.popup_edit_submission_view);
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

        mEdit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(ViewNotesFiltered.this, EditNotes.class);
                intent.putExtra("contentName", contentName);
                intent.putExtra("subId", subId);
                startActivity(intent);

            }

        });

        mDelete.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Remove the content //
                String record_to_remove = "Note/" + subId + "/" + contentName;

                // Remove the files //
                FirebaseDatabase database5 = FirebaseDatabase.getInstance();
                DatabaseReference stage5 = database5.getReference(record_to_remove);

                stage5.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        for (DataSnapshot item:snapshot.getChildren())
                        {

                            if (!item.getKey().equalsIgnoreCase("contentName"))
                            {

                                mStorageReference = FirebaseStorage.getInstance().getReference(item.child("fileName").getValue().toString());
                                mStorageReference.delete();

                            }

                        }

                        // Query to delete the record from the database table //
                        FirebaseDatabase database6 = FirebaseDatabase.getInstance();
                        DatabaseReference stage6 = database6.getReference(record_to_remove);

                        stage6.removeValue();

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(ViewNotesFiltered.this, ViewNotesFiltered.class);
                        intent.putExtra("subId", subId);
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
                Intent intent = new Intent(ViewNotesFiltered.this, ViewNotesFiltered.class);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}