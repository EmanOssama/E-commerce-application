package com.example.e_commerceproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_commerceproject.adapter.productInfoAdapter;
import com.example.e_commerceproject.model.productItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.collection.ImmutableSortedMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements itemClick{
    DrawerLayout drawerLayout;
    ImageView getNavDrawable;
    NavigationView navigationView;
    RecyclerView itemsRecyclerView;
    ViewGroup root;
    productInfoAdapter itemAdapter;
    productItem product;
    ImageView searchText,searchQr,searchVoice;
    SharedPreferences.Editor editor;
    SharedPreferences readPref;
    FirebaseAuth fAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceOrders;
    itemClick con;
    Button submitOrder;
    TextView price,le;
    ImageView mapLocationUser;
    Boolean checkLongClick=false;

    ArrayList<productItem> homeList = new ArrayList<>();
    ArrayList<productItem> itemList = new ArrayList<productItem>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout=findViewById(R.id.drawer_layout);
        getNavDrawable=findViewById(R.id.nav_draw);
        navigationView=findViewById(R.id.nav_drawable);
        searchText=findViewById(R.id.search_txt);
        searchQr=findViewById(R.id.search_qr);
        searchVoice=findViewById(R.id.serach_voice);
        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        fAuth = FirebaseAuth.getInstance();
        submitOrder = findViewById(R.id.buttonOrder);
        price = findViewById(R.id.textViewTotalPrice);
        le=findViewById(R.id.textView5);
        mapLocationUser = findViewById(R.id.mapicon);
        con=this;
        readPref = PreferenceManager.getDefaultSharedPreferences(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Shopping Cart");
        databaseReferenceOrders = FirebaseDatabase.getInstance().getReference().child("Accounts").child(fAuth.getCurrentUser().getUid()).child("Orders").child("ItemsInOrder");

        //search icon text call search fn to take user input then call searchall fn to apply search
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Search();
            }
        });

        //map icon => open maps activity
        mapLocationUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
            }
        });

        //search by voice icon call searchvoice fn
        searchVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchVoice();
            }
        });

        //search by qr icon open scanner view activity
        searchQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,scannerView.class));
                //-----------------
            }
        });

        //submit order in shopping cart
        submitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //make sure that user's address is located first then remove or clear the shopping cart itemList list
                if(!readPref.getString("KeyAddress","?").equals("?")){
                    itemsRecyclerView.removeAllViewsInLayout();
                    price.setText("0");
                    databaseReference.removeValue();
                    itemList.clear();
                    Toast.makeText(MainActivity.this, "Your order is shipped successfully.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Please, Enter your address first.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //drawer
        navigationView.setItemIconTintList(null);
        setHomeProducts();
        if(getIntent().getStringExtra("qr")!=null&&getIntent().getStringExtra("qr")!="") {
            searchAll(getIntent().getStringExtra("qr"));
            getIntent().putExtra("qr","");

        }

        //drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //logout
                if(item.getItemId()==R.id.Logout)
                {
                    editor.clear();
                    editor.commit();
                    startActivity(new Intent(MainActivity.this,Login.class));
                    finish();
                }
                //shopping cart set visibility of map and total price with true
                else if(item.getItemId()==R.id.ShoppingCart)
                {
                    checkLongClick=true;
                    mapLocationUser.setVisibility(View.VISIBLE);
                    submitOrder.setVisibility(View.VISIBLE);
                    le.setVisibility(View.VISIBLE);
                    price.setVisibility(View.VISIBLE);
                    shoppingCartUserItems();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //home
                else if(item.getItemId()==R.id.Home)
                {
                    hideIcons();
                    checkLongClick=false;
                    setHomeProducts();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //belts
                else if(item.getItemId()==R.id.Belts)
                {
                    hideIcons();
                    checkLongClick=false;
                    setBeltsProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //heels
                else if(item.getItemId()==R.id.Heels) {
                    hideIcons();
                    checkLongClick=false;
                    setHeelsProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //bags
                else if(item.getItemId()==R.id.Bags)
                {
                    hideIcons();
                    checkLongClick=false;
                    setBagsProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //sun glasses
                else if(item.getItemId()==R.id.SunGlasses)
                {
                    hideIcons();
                    checkLongClick=false;
                    setSunGlassesProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //lip stick
                else if(item.getItemId()==R.id.LipStick)
                {
                    hideIcons();
                    checkLongClick=false;
                    setLipStickProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //eyeliner
                else if(item.getItemId()==R.id.Eyeliner)
                {
                    hideIcons();
                    checkLongClick=false;
                    setEyeLinerProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //foundation
                else if(item.getItemId()==R.id.Foundation)
                {
                    hideIcons();
                    checkLongClick=false;
                    setFoundationProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //t-shirts
                else if(item.getItemId()==R.id.Tshirts)
                {
                    hideIcons();
                    checkLongClick=false;
                    setTShirtProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //blouses
                else if(item.getItemId()==R.id.Blouses)
                {
                    hideIcons();
                    checkLongClick=false;
                    setBlousesProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //pantaloons
                else if(item.getItemId()==R.id.Pantaloons)
                {
                    hideIcons();
                    checkLongClick=false;
                    setPantalonsProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //skirts
                else if(item.getItemId()==R.id.Skirts)
                {
                    hideIcons();
                    checkLongClick=false;
                    setSkirtsProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                //dresses
                else if(item.getItemId()==R.id.Dresses)
                {
                    hideIcons();
                    checkLongClick=false;
                    setDressesProduct();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            }
        });
    }

    //hide map and total price in all tabs except shopping cart
    public void hideIcons(){
        mapLocationUser.setVisibility(View.GONE);
        submitOrder.setVisibility(View.GONE);
        le.setVisibility(View.GONE);
        price.setVisibility(View.GONE);
    }

    //add items of user to the shopping cart to be displayed for the user in the shopping cart tab
    public void shoppingCartUserItems(){
        totalPrice=0;
        itemList.clear();
        //to load data from firebase and set it in the recycler view
        new asyncTask().execute();
        if(itemList.size()==0){
            itemsRecyclerView.removeAllViewsInLayout();
        }
    }
   public long totalPrice=0;

    public class asyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    int c = 0;
                    String img="",name="",priceItem="",quantity="";
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        c++;
                        if (c % 5 == 0) {
                            totalPrice+=Long.parseLong(dataSnapshot.getValue().toString());
                            itemList.add(new productItem(name,priceItem,Integer.parseInt(img),Integer.parseInt(quantity)));
                            c = 0;
                        } else {
                            if (c == 1) {
                                img=dataSnapshot.getValue().toString();
                            }
                            else if (c == 2) {
                                name=dataSnapshot.getValue().toString();
                            }
                            else if (c == 3) {
                                priceItem=dataSnapshot.getValue().toString();
                            }
                            else if (c == 4) {
                                quantity=dataSnapshot.getValue().toString();
                            }
                        }
                    }
                    itemAdapter=new productInfoAdapter(getApplicationContext(),itemList,con);
                    itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    itemsRecyclerView.setAdapter(itemAdapter);
                    price.setText(String.valueOf(totalPrice));
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                //to update total price for the user after remove an item from shopping cart
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(dataSnapshot.getKey().equals("Total Price")){
                            totalPrice -= Long.parseLong(dataSnapshot.getValue().toString());
                            price.setText(String.valueOf(totalPrice));
                        }
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    //logout check fn in case of yes go to login activity
    private static void logout(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout ?!");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finishAffinity();
                activity.startActivity(new Intent(activity,Login.class));
                //System.exit(0);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void openDrawer(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void Search() {
        final EditText search=new EditText(getApplicationContext());
        new AlertDialog.Builder(this).setTitle("Enter Item Name").
                setView(search).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                searchAll(search.getText().toString());
            }
        }).setNegativeButton("Cancel",null).show();
    }

    private void searchVoice()
    {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi, Please raise your voice to search what you want.");
        try {
            startActivityForResult(intent,1);
        }catch (ActivityNotFoundException e)
        {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1)
        {
            if(resultCode==-1&&data!=null)
            {
                ArrayList<String>result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                searchAll(result.get(0));
            }
        }
    }
    private void searchAll(String txt)
    {
        ArrayList<productItem>lst=new ArrayList<>();
        for(int i=0;i<homeList.size();i++)
        {
            if( homeList.get(i).getName().toLowerCase().contains(txt.toLowerCase())) {
                lst.add(homeList.get(i));
            }
        }
        if(lst.size()==0)
        {
            Toast.makeText(this, "Item does not exist here!.", Toast.LENGTH_SHORT).show();
        }
        else {
            Collections.shuffle(lst);
            setItemRecyclerView(lst);
        }
    }
    @Override
    public void onClickItem(int position) {
        Intent i=new Intent(MainActivity.this, itemCard.class);
        //on click at shopping cart
        if(checkLongClick){
            i.putExtra("img", String.valueOf(itemList.get(position).getImage()));
            i.putExtra("name", itemList.get(position).getName());
            i.putExtra("price", itemList.get(position).getPrice());
            i.putExtra("quantity", itemList.get(position).getQuantity());
            // (itemList.get(position).getQuantity()) * Long.parseLong( homeList.get(position).getPrice())
        }
        //on click at home or any other tab
        else{
            i.putExtra("img", String.valueOf(homeList.get(position).getImage()));
            i.putExtra("name", homeList.get(position).getName());
            i.putExtra("price", homeList.get(position).getPrice());
        }
        startActivity(i);
    }

    @Override
    public void onLongClickItem(int position) {
        //user can long press only in shopping cart to remove
        if(checkLongClick) {
            new AlertDialog.Builder(this).setTitle("Delete Item.")
                    .setMessage("Do You Want To Delete This Product Already ?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.child(itemList.get(position).getName().toString()).removeValue();
                            itemList.remove(position);
                            itemAdapter.notifyItemRemoved(position);
                        }
                    })
                    .setNegativeButton("NO", null).show();
        }
    }

    @Override
    //back button ho to home tab
    public void onBackPressed() {
        setHomeProducts();
    }

    //set items i shopping cart recycler view
    public void setItemRecyclerView(List<productItem> productList)
    {
        itemsRecyclerView=findViewById(R.id.rec);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.HORIZONTAL,false);
        itemsRecyclerView.setLayoutManager(layoutManager);
        itemAdapter=new productInfoAdapter(getApplicationContext(),productList,this);
        itemsRecyclerView.setAdapter(itemAdapter);
    }
    //fill foundations data
    public  void setFoundationProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Foundation 1","180",R.drawable.foundation1,1));
        homeList.add(new productItem("Foundation 2","200",R.drawable.foundation2,1));
        homeList.add(new productItem("Foundation 3","220",R.drawable.foundation3,1));
        homeList.add(new productItem("Foundation 4","240",R.drawable.foundation4,1));
        homeList.add(new productItem("Foundation 5","260",R.drawable.foundation5,1));
        homeList.add(new productItem("Foundation 6","280",R.drawable.foundation6,1));
        homeList.add(new productItem("Foundation 7","300",R.drawable.foundation7,1));
        homeList.add(new productItem("Foundation 8","320",R.drawable.foundation8,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill eyeliners data
    public  void setEyeLinerProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Eyeliner 1","100",R.drawable.eyeliner1,1));
        homeList.add(new productItem("Eyeliner 2","120",R.drawable.eyeliner2,1));
        homeList.add(new productItem("Eyeliner 3","140",R.drawable.eyeliner3,1));
        homeList.add(new productItem("Eyeliner 4","160",R.drawable.eyeliner4,1));
        homeList.add(new productItem("Eyeliner 5","180",R.drawable.eyeliner5,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill lip sticks data
    public  void setLipStickProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Lip Stick 1","50",R.drawable.lipstick1,1));
        homeList.add(new productItem("Lip Stick 2","35",R.drawable.lipstick2,1));
        homeList.add(new productItem("Lip Stick 3","55",R.drawable.lipstick3,1));
        homeList.add(new productItem("Lip Stick 4","45",R.drawable.lipstick4,1));
        homeList.add(new productItem("Lip Stick 5","50",R.drawable.lipstick5,1));
        homeList.add(new productItem("Lip Stick 6","30",R.drawable.lipstick6,1));
        homeList.add(new productItem("Lip Stick 7","20",R.drawable.lipstick7,1));
        homeList.add(new productItem("Lip Stick 8","40",R.drawable.lipstick8,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill bags data
    public  void setBagsProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Bag 1","100",R.drawable.bag1,1));
        homeList.add(new productItem("Bag 2","300",R.drawable.bag2,1));
        homeList.add(new productItem("Bag 3","350",R.drawable.bag3,1));
        homeList.add(new productItem("Bag 4","150",R.drawable.bag4,1));
        homeList.add(new productItem("Bag 5","350",R.drawable.bag5,1));
        homeList.add(new productItem("Bag 6","200",R.drawable.bag6,1));
        homeList.add(new productItem("Bag 7","170",R.drawable.bag7,1));
        homeList.add(new productItem("Bag 8","200",R.drawable.bag8,1));
        homeList.add(new productItem("Bag 9","180",R.drawable.bag9,1));
        homeList.add(new productItem("Bag 10","150",R.drawable.bag10,1));
        homeList.add(new productItem("Bag 11","220",R.drawable.bag11,1));
        homeList.add(new productItem("Bag 12","220",R.drawable.bag12,1));
        homeList.add(new productItem("Bag 13","130",R.drawable.bag13,1));
        homeList.add(new productItem("Bag 14","250",R.drawable.bag14,1));
        homeList.add(new productItem("Bag 15","250",R.drawable.bag15,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill heels data
    public  void setHeelsProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Heels 1","220",R.drawable.heels1,1));
        homeList.add(new productItem("Heels 2","180",R.drawable.heels2,1));
        homeList.add(new productItem("Heels 3","200",R.drawable.heels3,1));
        homeList.add(new productItem("Heels 4","190",R.drawable.heels4,1));
        homeList.add(new productItem("Heels 5","180",R.drawable.heels5,1));
        homeList.add(new productItem("Heels 6","250",R.drawable.heels6,1));
        homeList.add(new productItem("Heels 7","180",R.drawable.heels7,1));
        homeList.add(new productItem("Heels 8","250",R.drawable.heels8,1));
        homeList.add(new productItem("Heels 9","180",R.drawable.heels9,1));
        homeList.add(new productItem("Heels 10","200",R.drawable.heels10,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill sunglasses data
    public  void setSunGlassesProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Sun Glasses 1","200",R.drawable.sunglasses1,1));
        homeList.add(new productItem("Sun Glasses 2","220",R.drawable.sunglasses2,1));
        homeList.add(new productItem("Sun Glasses 3","50",R.drawable.sunglasses3,1));
        homeList.add(new productItem("Sun Glasses 4","180",R.drawable.sunglasses4,1));
        homeList.add(new productItem("Sun Glasses 5","200",R.drawable.sunglasses5,1));
        homeList.add(new productItem("Sun Glasses 6","100",R.drawable.sunglasses6,1));
        homeList.add(new productItem("Sun Glasses 7","220",R.drawable.sunglasses7,1));
        homeList.add(new productItem("Sun Glasses 8","250",R.drawable.sunglasses8,1));
        homeList.add(new productItem("Sun Glasses 9","180",R.drawable.sunglasses9,1));
        homeList.add(new productItem("Sun Glasses 10","150",R.drawable.sunglasses10,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill dresses data
    public  void setDressesProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Dress 1","200",R.drawable.dress1,1));
        homeList.add(new productItem("Dress 2","320",R.drawable.dress2,1));
        homeList.add(new productItem("Dress 3","260",R.drawable.dress3,1));
        homeList.add(new productItem("Dress 4","280",R.drawable.dress4,1));
        homeList.add(new productItem("Dress 5","340",R.drawable.dress5,1));
        homeList.add(new productItem("Dress 6","220",R.drawable.dress6,1));
        homeList.add(new productItem("Dress 7","320",R.drawable.dress7,1));
        homeList.add(new productItem("Dress 8","360",R.drawable.dress8,1));
        homeList.add(new productItem("Dress 9","380",R.drawable.dress9,1));
        homeList.add(new productItem("Dress 10","300",R.drawable.dress10,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill skirts data
    public  void setSkirtsProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Skirt 1","250",R.drawable.skirt1,1));
        homeList.add(new productItem("Skirt 2","300",R.drawable.skirt2,1));
        homeList.add(new productItem("Skirt 3","280",R.drawable.skirt3,1));
        homeList.add(new productItem("Skirt 4","220",R.drawable.skirt4,1));
        homeList.add(new productItem("Skirt 5","220",R.drawable.skirt5,1));
        homeList.add(new productItem("Skirt 6","180",R.drawable.skirt6,1));
        homeList.add(new productItem("Skirt 7","160",R.drawable.skirt7,1));
        homeList.add(new productItem("Skirt 8","180",R.drawable.skirt8,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill pantaloons data
    public  void setPantalonsProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Pantaloon 1","320",R.drawable.pantalon1,1));
        homeList.add(new productItem("Pantaloon 2","250",R.drawable.pantalon2,1));
        homeList.add(new productItem("Pantaloon 3","350",R.drawable.pantalon3,1));
        homeList.add(new productItem("Pantaloon 4","280",R.drawable.pantalon4,1));
        homeList.add(new productItem("Pantaloon 5","320",R.drawable.pantalon5,1));
        homeList.add(new productItem("Pantaloon 6","300",R.drawable.pantalon6,1));
        homeList.add(new productItem("Pantaloon 7","220",R.drawable.pantalon7,1));
        homeList.add(new productItem("Pantaloon 8","280",R.drawable.pantalon8,1));
        homeList.add(new productItem("Pantaloon 9","250",R.drawable.pantalon9,1));
        homeList.add(new productItem("Pantaloon 10","320",R.drawable.pantalon10,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);

    }
    //fill blouses data
    public  void setBlousesProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Blouse 1","220",R.drawable.blouse1,1));
        homeList.add(new productItem("Blouse 2","260",R.drawable.blouse2,1));
        homeList.add(new productItem("Blouse 3","250",R.drawable.blouse3,1));
        homeList.add(new productItem("Blouse 4","400",R.drawable.blouse4,1));
        homeList.add(new productItem("Blouse 5","360",R.drawable.blouse5,1));
        homeList.add(new productItem("Blouse 6","350",R.drawable.blouse6,1));
        homeList.add(new productItem("Blouse 7","360",R.drawable.blouse7,1));
        homeList.add(new productItem("Blouse 8","380",R.drawable.blouse8,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill t-shirts data
    public  void setTShirtProduct()
    {
        homeList.clear();
        homeList.add(new productItem("T-shirt 1","220",R.drawable.tshirt1,1));
        homeList.add(new productItem("T-shirt 2","160",R.drawable.tshirt2,1));
        homeList.add(new productItem("T-shirt 3","200",R.drawable.tshirt3,1));
        homeList.add(new productItem("T-shirt 4","180",R.drawable.tshirt4,1));
        homeList.add(new productItem("T-shirt 5","200",R.drawable.tshirt5,1));
        homeList.add(new productItem("T-shirt 6","170",R.drawable.tshirt6,1));
        homeList.add(new productItem("T-shirt 7","220",R.drawable.tshirt7,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //set belts data
    public void setBeltsProduct()
    {
        homeList.clear();
        homeList.add(new productItem("Belt 1","60",R.drawable.belt1,1));
        homeList.add(new productItem("Belt 2","120",R.drawable.belt2,1));
        homeList.add(new productItem("Belt 3","60",R.drawable.belt3,1));
        homeList.add(new productItem("Belt 4","50",R.drawable.belt4,1));
        homeList.add(new productItem("Belt 5","80",R.drawable.belt5,1));
        homeList.add(new productItem("Belt 6","40",R.drawable.belt6,1));
        homeList.add(new productItem("Belt 7","60",R.drawable.belt7,1));
        homeList.add(new productItem("Belt 8","50",R.drawable.belt8,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
    //fill home data
    public void setHomeProducts()
    {
        homeList.clear();
        //Bags
        homeList.add(new productItem("Bag 1","100",R.drawable.bag1,1));
        homeList.add(new productItem("Bag 2","300",R.drawable.bag2,1));
        homeList.add(new productItem("Bag 3","350",R.drawable.bag3,1));
        homeList.add(new productItem("Bag 4","150",R.drawable.bag4,1));
        homeList.add(new productItem("Bag 5","350",R.drawable.bag5,1));
        homeList.add(new productItem("Bag 6","200",R.drawable.bag6,1));
        homeList.add(new productItem("Bag 7","170",R.drawable.bag7,1));
        homeList.add(new productItem("Bag 8","200",R.drawable.bag8,1));
        homeList.add(new productItem("Bag 9","180",R.drawable.bag9,1));
        homeList.add(new productItem("Bag 10","150",R.drawable.bag10,1));
        homeList.add(new productItem("Bag 11","220",R.drawable.bag11,1));
        homeList.add(new productItem("Bag 12","220",R.drawable.bag12,1));
        homeList.add(new productItem("Bag 13","130",R.drawable.bag13,1));
        homeList.add(new productItem("Bag 14","250",R.drawable.bag14,1));
        homeList.add(new productItem("Bag 15","250",R.drawable.bag15,1));
        //-----Heels-------------------------------------------------------
        homeList.add(new productItem("Heels 1","220",R.drawable.heels1,1));
        homeList.add(new productItem("Heels 2","180",R.drawable.heels2,1));
        homeList.add(new productItem("Heels 3","200",R.drawable.heels3,1));
        homeList.add(new productItem("Heels 4","190",R.drawable.heels4,1));
        homeList.add(new productItem("Heels 5","180",R.drawable.heels5,1));
        homeList.add(new productItem("Heels 6","250",R.drawable.heels6,1));
        homeList.add(new productItem("Heels 7","180",R.drawable.heels7,1));
        homeList.add(new productItem("Heels 8","250",R.drawable.heels8,1));
        homeList.add(new productItem("Heels 9","180",R.drawable.heels9,1));
        homeList.add(new productItem("Heels 10","200",R.drawable.heels10,1));
        //------------------Belts----------------------------------------
        homeList.add(new productItem("Belt 1","60",R.drawable.belt1,1));
        homeList.add(new productItem("Belt 2","120",R.drawable.belt2,1));
        homeList.add(new productItem("Belt 3","60",R.drawable.belt3,1));
        homeList.add(new productItem("Belt 4","50",R.drawable.belt4,1));
        homeList.add(new productItem("Belt 5","80",R.drawable.belt5,1));
        homeList.add(new productItem("Belt 6","40",R.drawable.belt6,1));
        homeList.add(new productItem("Belt 7","60",R.drawable.belt7,1));
        homeList.add(new productItem("Belt 8","50",R.drawable.belt8,1));
        //-----------------sunGlasses----------------------------------------
        homeList.add(new productItem("Sun Glasses 1","200",R.drawable.sunglasses1,1));
        homeList.add(new productItem("Sun Glasses 2","220",R.drawable.sunglasses2,1));
        homeList.add(new productItem("Sun Glasses 3","50",R.drawable.sunglasses3,1));
        homeList.add(new productItem("Sun Glasses 4","180",R.drawable.sunglasses4,1));
        homeList.add(new productItem("Sun Glasses 5","200",R.drawable.sunglasses5,1));
        homeList.add(new productItem("Sun Glasses 6","100",R.drawable.sunglasses6,1));
        homeList.add(new productItem("Sun Glasses 7","220",R.drawable.sunglasses7,1));
        homeList.add(new productItem("Sun Glasses 8","250",R.drawable.sunglasses8,1));
        homeList.add(new productItem("Sun Glasses 9","180",R.drawable.sunglasses9,1));
        homeList.add(new productItem("Sun Glasses 10","150",R.drawable.sunglasses10,1));
        //-------------------LipStick----------------------
        homeList.add(new productItem("Lip Stick 1","50",R.drawable.lipstick1,1));
        homeList.add(new productItem("Lip Stick 2","35",R.drawable.lipstick2,1));
        homeList.add(new productItem("Lip Stick 3","55",R.drawable.lipstick3,1));
        homeList.add(new productItem("Lip Stick 4","45",R.drawable.lipstick4,1));
        homeList.add(new productItem("Lip Stick 5","50",R.drawable.lipstick5,1));
        homeList.add(new productItem("Lip Stick 6","30",R.drawable.lipstick6,1));
        homeList.add(new productItem("Lip Stick 7","20",R.drawable.lipstick7,1));
        homeList.add(new productItem("Lip Stick 8","40",R.drawable.lipstick8,1));
        //---------------------Eyeliner-------------------------------------
        homeList.add(new productItem("Eyeliner 1","100",R.drawable.eyeliner1,1));
        homeList.add(new productItem("Eyeliner 2","120",R.drawable.eyeliner2,1));
        homeList.add(new productItem("Eyeliner 3","140",R.drawable.eyeliner3,1));
        homeList.add(new productItem("Eyeliner 4","160",R.drawable.eyeliner4,1));
        homeList.add(new productItem("Eyeliner 5","180",R.drawable.eyeliner5,1));
        //------------------Foundation------------------------------
        homeList.add(new productItem("Foundation 1","180",R.drawable.foundation1,1));
        homeList.add(new productItem("Foundation 2","200",R.drawable.foundation2,1));
        homeList.add(new productItem("Foundation 3","220",R.drawable.foundation3,1));
        homeList.add(new productItem("Foundation 4","240",R.drawable.foundation4,1));
        homeList.add(new productItem("Foundation 5","260",R.drawable.foundation5,1));
        homeList.add(new productItem("Foundation 6","280",R.drawable.foundation6,1));
        homeList.add(new productItem("Foundation 7","300",R.drawable.foundation7,1));
        homeList.add(new productItem("Foundation 8","320",R.drawable.foundation8,1));
        //-------------------------T-shirt-------------------------------------------------------
        homeList.add(new productItem("T-shirt 1","220",R.drawable.tshirt1,1));
        homeList.add(new productItem("T-shirt 2","160",R.drawable.tshirt2,1));
        homeList.add(new productItem("T-shirt 3","200",R.drawable.tshirt3,1));
        homeList.add(new productItem("T-shirt 4","180",R.drawable.tshirt4,1));
        homeList.add(new productItem("T-shirt 5","200",R.drawable.tshirt5,1));
        homeList.add(new productItem("T-shirt 6","170",R.drawable.tshirt6,1));
        homeList.add(new productItem("T-shirt 7","220",R.drawable.tshirt7,1));
        //----------------------Blouses--------------------------
        homeList.add(new productItem("Blouse 1","220",R.drawable.blouse1,1));
        homeList.add(new productItem("Blouse 2","260",R.drawable.blouse2,1));
        homeList.add(new productItem("Blouse 3","250",R.drawable.blouse3,1));
        homeList.add(new productItem("Blouse 4","400",R.drawable.blouse4,1));
        homeList.add(new productItem("Blouse 5","360",R.drawable.blouse5,1));
        homeList.add(new productItem("Blouse 6","350",R.drawable.blouse6,1));
        homeList.add(new productItem("Blouse 7","360",R.drawable.blouse7,1));
        homeList.add(new productItem("Blouse 8","380",R.drawable.blouse8,1));
        //--------------------Pantalons-----------------
        homeList.add(new productItem("Pantaloon 1","320",R.drawable.pantalon1,1));
        homeList.add(new productItem("Pantaloon 2","250",R.drawable.pantalon2,1));
        homeList.add(new productItem("Pantaloon 3","350",R.drawable.pantalon3,1));
        homeList.add(new productItem("Pantaloon 4","280",R.drawable.pantalon4,1));
        homeList.add(new productItem("Pantaloon 5","320",R.drawable.pantalon5,1));
        homeList.add(new productItem("Pantaloon 6","300",R.drawable.pantalon6,1));
        homeList.add(new productItem("Pantaloon 7","220",R.drawable.pantalon7,1));
        homeList.add(new productItem("Pantaloon 8","280",R.drawable.pantalon8,1));
        homeList.add(new productItem("Pantaloon 9","250",R.drawable.pantalon9,1));
        homeList.add(new productItem("Pantaloon 10","320",R.drawable.pantalon10,1));
        //--------------------Skirts----------------------------
        homeList.add(new productItem("Skirt 1","250",R.drawable.skirt1,1));
        homeList.add(new productItem("Skirt 2","300",R.drawable.skirt2,1));
        homeList.add(new productItem("Skirt 3","280",R.drawable.skirt3,1));
        homeList.add(new productItem("Skirt 4","220",R.drawable.skirt4,1));
        homeList.add(new productItem("Skirt 5","220",R.drawable.skirt5,1));
        homeList.add(new productItem("Skirt 6","180",R.drawable.skirt6,1));
        homeList.add(new productItem("Skirt 7","160",R.drawable.skirt7,1));
        homeList.add(new productItem("Skirt 8","180",R.drawable.skirt8,1));
        //-----------------Dresses---------------------------
        homeList.add(new productItem("Dress 1","200",R.drawable.dress1,1));
        homeList.add(new productItem("Dress 2","320",R.drawable.dress2,1));
        homeList.add(new productItem("Dress 3","260",R.drawable.dress3,1));
        homeList.add(new productItem("Dress 4","280",R.drawable.dress4,1));
        homeList.add(new productItem("Dress 5","340",R.drawable.dress5,1));
        homeList.add(new productItem("Dress 6","220",R.drawable.dress6,1));
        homeList.add(new productItem("Dress 7","320",R.drawable.dress7,1));
        homeList.add(new productItem("Dress 8","360",R.drawable.dress8,1));
        homeList.add(new productItem("Dress 9","380",R.drawable.dress9,1));
        homeList.add(new productItem("Dress 10","300",R.drawable.dress10,1));
        Collections.shuffle(homeList);
        setItemRecyclerView(homeList);
    }
}