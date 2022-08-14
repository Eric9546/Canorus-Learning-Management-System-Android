package com.example.canoruslearningmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class ViewLogFiltered extends AppCompatActivity implements OneRowAdapterXS.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    String date = "";
    String type = "";
    String title = "";

    OneRowAdapterXS adapter;

    TextView mTitle;

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log_filtered);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            date = extras.getString("date");
            type = extras.getString("type");

        }

        title = date + "-" + type;

        mTitle = findViewById(R.id.viewLogFilteredText1);

        mTitle.setText(title);

        // Retrieve the data //
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        fStore.collection(title).addSnapshotListener(new EventListener<QuerySnapshot>()
        {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {

                for (DocumentSnapshot snapshot : queryDocumentSnapshots)
                {

                    row1.add(snapshot.getString("logMsg"));

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.viewLogFilteredRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewLogFiltered.this));
                adapter = new OneRowAdapterXS(ViewLogFiltered.this, row1);
                adapter.setClickListener(ViewLogFiltered.this);
                recyclerView.setAdapter(adapter);

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
                Intent intent = new Intent(ViewLogFiltered.this, ViewLogFiltered.class);
                intent.putExtra("date", date);
                intent.putExtra("type", type);
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



    }

}