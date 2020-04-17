package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileActivity extends AppCompatActivity {

    private String recieveruserID, senderuserID,currentstate;

    private CircleImageView userprofileimage;
    private TextView userprofilename,userprofilestarus;
    private Button sendmessagerequestbutton, declinemessagerequestbutton;

    private DatabaseReference usersref, chatrequestref, contactsref, notificationref;
    private FirebaseAuth myauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        myauth= FirebaseAuth.getInstance();
        usersref= FirebaseDatabase.getInstance().getReference().child("Users");
        chatrequestref= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsref= FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationref= FirebaseDatabase.getInstance().getReference().child("Notifications");


        recieveruserID= getIntent().getExtras().get("visituserid").toString();
        senderuserID= myauth.getCurrentUser().getUid();


        userprofileimage =(CircleImageView) findViewById(R.id.visitprofileimage);
        userprofilename= (TextView) findViewById(R.id.visitusername);
        userprofilestarus= (TextView) findViewById(R.id.visitprofilestatus);
        sendmessagerequestbutton= (Button) findViewById(R.id.sendmessagerequestbutton);
        declinemessagerequestbutton= (Button) findViewById(R.id.declinemessagerequestbutton);
        currentstate= "new";

        retrieveuserinfo();

    }

    private void retrieveuserinfo() {
        usersref.child(recieveruserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() &&  (dataSnapshot.hasChild("Image"))){
                    String username= dataSnapshot.child("name").getValue().toString();
                    String userimage= dataSnapshot.child("Image").getValue().toString();
                    String userstatus= dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(userprofileimage);
                    userprofilename.setText(username);
                    userprofilestarus.setText(userstatus);

                    Managechatrequests();
                }
                else{
                    String username= dataSnapshot.child("name").getValue().toString();
                    String userstatus= dataSnapshot.child("status").getValue().toString();

                    userprofilename.setText(username);
                    userprofilestarus.setText(userstatus);

                    Managechatrequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void Managechatrequests() {

        chatrequestref.child(senderuserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(recieveruserID)){
                            String request_type= dataSnapshot.child(recieveruserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent")){
                                currentstate= "request_sent";
                                sendmessagerequestbutton.setText("Cancel Message Request");
                            }
                            else if (request_type.equals("recieved")){
                                currentstate= "request_recieved";
                                sendmessagerequestbutton.setText("Accept Message Request");

                                declinemessagerequestbutton.setVisibility(View.VISIBLE);
                                declinemessagerequestbutton.setEnabled(true);

                                declinemessagerequestbutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Cancelchatrequest();
                                    }
                                });

                            }
                        }
                        else{
                            contactsref.child(senderuserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(recieveruserID)){
                                                currentstate= "friends";
                                                sendmessagerequestbutton.setText("Unfriend");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(!senderuserID.equals(recieveruserID)){
            sendmessagerequestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendmessagerequestbutton.setEnabled(false);

                    if(currentstate.equals("new")){
                        sendchatrequest();
                    }
                    if(currentstate.equals("request_sent")){
                        Cancelchatrequest();
                    }
                    if(currentstate.equals("request_recieved")){
                        Acceptchatrequest();
                    }
                    if(currentstate.equals("friends")){
                        Removespecificcontact();
                    }
                }
            });
        }
        else{
            sendmessagerequestbutton.setVisibility(View.INVISIBLE);
        }
    }

    private void Removespecificcontact() {
        contactsref.child(senderuserID).child(recieveruserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            contactsref.child(recieveruserID).child(senderuserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendmessagerequestbutton.setEnabled(true);
                                                currentstate = "new";
                                                sendmessagerequestbutton.setText("Send Message");

                                                declinemessagerequestbutton.setVisibility(View.INVISIBLE);
                                                declinemessagerequestbutton.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void Acceptchatrequest() {
        contactsref.child(senderuserID).child(recieveruserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactsref.child(recieveruserID).child(senderuserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                chatrequestref.child(senderuserID).child(recieveruserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    chatrequestref.child(recieveruserID).child(senderuserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendmessagerequestbutton.setEnabled(true);
                                                                                    currentstate = "friends";
                                                                                    sendmessagerequestbutton.setText("Unfriend");

                                                                                    declinemessagerequestbutton.setVisibility(View.INVISIBLE);
                                                                                    declinemessagerequestbutton.setEnabled(false);
                                                                                }

                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void Cancelchatrequest() {
        chatrequestref.child(senderuserID).child(recieveruserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            chatrequestref.child(recieveruserID).child(senderuserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendmessagerequestbutton.setEnabled(true);
                                                currentstate = "new";
                                                sendmessagerequestbutton.setText("Send Message");

                                                declinemessagerequestbutton.setVisibility(View.INVISIBLE);
                                                declinemessagerequestbutton.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void sendchatrequest() {
        chatrequestref.child(senderuserID).child(recieveruserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            chatrequestref.child(recieveruserID).child(senderuserID)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> chatnotificationMap=new HashMap<>();
                                                chatnotificationMap.put("from", senderuserID);
                                                chatnotificationMap.put("type", "request");

                                                notificationref.child(recieveruserID).push()
                                                        .setValue(chatnotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    sendmessagerequestbutton.setEnabled(true);
                                                                    currentstate="request_sent";
                                                                    sendmessagerequestbutton.setText("Cancel Message Request");
                                                                }
                                                            }
                                                        });

                                                sendmessagerequestbutton.setEnabled(true);
                                                currentstate="request_sent";
                                                sendmessagerequestbutton.setText("Cancel Message Request");
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}
