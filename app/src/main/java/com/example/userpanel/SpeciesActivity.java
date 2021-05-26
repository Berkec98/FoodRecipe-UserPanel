package com.example.userpanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.userpanel.Interface.ItemClickListener;
import com.example.userpanel.Model.Species;
import com.example.userpanel.Model.category;
import com.example.userpanel.ViewHolder.SpeciesViewHolder;
import com.example.userpanel.ViewHolder.categoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class SpeciesActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference sway;
    FirebaseStorage storage;
    StorageReference pway;
    FirebaseRecyclerAdapter<Species, SpeciesViewHolder> adapter;

    String categoryID="";

    Species newSpecies;

    RecyclerView recyclerView_species;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Species, SpeciesViewHolder> searchadapter;
    List<String> suggestionlist = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species);

        database= FirebaseDatabase.getInstance();
        sway=database.getReference("Species");
        storage=FirebaseStorage.getInstance();
        pway=storage.getReference();


        recyclerView_species=findViewById(R.id.species_recycle);
        recyclerView_species.setHasFixedSize(true);
        layoutManager=new GridLayoutManager(this,2);
        recyclerView_species.setLayoutManager(layoutManager);

        if (getIntent() != null){
            categoryID=getIntent().getStringExtra("CategoryID");
        }
        if (!categoryID.isEmpty()){
            speciesdownload(categoryID);
        }

        materialSearchBar=findViewById(R.id.searchspecies);
        materialSearchBar.setHint("Search species");
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
                    recyclerView_species.setAdapter(adapter);
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

        Query nameList = sway.orderByChild("name").equalTo(text.toString());
        FirebaseRecyclerOptions<Species> speciesoptions = new FirebaseRecyclerOptions.Builder<Species>()
                .setQuery(nameList,Species.class).build();

        searchadapter = new FirebaseRecyclerAdapter<Species, SpeciesViewHolder>(speciesoptions) {
            @Override
            protected void onBindViewHolder(@NonNull SpeciesViewHolder speciesViewHolder, int i, @NonNull Species species) {

                speciesViewHolder.txtspeciesname.setText(species.getName());
                Picasso.with(getBaseContext()).load(species.getFoto()).into(speciesViewHolder.imageView);

                Species local = species;

                speciesViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        Intent species = new Intent(SpeciesActivity.this,FoodActivity.class);
                        species.putExtra("SpeciesID",searchadapter.getRef(position).getKey());

                        startActivity(species);

                    }
                });

            }

            @NonNull
            @Override
            public SpeciesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_species_line_item,parent,false);
                return new SpeciesViewHolder(itemView);
            }
        };

        searchadapter.startListening();
        recyclerView_species.setAdapter(searchadapter);

    }

    private void suggestiondownload() {

        sway.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                    Species item= postSnapshot.getValue(Species.class);
                    suggestionlist.add(item.getName());
                }

                materialSearchBar.setLastSuggestions(suggestionlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        
    }

    private void speciesdownload(String categoryID) {
        Query filter= sway.orderByChild("categoryID").equalTo(categoryID);
        FirebaseRecyclerOptions<Species> options = new FirebaseRecyclerOptions.Builder<Species>()
                .setQuery(filter,Species.class).build();
        adapter = new FirebaseRecyclerAdapter<Species, SpeciesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SpeciesViewHolder holder, int i, @NonNull Species model) {
                holder.txtspeciesname.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getFoto()).into(holder.imageView);

                Species clicked = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        // What will it do when clicked on the line
                        Intent species = new Intent(SpeciesActivity.this,FoodActivity.class);
                        species.putExtra("SpeciesID",adapter.getRef(position).getKey());

                        startActivity(species);

                    }
                });
            }

            @NonNull
            @Override
            public SpeciesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_species_line_item,parent,false);
                return new SpeciesViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_species.setAdapter(adapter);
    }
}