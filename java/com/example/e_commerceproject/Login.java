package com.example.e_commerceproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.prefs.PreferenceChangeEvent;

public class Login extends AppCompatActivity {

    EditText eMail,Password;
    Button loginBtn;
    TextView createAccount;
    TextView resetPassword;
    ProgressBar progBar;
    FirebaseAuth fAuth;
    CheckBox rememberMe;
    SharedPreferences.Editor prefeEditor;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eMail = findViewById(R.id.editTextEmailLogin);
        Password = findViewById(R.id.editTextPasswordLogin);
        loginBtn = findViewById(R.id.buttonLogin);
        createAccount = findViewById(R.id.textViewCreateRegister);
        resetPassword = findViewById(R.id.textViewResetPassword);
        progBar = findViewById(R.id.progressBarLogin);
        fAuth = FirebaseAuth.getInstance();
        rememberMe=findViewById(R.id.rem);
        prefeEditor= PreferenceManager.getDefaultSharedPreferences(this).edit();
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("tag",sharedPreferences.getString("Rem","?"));
        if(sharedPreferences.getString("Rem","?").equals("true"))
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resetPasswordFn(v);
                EditText resetEmail=new EditText(v.getContext());
                AlertDialog.Builder passwordDialog=new AlertDialog.Builder(v.getContext());
                passwordDialog.setTitle("Reset Password").setMessage("Please Enter Your Email!");
                passwordDialog.setView(resetEmail);
                passwordDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail=resetEmail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Check Your Email To Reset Password!",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                });
                passwordDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordDialog.create().show();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = eMail.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    eMail.setError("E-mail is Required!");

                }
                if(TextUtils.isEmpty(password)){
                    Password.setError("Password is Required!");
                }
                if(password.length() < 6){
                    Password.setError("Password should at least contain 6 characters!");
                }
                progBar.setVisibility(View.VISIBLE);
                //authenticate the user
                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            if(rememberMe.isChecked())
                            {
                                prefeEditor.putString("Rem","true");
                                prefeEditor.apply();
                            }
                            Toast.makeText(Login.this,"Logged in Successfully!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(Login.this,"Error!"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    public void Register(View view) {
        startActivity(new Intent(Login.this,Register.class));
    }

    public void resetPasswordFn(View v) {

    }
}