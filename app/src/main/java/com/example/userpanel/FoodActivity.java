package com.example.userpanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.userpanel.Interface.ItemClickListener;
import com.example.userpanel.Model.Food;
import com.example.userpanel.Model.category;
import com.example.userpanel.ViewHolder.FoodViewHolder;
import com.example.userpanel.ViewHolder.categoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.widget.FButton;

public class FoodActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference fway;
    FirebaseStorage storage;
    StorageReference pway;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    String SpeciesID="";
    Food new_food;

    RecyclerView recyclerView_food;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchadapter;
    List<String> suggestionlist = new ArrayList<>();
    MaterialSearchBar materialSearchBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        database= FirebaseDatabase.getInstance();
        fway=database.getReference("Food");
        storage=FirebaseStorage.getInstance();
        pway=storage.getReference();

        recyclerView_food=findViewById(R.id.recycle_food);
        recyclerView_food.setHasFixedSize(true);
        layoutManager=new GridLayoutManager(this,2);
        recyclerView_food.setLayoutManager(layoutManager);

        if (getIntent() != null){
            SpeciesID=getIntent().getStringExtra("SpeciesID");
        }
        if (!SpeciesID.isEmpty()){
            fooddownload(SpeciesID);
        }

        materialSearchBar=findViewById(R.id.searchfood);
        materialSearchBar.setHint("Search category");
        materialSearchBar.setCardViewElevation(10);

        suggestiondownload();

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                List<String> suggestion = new ArrayList<String>();
                for(String Search:suggestionlist){
                    if (Search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggestion.add(Search);
                }

                materialSearchBar.setLastSuggestions(suggestion);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled){
                    recyclerView_food.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                searchstart(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void searchstart(CharSequence text) {

        Query nameList = fway.orderByChild("foodname").equalTo(text.toString());
        FirebaseRecyclerOptions<Food> foodoptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(nameList,Food.class).build();

        searchadapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodoptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i, @NonNull Food food) {

                foodViewHolder.txtfoodname.setText(food.getFoodname());
                Picasso.with(getBaseContext()).load(food.getFoto()).into(foodViewHolder.imageView);

                Food local = food;

                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {

                        Intent food = new Intent(FoodActivity.this,FoodDetailActivity.class);
                        food.putExtra("FoodID",searchadapter.getRef(position).getKey());

                        startActivity(food);

                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_food_line_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };

        searchadapter.startListening();
        recyclerView_food.setAdapter(searchadapter);

    }

    private void suggestiondownload() {

        fway.orderByChild("foodname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                    Food item= postSnapshot.getValue(Food.class);
                    suggestionlist.add(item.getFoodname());
                }

                materialSearchBar.setLastSuggestions(suggestionlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fooddownload(String speciesID) {

        Query filter= fway.orderByChild("speciesID").equalTo(speciesID);
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(filter,Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int i, @NonNull Food model) {
                holder.txtfoodname.setText(model.getFoodname());
                Picasso.with(getBaseContext()).load(model.getFoto()).into(holder.imageView);

                Food clicked = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        // What will it do when clicked on the line
                        Intent food = new Intent(FoodActivity.this,FoodDetailActivity.class);
                        food.putExtra("FoodID",adapter.getRef(position).getKey());

                        startActivity(food);

                    }
                });


            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_food_line_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_food.setAdapter(adapter);


    }
}