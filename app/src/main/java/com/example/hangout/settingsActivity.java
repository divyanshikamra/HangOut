package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class settingsActivity extends AppCompatActivity {

    private Button updateaccountsettings;
    private EditText username, userstatus;
    private CircleImageView userprofileimage;
    private String currentuserID;
    private FirebaseAuth myauth;
    private DatabaseReference rootref;
    private  static final int Gallerypick=1;
    private StorageReference userprofileimageref;
    private ProgressDialog loadingbar;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myauth= FirebaseAuth.getInstance();
        currentuserID= myauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();
        userprofileimageref= FirebaseStorage.getInstance().getReference().child("Profile Images");

        Initializefields();

        username.setVisibility(View.INVISIBLE);

        updateaccountsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatesettings();
            }
        });

        Retrieveuserinfo();

        userprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,Gallerypick);
            }
        });
    }




    private void Initializefields() {
        updateaccountsettings = (Button) findViewById(R.id.update_settings_button);
        username= (EditText) findViewById(R.id.set_user_name);
        userstatus= (EditText) findViewById(R.id.set_user_status);
        userprofileimage= (CircleImageView) findViewById(R.id.set_profile_image);
        loadingbar= new ProgressDialog(this);
        settingsToolbar= (Toolbar) findViewById(R.id.settingstoolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallerypick && resultCode==RESULT_OK && data!=null){
            Uri Imageuri= data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){
                loadingbar.setTitle("Set profile image");
                loadingbar.setMessage("Please wait your profile image is uploading");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                Uri resulturi = result.getUri();


                final StorageReference filepath= userprofileimageref.child(currentuserID + ".jpg");

                UploadTask uploadtask= filepath.putFile(resulturi);
                Task<Uri> urltask = uploadtask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(settingsActivity.this,"Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            Uri downloadUri = task.getResult();
                            String downloadurl = downloadUri.toString();


                            rootref.child("Users").child(currentuserID).child("Image")
                                    .setValue(downloadurl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(settingsActivity.this,"Image saved successfully", Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                            else{
                                                String message = task.getException().toString();
                                                Toast.makeText(settingsActivity.this,"Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(settingsActivity.this,"Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                });

            }

        }

    }

    private void updatesettings() {
        String setusername = username.getText().toString();
        String setuserstatus = userstatus.getText().toString();

        if (TextUtils.isEmpty(setusername)){
            Toast.makeText(this, "Please enter username! ", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(setuserstatus)){
            Toast.makeText(this, "Please enter your status! ", Toast.LENGTH_SHORT).show();

        }
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
               profileMap.put("uid", currentuserID);
               profileMap.put("name", setusername);
               profileMap.put("status", setuserstatus);
            rootref.child("Users").child(currentuserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendusertomainactivity();
                                Toast.makeText(settingsActivity.this,"Profile updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message= task.getException().toString();
                                Toast.makeText(settingsActivity.this, "Error : " + message,Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

        }

    }
    private void Retrieveuserinfo() {
        rootref.child("Users").child(currentuserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("Image")) ){
                            String retrieveusername= dataSnapshot.child("name").getValue().toString();
                            String retrievestatus= dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage= dataSnapshot.child("Image").getValue().toString();

                            username.setText(retrieveusername);
                            userstatus.setText(retrievestatus);
                            Picasso.get().load(retrieveProfileImage).into(userprofileimage);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveusername= dataSnapshot.child("name").getValue().toString();
                            String retrievestatus= dataSnapshot.child("status").getValue().toString();

                            username.setText(retrieveusername);
                            userstatus.setText(retrievestatus);

                        }
                        else{
                            username.setVisibility((View.VISIBLE));
                            Toast.makeText(settingsActivity.this, "Please set and update your profile information", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendusertomainactivity() {
        Intent settingsintent = new Intent(settingsActivity.this,MainActivity.class);
        settingsintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsintent);
        finish();
    }
}
