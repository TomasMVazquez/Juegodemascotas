package com.applications.toms.juegodemascotas.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applications.toms.juegodemascotas.R;
import com.applications.toms.juegodemascotas.controller.PetController;
import com.applications.toms.juegodemascotas.dao.DaoPet;
import com.applications.toms.juegodemascotas.model.Pet;
import com.applications.toms.juegodemascotas.util.ResultListener;
import com.applications.toms.juegodemascotas.view.adapter.PetsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements PetsAdapter.PetsAdapterInterface {

    private static final String TAG = "SearchActivity";

    private static FirebaseUser currentUser;
    private static PetsAdapter petsAdapter;
    private PetController petController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView searchView = findViewById(R.id.searchView);

        Toolbar myToolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        petController = new PetController();

        petsAdapter = new PetsAdapter(new ArrayList<>(),this,this);

        //Recycler View
        RecyclerView recyclerViewPets = findViewById(R.id.recyclerPets);
        recyclerViewPets.hasFixedSize();
        //LayoutManager
        LinearLayoutManager llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerViewPets.setLayoutManager(llm);
        //adaptador
        recyclerViewPets.setAdapter(petsAdapter);

        if (currentUser != null){
            //Traigo Mascotas Owner
            petController.givePetList(this,result -> petsAdapter.setPetList(result));
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchResult(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: search: " + newText);
                searchResult(newText);
                return false;
            }
        });

    }

    private void searchResult(String searchText){
        Log.d(TAG, "searchResult: search: " + searchText);
        petController.giveResultSearch(searchText, this, result -> {
            petsAdapter.setPetList(result);
            Log.d(TAG, "searchResult: result: " + result);
            Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void goToProfileFromPets(String idOwner, Pet pet) {

    }
}