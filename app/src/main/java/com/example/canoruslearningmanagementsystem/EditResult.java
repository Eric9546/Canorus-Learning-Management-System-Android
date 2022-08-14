package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditResult extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
{

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

    TwoRowAdapter adapter;

    EditText mSearch;
    Button mSubmit;

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mSearch = findViewById(R.id.editResultName);
        mSubmit = findViewById(R.id.editResultSubmit);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            program = extras.getString("program");
            search = extras.getString("search");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Registration/");

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("program").getValue().toString().equalsIgnoreCase(program))
                    {

                        if (item.child("name").getValue().toString().toUpperCase().contains(search))
                        {

                            row1.add(item.child("id").getValue().toString());
                            row2.add(item.child("name").getValue().toString());
                            row3.add(item.child("session").getValue().toString());

                        }

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editResultRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditResult.this));
                adapter = new TwoRowAdapter (EditResult.this, row1, row2);
                adapter.setClickListener(EditResult.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String search = mSearch.getText().toString().toUpperCase();

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditResult.this, EditResult.class);
                        intent.putExtra("program", program);
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
                Intent intent = new Intent(EditResult.this, EditResult.class);
                intent.putExtra("program", program);
                intent.putExtra("search", search);
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

        Intent intent = new Intent(EditResult.this, EditResultStudent.class);
        intent.putExtra("stuId", row1.get(position));
        intent.putExtra("program", program);
        intent.putExtra("session", row3.get(position));
        intent.putExtra("search", "");
        startActivity(intent);

    }

}