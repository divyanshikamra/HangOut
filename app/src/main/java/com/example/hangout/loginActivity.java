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
import com.google.firebase.auth.FirebaseUser;


public class loginActivity extends AppCompatActivity {

    private FirebaseAuth myauth;
    private ProgressDialog loadingbar;
    private Button LoginButton, phoneloginbutton;
    private EditText useremail, userpassword;
    private TextView neednewaccountlink , forgetpasswordlink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myauth = FirebaseAuth.getInstance();

        InitializeFields();

        neednewaccountlink.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                sendusertoregisteractivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alloeusertologin();
            }
        });

        phoneloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneloginintent = new Intent(loginActivity.this, PhoneloginActivity.class);
                startActivity(phoneloginintent);
            }
        });
    }

    private void Alloeusertologin() {

        String email= useremail.getText().toString();
        String password= userpassword.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText( this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText( this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("Sign In");
            loadingbar.setMessage("Please Wait, while we are opening your account");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            myauth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if  (task.isSuccessful()){
                                String currentuserid= myauth.getCurrentUser().getUid();


                                sendusertomainactivity();
                                Toast.makeText(loginActivity.this, "Logged In Successfully",Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else{
                                String message= task.getException().toString();
                                Toast.makeText(loginActivity.this, "Error : " + message,Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        phoneloginbutton = (Button) findViewById(R.id.phone_login_button);
        useremail = (EditText) findViewById(R.id.login_email);
        userpassword = (EditText) findViewById(R.id.login_password);
        neednewaccountlink= (TextView) findViewById(R.id.need_an_account_link);
        forgetpasswordlink= (TextView) findViewById(R.id.forget_password_link);
        loadingbar= new ProgressDialog(this);
    }



    private void sendusertomainactivity() {
        Intent mainintent = new Intent(loginActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    private void sendusertoregisteractivity() {
        Intent registerintent = new Intent(loginActivity.this,registerActivity.class);
        startActivity(registerintent);
    }
}
