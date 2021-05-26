package com.example.userpanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
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
import com.example.userpanel.Model.category;
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

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference cway;
    FirebaseStorage storage;
    StorageReference pway;
    FirebaseRecyclerAdapter<category, categoryViewHolder> adapter;

    RecyclerView recyclerView_category;
    RecyclerView.LayoutManager layoutManager;

    category new_category;

    FirebaseRecyclerAdapter<category, categoryViewHolder> searchadapter;
    List<String> suggestionlist = new ArrayList<>();
    MaterialSearchBar materialSearchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database= FirebaseDatabase.getInstance();
        cway=database.getReference("category");
        storage=FirebaseStorage.getInstance();
        pway=storage.getReference();

        recyclerView_category=findViewById(R.id.category_recycle);
        recyclerView_category.setHasFixedSize(true);
        layoutManager=new GridLayoutManager(this,2);
        recyclerView_category.setLayoutManager(layoutManager);

        categorydownload();

        materialSearchBar=findViewById(R.id.searchcategory);
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
                    recyclerView_category.setAdapter(adapter);
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

        Query nameList = cway.orderByChild("name").equalTo(text.toString());
        FirebaseRecyclerOptions<category> categoryoptions = new FirebaseRecyclerOptions.Builder<category>()
                .setQuery(nameList,category.class).build();

        searchadapter = new FirebaseRecyclerAdapter<category, categoryViewHolder>(categoryoptions) {
            @Override
            protected void onBindViewHolder(@NonNull categoryViewHolder categoryViewHolder, int i, @NonNull category category) {

                categoryViewHolder.txtcategoryname.setText(category.getName());
                Picasso.with(getBaseContext()).load(category.getFoto()).into(categoryViewHolder.imageView);

                category local = category;

                categoryViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {

                        Intent species = new Intent(MainActivity.this,SpeciesActivity.class);
                        species.putExtra("CategoryID",searchadapter.getRef(position).getKey());

                        startActivity(species);

                    }
                });

            }

            @NonNull
            @Override
            public categoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_category_line_item,parent,false);
                return new categoryViewHolder(itemView);
            }
        };

        searchadapter.startListening();
        recyclerView_category.setAdapter(searchadapter);

    }

    private void suggestiondownload() {
        cway.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                   category item= postSnapshot.getValue(category.class);
                    suggestionlist.add(item.getName());
                }

                materialSearchBar.setLastSuggestions(suggestionlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categorydownload() {

        FirebaseRecyclerOptions<category> options = new FirebaseRecyclerOptions.Builder<category>()
                .setQuery(cway,category.class).build();
        adapter = new FirebaseRecyclerAdapter<category, categoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull categoryViewHolder categoryViewHolder, int i, @NonNull category category) {
                categoryViewHolder.txtcategoryname.setText(category.getName());
                Picasso.with(getBaseContext()).load(category.getFoto()).into(categoryViewHolder.imageView);

                category clicked = category;
                categoryViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        // What will it do when clicked on the line

                        Intent species = new Intent(MainActivity.this,SpeciesActivity.class);
                        species.putExtra("CategoryID",adapter.getRef(position).getKey());

                        startActivity(species);

                    }
                });


            }

            @NonNull
            @Override
            public categoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_category_line_item,parent,false);
                return new categoryViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_category.setAdapter(adapter);


    }
}