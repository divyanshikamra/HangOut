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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneloginActivity extends AppCompatActivity {

    private Button sendverificationbutton, verifybutton;
    private EditText inputphonenumber, inputverifycode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private  PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonelogin);

        mAuth=FirebaseAuth.getInstance();

        sendverificationbutton= (Button) findViewById(R.id.send_ver_code_button);
        verifybutton= (Button) findViewById(R.id.verifybutton);
        inputphonenumber=(EditText) findViewById(R.id.phonenumberinput);
        inputverifycode=(EditText) findViewById(R.id.verificationinput);
        loadingbar= new ProgressDialog (this);

        sendverificationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phonenumber = inputphonenumber.getText().toString();
                if(TextUtils.isEmpty(phonenumber)){
                    Toast.makeText(PhoneloginActivity.this,"Phone Number is required",Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingbar.setTitle("Phone verification");
                    loadingbar.setMessage("Please wait, we are authenticating your phone");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phonenumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneloginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputphonenumber.setVisibility(View.INVISIBLE);
                sendverificationbutton.setVisibility(View.INVISIBLE);

                String verificationcode = inputverifycode.getText().toString();
                if (TextUtils.isEmpty(verificationcode)) {
                    Toast.makeText(PhoneloginActivity.this, "Please write first!", Toast.LENGTH_SHORT).show();
                } else {
                    loadingbar.setTitle("code verification");
                    loadingbar.setMessage("Please wait, we are verifying the code entered");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationcode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingbar.dismiss();

                Toast.makeText(PhoneloginActivity.this,"Invalid phone number!Please enter valid phone number with country code",Toast.LENGTH_SHORT).show();

                inputphonenumber.setVisibility(View.VISIBLE);
                sendverificationbutton.setVisibility(View.VISIBLE);

                verifybutton.setVisibility(View.INVISIBLE);
                inputverifycode.setVisibility(View.INVISIBLE);

            }

            public void onCodeSent( String verificationId,
                                    PhoneAuthProvider.ForceResendingToken token) {


                mVerificationId = verificationId;
                mResendToken = token;

                loadingbar.dismiss();
                Toast.makeText(PhoneloginActivity.this,"Code has been sent!",Toast.LENGTH_SHORT).show();

                inputphonenumber.setVisibility(View.INVISIBLE);
                sendverificationbutton.setVisibility(View.INVISIBLE);

                verifybutton.setVisibility(View.VISIBLE);
                inputverifycode.setVisibility(View.VISIBLE);
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingbar.dismiss();
                            Toast.makeText(PhoneloginActivity.this,"Congrulations! you have been logged in successfully",Toast.LENGTH_SHORT).show();
                            sendusertomainactivity();
                        }
                        else {

                            String message = task.getException().toString();
                            Toast.makeText(PhoneloginActivity.this,"Error:"+ message,Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendusertomainactivity() {
        Intent mainintent= new Intent(PhoneloginActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }

}
