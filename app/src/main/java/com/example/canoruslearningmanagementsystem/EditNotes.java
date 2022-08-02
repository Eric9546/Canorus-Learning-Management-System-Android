package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditNotes extends AppCompatActivity implements TwoRowAdapter.ItemClickListener, AdapterView.OnItemSelectedListener
{

    private static final int FILE_CODE = 69;

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();
    ArrayList<String> ext1 = new ArrayList<>();
    ArrayList<String> ext2 = new ArrayList<>();

    TwoRowAdapter adapter;

    String contentName = "";
    String subId = "";
    String newfilename = "";
    String extension = "";
    String mime = "";
    String fileTitle = "";
    String fileDesc = "";
    String exit = "false";

    EditText mFileTitle, mFileDesc;
    Button mSubmit;
    Spinner mSpinner;

    StorageReference mStorageReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CODE)
        {

            UploadTask uploadTask = mStorageReference.putFile(data.getData());

            // Create file retrieval task //
            Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {

                    if (!task.isSuccessful())
                    {

                        Toast.makeText(EditNotes.this, "Submission Failed", Toast.LENGTH_SHORT).show();

                    }

                    return mStorageReference.getDownloadUrl();

                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {

                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {

                    if (task.isSuccessful())
                    {

                        // Inserting the data into the database table //
                        String record_to_submit = "Note/" + subId + "/" + contentName + "/" + fileTitle;

                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference(record_to_submit);

                        stage3.child("fileTitle").setValue(fileTitle);
                        stage3.child("fileDesc").setValue(fileDesc);
                        stage3.child("fileName").setValue(newfilename);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditNotes.this, EditNotes.class);
                        intent.putExtra("contentName", contentName);
                        intent.putExtra("subId", subId);
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }

                }

            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mFileTitle = findViewById(R.id.editNotesTitle);
        mFileDesc = findViewById(R.id.editNotesDesc);
        mSubmit = findViewById(R.id.editNotesSubmit);

        mSpinner = findViewById(R.id.editNotesSpinner);
        mSpinner.setOnItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            contentName = extras.getString("contentName");
            subId = extras.getString("subId");

        }

        // Set up the drop down menu //
        ext1.add("pdf");
        ext1.add("vnd.openxmlformats-officedocument.wordprocessingml.document");
        ext1.add("vnd.ms-powerpoint");
        ext2.add(".pdf");
        ext2.add(".docx");
        ext2.add(".pptx");
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(EditNotes.this, android.R.layout.simple_spinner_item, ext2);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapterSpinner);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Note/" + subId + "/" + contentName);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (!item.getValue().toString().equalsIgnoreCase(contentName))
                    {

                        row1.add(item.child("fileTitle").getValue().toString());
                        row2.add(item.child("fileDesc").getValue().toString());
                        row3.add(item.child("fileName").getValue().toString());

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editNotesRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditNotes.this));
                adapter = new TwoRowAdapter(EditNotes.this, row1, row2);
                adapter.setClickListener(EditNotes.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        fileTitle = mFileTitle.getText().toString();
                        fileDesc = mFileDesc.getText().toString();

                        if (TextUtils.isEmpty(fileTitle))
                        {

                            mFileTitle.setError("File Title Cannot Be Empty!");
                            return;

                        }

                        if (TextUtils.isEmpty(fileDesc))
                        {

                            mFileDesc.setError("File Description Cannot Be Empty!");
                            return;

                        }

                        // Query to check if content name already exists //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Note/" + subId + "/" + contentName + "/" + fileTitle);

                        stage2.addValueEventListener(new ValueEventListener()
                        {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2)
                            {

                                if (snapshot2.getValue() == null)
                                {

                                    // Get the timestamp //
                                    SimpleDateFormat logDate = new SimpleDateFormat("ddMMyyyy");
                                    SimpleDateFormat logTime = new SimpleDateFormat("HHmmss");
                                    Date date = new Date ();

                                    // Create link to firebase storage for submission file //
                                    newfilename = "NOTES_" + logDate.format(date) + logTime.format(date) + extension;
                                    mStorageReference = FirebaseStorage.getInstance().getReference(newfilename);

                                    // Upload the file to storage //
                                    Intent intent = new Intent();
                                    intent.setType("application/" + mime);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_CODE);

                                    exit = "true";

                                }

                                else
                                {

                                    if (exit.equalsIgnoreCase("true"))
                                    {



                                    }

                                    else
                                    {

                                        Toast.makeText(EditNotes.this, "File Title Already Exists!", Toast.LENGTH_SHORT).show();

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {


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

        Button mView, mDelete;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_notes, null);

        mView = popupView.findViewById(R.id.popup_edit_notes_view);
        mDelete = popupView.findViewById(R.id.popup_edit_notes_delete);

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

                // Download the notes //
                String fileLink = "https://firebasestorage.googleapis.com/v0/b/canorus-18990.appspot.com/o/" + row3.get(position) + "?alt=media&";

                Uri urifile = Uri.parse(fileLink);

                DownloadManager downloadManager = (DownloadManager) EditNotes.this.getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Request request = new DownloadManager.Request(urifile);

                request.setTitle(row3.get(position));
                request.setDescription(row3.get(position));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                Long reference = downloadManager.enqueue(request);

            }

        });

        mDelete.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Remove the notes //
                String record_to_remove = "Note/" + subId + "/" + contentName + "/" + row1.get(position);

                // Remove the file //
                mStorageReference = FirebaseStorage.getInstance().getReference(row3.get(position));
                mStorageReference.delete();

                // Query to delete the record from the database table //
                FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                DatabaseReference stage4 = database4.getReference(record_to_remove);

                stage4.removeValue();

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditNotes.this, EditNotes.class);
                intent.putExtra("contentName", contentName);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

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
                Intent intent = new Intent(EditNotes.this, EditNotes.class);
                intent.putExtra("contentName", contentName);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        mime = ext1.get(position).toString();
        extension = ext2.get(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

}