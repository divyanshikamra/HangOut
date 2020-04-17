package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class groupchatActivity extends AppCompatActivity {

    private Toolbar mytoolbar;
    private ImageButton sendmymessagebutton;
    private EditText usermessageinput;
    private ScrollView myscrollview;
    private TextView displaytextmessage;

    private FirebaseAuth myauth;
    private DatabaseReference userref, groupnameref, groupmessagerefkey;

    private String currentgroupname, currentuserID, currentusername, currentdate, currenttime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        currentgroupname= getIntent().getExtras().get("groupname").toString();
        Toast.makeText(groupchatActivity.this, currentgroupname, Toast.LENGTH_SHORT).show();


        myauth=FirebaseAuth.getInstance();
        currentuserID = myauth.getCurrentUser().getUid();
        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameref= FirebaseDatabase.getInstance().getReference().child("Groups").child(currentgroupname);

        InitializeFields();

        getuserinfo();

        sendmymessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savemessageinfotodatabase();

                usermessageinput.setText("");
                myscrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    Displaymessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    Displaymessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void InitializeFields() {
        mytoolbar= (Toolbar) findViewById(R.id.groupchatbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setTitle(currentgroupname);

        sendmymessagebutton= (ImageButton) findViewById(R.id.sendmessagebutton);
        usermessageinput= (EditText) findViewById(R.id.inputgroupmessage);
        displaytextmessage=(TextView) findViewById(R.id.groupchatdisplay);
        myscrollview= (ScrollView) findViewById(R.id.myscrollview);
    }

    private void getuserinfo() {
        userref.child(currentuserID). addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentusername=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void savemessageinfotodatabase() {
        String message = usermessageinput.getText().toString();
        String messagekey= groupnameref.push().getKey();

        if (TextUtils.isEmpty(message)){
            Toast.makeText( this, "Please write message first ", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat currentDataformat = new SimpleDateFormat("MMM dd,yyyy");
            currentdate= currentDataformat.format(calfordate.getTime());

            Calendar calfortime = Calendar.getInstance();
            SimpleDateFormat currenttimeformat = new SimpleDateFormat("hh:mm a");
            currenttime= currenttimeformat.format(calfortime.getTime());

            HashMap<String, Object> groupmessagekey = new HashMap<>();
            groupnameref.updateChildren(groupmessagekey);

            groupmessagerefkey = groupnameref.child(messagekey);

            HashMap<String, Object> messageinfomap = new HashMap<>();
               messageinfomap.put("name",currentusername);
               messageinfomap.put("message",message);
               messageinfomap.put("date",currentdate);
               messageinfomap.put("time",currenttime);
            groupmessagerefkey.updateChildren(messageinfomap);
        }
    }

    private void Displaymessages(DataSnapshot dataSnapshot) {
        Iterator iterator= dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatdate= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatmessage= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatname= (String) ((DataSnapshot)iterator.next()).getValue();
            String chattime= (String) ((DataSnapshot)iterator.next()).getValue();

            displaytextmessage.append(chatname + " :\n"+ chatmessage + "\n" + chattime +"    " +chatdate+ "\n\n\n");
            myscrollview.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

}
