package com.example.e_commerceproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Register extends AppCompatActivity {

    EditText fullName,Password,eMail,Phone,birthDate;
    Button registerBtn;
    TextView loginText;
    ProgressBar progBar;
    FirebaseAuth fAuth;
    DatePickerDialog.OnDateSetListener setListener;
    FirebaseUser user;
    DatabaseReference dr= FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.editTextFullName);
        Password = findViewById(R.id.editTextPasswordLogin);
        eMail = findViewById(R.id.editTextEmailLogin);
        Phone = findViewById(R.id.editTextPhone);
        birthDate = findViewById(R.id.editTextBirthDate);
        registerBtn = findViewById(R.id.buttonLogin);
        loginText = findViewById(R.id.textViewCreateRegister);
        progBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        birthDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog=new DatePickerDialog(Register.this, DatePicker::updateDate,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date;
                        date=String.valueOf(dayOfMonth).concat("/").concat(String.valueOf(month+1)).concat("/").concat(String.valueOf(year));
                        birthDate.setText(date);
                    }
                });
            }
        });

        /*if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }*/

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = eMail.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String phone = Phone.getText().toString().trim();
                String birthdate = birthDate.getText().toString().trim();
                String username =  fullName.getText().toString().trim();
                if(TextUtils.isEmpty(username)){
                    fullName.setError("Full Name is Required!");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    eMail.setError("E-mail is Required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Password.setError("Password is Required!");
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    Phone.setError("Phone is Required!");
                    return;
                }
                if(TextUtils.isEmpty(birthdate)){
                    birthDate.setError("Birth Date is Required!");
                    return;
                }
                if(password.length() < 6){
                    Password.setError("Password should at least contain 6 characters!");
                    return;
                }
                progBar.setVisibility(View.VISIBLE);
                //Register user in firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            saveUsers();
                            Toast.makeText(Register.this,"User Created Successfully!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(Register.this,"Error!"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
    public void Login(View view) {
        startActivity(new Intent(Register.this,Login.class));
        finish();
    }
    public void saveUsers(){
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("User Name").setValue(fullName.getText().toString());
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Email").setValue(eMail.getText().toString());
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Password").setValue(Password.getText().toString());
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Phone").setValue(Phone.getText().toString());
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Birth Date").setValue(birthDate.getText().toString());
    }



}