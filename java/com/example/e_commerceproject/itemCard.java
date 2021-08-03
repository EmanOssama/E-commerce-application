package com.example.e_commerceproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class itemCard extends AppCompatActivity {

    ImageView img,plus,minus;
    TextView name;
    TextView price,counter;
    FloatingActionButton addToCard;
    FirebaseAuth fAuth;
    DatabaseReference dr= FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_card);

        img = findViewById(R.id.imageData);
        name = findViewById(R.id.textViewNameData);
        price = findViewById(R.id.textPriceData);
        counter = findViewById(R.id.textCounter);
        plus=findViewById(R.id.imagePlus);
        minus=findViewById(R.id.imageNegative);
        fAuth = FirebaseAuth.getInstance();
        addToCard = findViewById(R.id.buttonAddCart);

        img.setImageResource(Integer.parseInt(getIntent().getExtras().get("img").toString()));
        name.setText(getIntent().getExtras().get("name").toString());
        price.setText(getIntent().getExtras().get("price").toString());
        if(getIntent().getExtras().get("quantity")!=null)
         counter.setText(getIntent().getExtras().get("quantity").toString());
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.setText(String.valueOf(Integer.parseInt(counter.getText().toString())+1));
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!counter.getText().toString().equals("1"))
                {
                    counter.setText(String.valueOf(Integer.parseInt(counter.getText().toString())-1));
                }
                else if(counter.getText().toString().equals("1")){
                    Toast.makeText(itemCard.this, "Please, one item should be selected from each product.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addToCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToCard();
            }
        });

    }
    public void addItemToCard(){
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Shopping Cart").child(name.getText().toString()).child("Name").setValue(name.getText().toString());
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Shopping Cart").child(name.getText().toString()).child("Price").setValue(price.getText().toString());
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Shopping Cart").child(name.getText().toString()).child("Quantity").setValue(counter.getText().toString());

        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Shopping Cart").child(name.getText().toString()).child("Total Price").setValue( String.valueOf(Integer.parseInt(counter.getText().toString())  *Integer.parseInt(price.getText().toString()))  );
        dr.child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Shopping Cart").child(name.getText().toString()).child("Image").setValue(getIntent().getExtras().get("img").toString());
        Toast.makeText(this, "Item added successfully to your cart.", Toast.LENGTH_SHORT).show();
    }
}