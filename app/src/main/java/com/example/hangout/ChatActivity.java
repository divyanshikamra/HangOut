package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messagerecieverid, messagerecievername, messagerecieverimage, messagesenderid;
    private TextView username, userlastseen;
    private CircleImageView userimage;

    private Toolbar chattoolbar;
    private FirebaseAuth myauth;
    private DatabaseReference rootref;

    private ImageButton sendmessagebutton, sendfilesbutton;
    private EditText messageinputtext;

    private final List<Messages> messagesList= new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView usermessageslist;

    private String savecurrenttime,savecurrentdate;
    private String checker="",myurl="";
    private StorageTask uploadtask;
    private Uri fileuri;
    private ProgressDialog loadingbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myauth= FirebaseAuth.getInstance();
        messagesenderid= myauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();

        messagerecieverid= getIntent().getExtras().get("visit_user_id").toString();
        messagerecievername= getIntent().getExtras().get("visit_user_name").toString();
        messagerecieverimage= getIntent().getExtras().get("visit_image").toString();

        InitializeControllers();

        username.setText(messagerecievername);
        Picasso.get().load(messagerecieverimage).placeholder(R.drawable.profile_image).into(userimage);

        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sendmessage();
            }
        });

        Displaylastseen();

        sendfilesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF files",
                                "MS Word files"

                        };
                AlertDialog.Builder builder= new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select type of file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            checker="image";
                            Intent intent= new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"), 438);
                        }
                        if (i==1){
                            checker="pdf";
                            Intent intent= new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF File"), 438);
                        }
                        if (i==2){
                            checker="docx";
                            Intent intent= new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select MS Word File"), 438);
                        }
                    }
                });
                builder.show();

            }
        });
        rootref.child("Messages").child(messagesenderid).child(messagerecieverid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages= dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        usermessageslist.smoothScrollToPosition(usermessageslist.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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



    private void InitializeControllers() {

        chattoolbar= (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattoolbar);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarview= layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionbarview);

        userimage= (CircleImageView) findViewById(R.id.custom_profile_image);
        username= (TextView) findViewById(R.id.custom_profile_name);
        userlastseen= (TextView) findViewById(R.id.custom_user_lastseen);

        sendmessagebutton=(ImageButton) findViewById(R.id.send_message_button);
        sendfilesbutton=(ImageButton) findViewById(R.id.send_files_button);
        messageinputtext=(EditText) findViewById(R.id.inputmessage);

        messageAdapter= new MessageAdapter(messagesList);
        usermessageslist= (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager= new LinearLayoutManager(this);
        usermessageslist.setLayoutManager(linearLayoutManager);
        usermessageslist.setAdapter(messageAdapter);

        loadingbar= new ProgressDialog(this);

        Calendar calendar= Calendar.getInstance();

        SimpleDateFormat currentdate= new SimpleDateFormat("MMM dd,yyyy");
        savecurrentdate = currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime= new SimpleDateFormat("hh:mm a");
        savecurrenttime = currenttime.format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingbar.setTitle("Sending image");
            loadingbar.setMessage("Please wait, we are sending chosen file");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            fileuri=data.getData();

            if(!checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messagesenderref= "Messages/" + messagesenderid +"/" +messagerecieverid;
                final String messagerecieverref= "Messages/" + messagerecieverid +"/" + messagesenderid;

                DatabaseReference usermessagekeyref= rootref.child("Messages")
                        .child(messagesenderid).child(messagerecieverid).push();

                final String messagepushId = usermessagekeyref.getKey();

                final StorageReference filepath = storageReference.child(messagepushId+ "."+ checker);

                UploadTask uploadTask = filepath.putFile(fileuri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()){

                            Uri downloadUrl = task.getResult();
                            myurl = downloadUrl.toString();

                            Map messagetextbody= new HashMap();
                            messagetextbody.put("message",myurl);
                            messagetextbody.put("name",fileuri.getLastPathSegment());
                            messagetextbody.put("type",checker);
                            messagetextbody.put("from",messagesenderid);
                            messagetextbody.put("to",messagerecieverid);
                            messagetextbody.put("messageid",messagepushId);
                            messagetextbody.put("time",savecurrenttime);
                            messagetextbody.put("date",savecurrentdate);


                            Map messagebodydetails= new HashMap();
                            messagebodydetails.put(messagesenderref + "/" + messagepushId, messagetextbody);
                            messagebodydetails.put(messagerecieverref + "/" + messagepushId, messagetextbody);

                            rootref.updateChildren(messagebodydetails);
                            loadingbar.dismiss();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingbar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messagesenderref= "Messages/" + messagesenderid +"/" +messagerecieverid;
                final String messagerecieverref= "Messages/" + messagerecieverid +"/" + messagesenderid;

                DatabaseReference usermessagekeyref= rootref.child("Messages")
                        .child(messagesenderid).child(messagerecieverid).push();

                final String messagepushId = usermessagekeyref.getKey();

                final StorageReference filepath = storageReference.child(messagepushId+ "."+ "jpg");

                uploadtask = filepath.putFile(fileuri);

                uploadtask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }) .addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadurl= task.getResult();
                            myurl= downloadurl.toString();

                            Map messagetextbody= new HashMap();
                            messagetextbody.put("message",myurl);
                            messagetextbody.put("name",fileuri.getLastPathSegment());
                            messagetextbody.put("type",checker);
                            messagetextbody.put("from",messagesenderid);
                            messagetextbody.put("to",messagerecieverid);
                            messagetextbody.put("messageid",messagepushId);
                            messagetextbody.put("time",savecurrenttime);
                            messagetextbody.put("date",savecurrentdate);


                            Map messagebodydetails= new HashMap();
                            messagebodydetails.put(messagesenderref + "/" + messagepushId, messagetextbody);
                            messagebodydetails.put(messagerecieverref + "/" + messagepushId, messagetextbody);

                            rootref.updateChildren(messagebodydetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Message sent successfully",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        loadingbar.dismiss();;
                                        Toast.makeText(ChatActivity.this,"Error!",Toast.LENGTH_SHORT).show();
                                    }
                                    messageinputtext.setText("");
                                }
                            });

                        }
                    }
                });

            }
            else{
                loadingbar.dismiss();
                Toast.makeText(this,"No item selected, Error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void Displaylastseen(){
        rootref.child("Users").child(messagerecieverid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userstate").hasChild("state")){
                            String state= dataSnapshot.child("userstate").child("state").getValue().toString();
                            String date= dataSnapshot.child("userstate").child("date").getValue().toString();
                            String time= dataSnapshot.child("userstate").child("time").getValue().toString();

                            if(state.equals("online")){
                                userlastseen.setText("online");
                            }
                            else if (state.equals("offline")){
                                userlastseen.setText("Last seen: "  + date +"   " +time);
                            }
                        }
                        else{
                            userlastseen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void Sendmessage(){
        String messagetext= messageinputtext.getText().toString();

        if(TextUtils.isEmpty(messagetext)){
            Toast.makeText(this, "Can't send Empty message", Toast.LENGTH_SHORT).show();
        }
        else{
            String messagesenderref= "Messages/" + messagesenderid +"/" +messagerecieverid;
            String messagerecieverref= "Messages/" + messagerecieverid +"/" + messagesenderid;

            DatabaseReference usermessagekeyref= rootref.child("Messages")
                    .child(messagesenderid).child(messagerecieverid).push();

            String messagepushId = usermessagekeyref.getKey();

            Map messagetextbody= new HashMap();
            messagetextbody.put("message",messagetext);
            messagetextbody.put("type","text");
            messagetextbody.put("from",messagesenderid);
            messagetextbody.put("to",messagerecieverid);
            messagetextbody.put("messageid",messagepushId);
            messagetextbody.put("time",savecurrenttime);
            messagetextbody.put("date",savecurrentdate);


            Map messagebodydetails= new HashMap();
            messagebodydetails.put(messagesenderref + "/" + messagepushId, messagetextbody);
            messagebodydetails.put(messagerecieverref + "/" + messagepushId, messagetextbody);

            rootref.updateChildren(messagebodydetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this,"Message sent successfully",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this,"Error!",Toast.LENGTH_SHORT).show();
                    }
                    messageinputtext.setText("");
                }
            });

        }
    }
}
