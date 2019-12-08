package com.applications.toms.juegodemascotas.view.menu_fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applications.toms.juegodemascotas.R;
import com.applications.toms.juegodemascotas.controller.PetController;
import com.applications.toms.juegodemascotas.model.Pet;
import com.applications.toms.juegodemascotas.util.FragmentTitles;
import com.applications.toms.juegodemascotas.view.adapter.PetsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements PetsAdapter.PetsAdapterInterface, FragmentTitles {

    public static final String TAG = "SearchFragment";

    private static PetsAdapter petsAdapter;
    private PetController petController;
    private Context context;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private SearchInterface searchInterface;

    public SearchFragment() {
        // Required empty public constructor
    }

    public void setSearchInterface(SearchInterface searchInterface) {
        this.searchInterface = searchInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        context = getContext();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setQueryHint(getString(R.string.search));

        //Get Firebase User instance
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Get Pet controller
        petController = new PetController();

        //Get Pet Adapter for recycler
        petsAdapter = new PetsAdapter(new ArrayList<>(),context,this);

        //Recycler View
        RecyclerView recyclerViewPets = view.findViewById(R.id.recyclerPets);
        recyclerViewPets.hasFixedSize();
        //LayoutManager
        LinearLayoutManager llm = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerViewPets.setLayoutManager(llm);
        //adaptador
        recyclerViewPets.setAdapter(petsAdapter);

        //If the user is logged then get all pets from DataBase
        if (currentUser != null){
            petController.givePetList(context,result -> petsAdapter.setPetList(result));
        }

        //For search Logic while writing or when enter
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchResult(query.toUpperCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: search: " + newText);
                searchResult(newText.toUpperCase());
                return false;
            }
        });

        return view;
    }

    @Override
    public int getFragmentTitle() {
        return R.string.search;
    }

    public interface SearchInterface{
        void chatFromSearch(String userToChat);
    }

    //Get results from search
    private void searchResult(String searchText){
        Log.d(TAG, "searchResult: search: " + searchText);
        petController.giveResultSearch(searchText, context, result -> {
            petsAdapter.setPetList(result);
            Log.d(TAG, "searchResult: result: " + result);
        });
    }

    //Go to a profile when clicking the card of a pet.
    @Override
    public void goToProfileFromPets(String idOwner, Pet pet) {
        //TODO CHANGE TO FRAGMENT 2
//        Intent intent = new Intent(SearchActivity.this,ProfileActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString(ProfileActivity.KEY_TYPE,"2");
//        bundle.putString(ProfileActivity.KEY_USER_ID,idOwner);
//        bundle.putString(ProfileActivity.KEY_PET_ID, pet.getIdPet());
//        intent.putExtras(bundle);
//        startActivity(intent);
    }

    //Go to a chat when clicking the chat icon.
    @Override
    public void goToChat(String userToChat) {
        searchInterface.chatFromSearch(userToChat);
    }

    //Add it as a friend when clicking the heart icon.
    @Override
    public void addFriend(Pet pet) {
        Toast.makeText(context, "Agregando a " + pet.getNombre() + " a mi lista de amigos", Toast.LENGTH_SHORT).show();

        //Create on the current user a document with firend list
        CollectionReference myFriendCol = db.collection(getString(R.string.collection_users))
                .document(currentUser.getUid()).collection(getString(R.string.collection_my_friends));

        myFriendCol.document(pet.getIdPet()).set(pet).addOnSuccessListener(aVoid -> {
            //TODO si ya existia te lo reemplaza... Deberia no realizar todo esto o si?
            Toast.makeText(context, "Agregado!", Toast.LENGTH_SHORT).show();
        });
    }
}