package com.applications.toms.juegodemascotas.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.applications.toms.juegodemascotas.R;
import com.applications.toms.juegodemascotas.controller.PetsFromOwnerController;
import com.applications.toms.juegodemascotas.model.Duenio;
import com.applications.toms.juegodemascotas.model.Mascota;
import com.applications.toms.juegodemascotas.model.MascotaConteiner;
import com.applications.toms.juegodemascotas.util.ResultListener;
import com.applications.toms.juegodemascotas.view.adapter.MyPetsAdapter;
import com.applications.toms.juegodemascotas.view.fragment.AddPetFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MyPetsActivity extends AppCompatActivity implements MyPetsAdapter.AdapterInterface {

    public static final String KEY_DUENIO_ID = "duenio";

    private FirebaseDatabase mDatabase;
    private static DatabaseReference mReference;

    private static FirebaseFirestore db;
    private static String userFirestore;

    private static FirebaseUser currentUser;
    private static Context context;

    //Atributos
    private static MyPetsAdapter myPetsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pets);

        context = getApplicationContext();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        db = FirebaseFirestore.getInstance();
        userFirestore = getResources().getString(R.string.collection_users);

        FloatingActionButton fabAddPet = findViewById(R.id.fabAddPet);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String idDuenio = bundle.getString(KEY_DUENIO_ID);

        myPetsAdapter = new MyPetsAdapter(new ArrayList<Mascota>(),this,this);

        //TODO pets from store
        //Traigo Mascotas Duenio
        final CollectionReference userRefMasc = db.collection(userFirestore)
                .document(currentUser.getUid()).collection("misMascotas");

        userRefMasc.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                List<Mascota> misMascotas = new ArrayList<>();
                misMascotas.addAll(queryDocumentSnapshots.toObjects(Mascota.class));
                myPetsAdapter.setMascotaList(misMascotas);
            }
        });


        //Recycler View
        RecyclerView recyclerViewPets = findViewById(R.id.recyclerMyPets);
        recyclerViewPets.hasFixedSize();
        //LayoutManager
        LinearLayoutManager llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerViewPets.setLayoutManager(llm);
        //adaptador
        recyclerViewPets.setAdapter(myPetsAdapter);

        fabAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPetFragment addPetFragment = new AddPetFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.containerPets,addPetFragment);
                fragmentTransaction.commit();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        misMascotas.clear();
    }

    @Override
    public void goToProfile(String idOwner, Mascota mascotaProfile) {
        Intent intent = new Intent(MyPetsActivity.this,ProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ProfileActivity.KEY_TYPE,"2");
        bundle.putString(ProfileActivity.KEY_USER_ID,idOwner);
        bundle.putString(ProfileActivity.KEY_PET_ID,mascotaProfile.getIdPet());
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public static void addPetToDataBase(final String name, final String raza, final String size, final String birth, final String sex, final String photo, final String info){
        final CollectionReference userRefMasc = db.collection(userFirestore)
                .document(currentUser.getUid()).collection("misMascotas");
        DocumentReference petsRef = db.collection(context.getResources().getString(R.string.collection_pets))
                .document(currentUser.getUid());

        final String idPet = userRefMasc.document().getId();
        Mascota newMascota = new Mascota(idPet,name,raza,size,sex,birth,photo,info,currentUser.getUid());

        userRefMasc.document(idPet).set(newMascota)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //TODO
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO
                    }
                });

        petsRef.set(newMascota).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //TODO
                }else{
                    //TODO
                }
            }
        });
    }

}
