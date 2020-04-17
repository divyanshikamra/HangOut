package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myviewpager;
    private TabLayout mytablayout;
    private tabaccessoradapter mytabsaccessoradaptor;

    private FirebaseAuth myauth;
    private DatabaseReference rootref;
    private String currentuserid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myauth= FirebaseAuth.getInstance();

        rootref= FirebaseDatabase.getInstance().getReference();

        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("HangOut");

        myviewpager= (ViewPager) findViewById(R.id.main_tabs_pager);
        mytabsaccessoradaptor= new tabaccessoradapter(getSupportFragmentManager());
        myviewpager.setAdapter(mytabsaccessoradaptor);

        mytablayout= (TabLayout) findViewById(R.id.main_tabs);
        mytablayout.setupWithViewPager(myviewpager);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=myauth.getCurrentUser();
        if (currentuser==null){
            sendusertologinactivity();
        }
        else{
            updateuserstatus("online");
            Verifyuserexistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentuser=myauth.getCurrentUser();
        if(currentuser != null){
            updateuserstatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentuser=myauth.getCurrentUser();
        if(currentuser != null){
            updateuserstatus("offline");
        }
    }

    private void Verifyuserexistence() {
        String currentuserID= myauth.getCurrentUser().getUid();
        rootref.child("Users").child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendusertosettingsactivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_option){

            updateuserstatus("offline");

            myauth.signOut();
            sendusertologinactivity();
        }
        if(item.getItemId()==R.id.main_settings_option){
            sendusertosettingsactivity();

        }
        if(item.getItemId()==R.id.main_create_group_option){
            requestnewgroup();
        }
        if(item.getItemId()==R.id.main_find_friends_option){

            sendusertofindfriendsactivity();
        }
        return true;
    }

    private void requestnewgroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupnamefield = new EditText(MainActivity.this);
        groupnamefield.setHint("e.g Wncc freshers 2019");
        builder.setView(groupnamefield);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupname = groupnamefield.getText().toString();
                if(TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this, "Please Enter Groupname", Toast.LENGTH_SHORT).show();
                }
                else{
                    createnewgroup( groupname);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createnewgroup(final String groupname) {
        rootref.child("Groups").child(groupname).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this,groupname+ " group is created",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendusertologinactivity() {
        Intent loginintent = new Intent(MainActivity.this,loginActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }
    private void sendusertosettingsactivity() {
        Intent settingsintent = new Intent(MainActivity.this,settingsActivity.class);
        startActivity(settingsintent);

    }
    private void sendusertofindfriendsactivity() {
        Intent findfriendsintent = new Intent(MainActivity.this,findFriendsActivity.class);
        startActivity(findfriendsintent);
    }
    private void updateuserstatus(String state){

        String savecurrenttime,savecurrentdate;

        Calendar calendar= Calendar.getInstance();

        SimpleDateFormat currentdate= new SimpleDateFormat("MMM dd,yyyy");
        savecurrentdate = currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime= new SimpleDateFormat("hh:mm a");
        savecurrenttime = currenttime.format(calendar.getTime());

        HashMap<String, Object> onlinestate = new HashMap<>();
        onlinestate.put("time", savecurrenttime);
        onlinestate.put("date", savecurrentdate);
        onlinestate.put("state", state);

        currentuserid=myauth.getCurrentUser().getUid();
        rootref.child("Users").child(currentuserid).child("userstate")
                .updateChildren(onlinestate);
    }
}
