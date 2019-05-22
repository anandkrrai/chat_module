package com.workafy.workafyapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workafy.workafyapp.Utils.Utils;
import com.workafy.workafyapp.Utils.stringManipulation;
import com.workafy.workafyapp.discussion_chat.discussion_issue;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseUser user;
    public static final int RC_SIGN_IN = 1;
    LinearLayout ll;
    ListView listview;
    DatabaseReference databaseReference;
    String[]  issues_list;
    Button start_issue;
    SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.getDatabase();

        ll= findViewById(R.id.llMain);
        AnimationDrawable animationDrawable = (AnimationDrawable)ll.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        mFirebaseAuth = FirebaseAuth.getInstance();


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("discussion");
        databaseReference.keepSynced(true);
        listview= findViewById(R.id.disscussion_board_listview);
        start_issue=findViewById(R.id.discussion_board_start_new_issue_button);
        pullToRefresh =findViewById(R.id.pullToRefresh);


        start_issue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_dialog();
            }
        });


        populate_list();

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populate_list();

                pullToRefresh.setRefreshing(false);
            }
        });


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), discussion_issue.class);
                intent.putExtra("category", stringManipulation.compress(issues_list[position]));
                startActivity(intent);
            }
        });



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Collections.singletonList(
                                            new AuthUI.IdpConfig.PhoneBuilder().build()
                                    ))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void show_dialog() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_new_discussion,null);
        final EditText name_of_issue= mView.findViewById(R.id.new_issue_heading);
        Button submit = mView.findViewById(R.id.start_issue);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!name_of_issue.getText().toString().isEmpty()) {
                    String new_issue_name = name_of_issue.getText().toString();
                    new_issue_name= stringManipulation.compress(new_issue_name);
                    if(does_name_exist(new_issue_name)){
                        Toast.makeText(getApplicationContext(),"discussion of the given name already exist", Toast.LENGTH_LONG).show();
                    }else {
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), discussion_issue.class);
                        intent.putExtra("category", new_issue_name);
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "please fill the name of the issue", Toast.LENGTH_LONG).show();
                }
            }
        });

        mBuilder.setView(mView);

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();

    }

    public boolean does_name_exist(final String name){
        final boolean[] rv = {false};

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                rv[0] = name != null && !dataSnapshot.hasChild(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return rv[0];
    }

    public void populate_list(){

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                issues_list = new String[(int)dataSnapshot.getChildrenCount()];
                int i=0;

                for(DataSnapshot data : dataSnapshot.getChildren()){
                    issues_list[i]=stringManipulation.expand(data.getKey());
                    ++i;
                }

                listview.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, issues_list));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

