package com.example.userpanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.userpanel.Model.Food;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import info.hoang8f.widget.FButton;

public class FoodDetailActivity extends AppCompatActivity {

    TextView foodrecipes,foodmaterials,food_name,video_id;
    ImageView food_detail;
    FButton food_link;
    CollapsingToolbarLayout collapsingToolbarLayout;

    String FoodID="";

    FirebaseDatabase database;
    DatabaseReference foodway;

    Food clickfood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        video_id= findViewById(R.id.txtvideo_id);
        food_name= findViewById(R.id.txtfoodname);
        foodrecipes= findViewById(R.id.txtfoodrecipes);
        foodmaterials= findViewById(R.id.txtfoodmaterials);
        food_detail=findViewById(R.id.foodname_detail);
        food_link=findViewById(R.id.btn_link);

        food_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //link -> video
                Intent VideoID = new Intent(FoodDetailActivity.this,LinkActivity.class);
                VideoID.putExtra("Link",video_id.getText().toString());

                startActivity(VideoID);


            }
        });



        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        database= FirebaseDatabase.getInstance();
        foodway=database.getReference("Food");

        if (getIntent() != null){
            FoodID=getIntent().getStringExtra("FoodID");
        }
        if (!FoodID.isEmpty()){
            detaildownload(FoodID);
        }

    }

    private void detaildownload(String foodID) {

        foodway.child(foodID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clickfood= snapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(clickfood.getFoto()).into(food_detail);
                collapsingToolbarLayout.setTitle(clickfood.getFoodname());
                foodmaterials.setText(clickfood.getFoodmaterials());
                foodrecipes.setText(clickfood.getFoodrecipes());
                food_name.setText(clickfood.getFoodname());
                video_id.setText(clickfood.getFoodrecipeslink());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}