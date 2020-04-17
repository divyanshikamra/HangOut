package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Context;

import static android.app.ProgressDialog.show;

public class registerActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText useremail, userpassword;
    private TextView alreadyhaveanaccountlink;
    private FirebaseAuth myauth;
    private ProgressDialog loadingbar;
    private DatabaseReference rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myauth= FirebaseAuth.getInstance();
        rootref= FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        alreadyhaveanaccountlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendusertologinactivity();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Createnewaccount();
            }
        });

    }

    private void Createnewaccount() {
        String email= useremail.getText().toString();
        String password= userpassword.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText( this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText( this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("Creating New Account");
            loadingbar.setMessage("Please Wait, while we are creating your account");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            myauth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                String currentuserID =myauth.getCurrentUser().getUid();
                                rootref.child("Users").child(currentuserID).setValue("");
                                sendusertomainactivity();
                                Toast.makeText(registerActivity.this, "Account created successfully",Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else{
                                String message= task.getException().toString();
                                Toast.makeText(registerActivity.this, "Error : " + message,Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {

        registerButton = (Button) findViewById(R.id.register_button);
        useremail = (EditText) findViewById(R.id.register_email);
        userpassword = (EditText) findViewById(R.id.register_password);
        alreadyhaveanaccountlink= (TextView) findViewById(R.id.already_have_an_account_link);
        loadingbar= new ProgressDialog(this);
    }
    private void sendusertologinactivity() {
        Intent loginintent = new Intent(registerActivity.this,loginActivity.class);
        startActivity(loginintent);
    }
    private void sendusertomainactivity() {
        Intent mainintent = new Intent(registerActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}
