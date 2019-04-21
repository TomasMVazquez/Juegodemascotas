package com.applications.toms.juegodemascotas.view;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.toms.juegodemascotas.R;
import com.applications.toms.juegodemascotas.controller.PetsFromOwnerController;
import com.applications.toms.juegodemascotas.model.Duenio;
import com.applications.toms.juegodemascotas.model.Mascota;
import com.applications.toms.juegodemascotas.util.ResultListener;
import com.applications.toms.juegodemascotas.view.adapter.CirculePetsAdapter;
import com.applications.toms.juegodemascotas.view.adapter.MyPetsAdapter;
import com.applications.toms.juegodemascotas.view.fragment.UpdateProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.EasyImage;

public class ProfileActivity extends AppCompatActivity implements UpdateProfileFragment.OnFragmentNotify, CirculePetsAdapter.AdapterInterfaceCircule {

    public static final String KEY_TYPE = "type";
    public static final String KEY_USER_ID = "user_id";
    public static final int KEY_CAMERA = 301;

    //Atributos
    private static CirculePetsAdapter circulePetsAdapter;

    private FirebaseStorage mStorage;
    private static FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvDir;
    private TextView tvAboutProfile;
    private TextView tvMyPetsOwner;
    private RecyclerView rvMyPetsOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        ivProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        tvDir = findViewById(R.id.tvDir);
        tvAboutProfile = findViewById(R.id.tvAboutProfile);
        tvMyPetsOwner = findViewById(R.id.tvMyPetsOwner);
        rvMyPetsOwner = findViewById(R.id.rvMyPetsOwner);

        FloatingActionButton fabImageProfile = findViewById(R.id.fabImageProfile);
        FloatingActionButton fabEditProfile = findViewById(R.id.fabEditProfile);

        //intent
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        String type = bundle.getString(KEY_TYPE);
        String userId = bundle.getString(KEY_USER_ID);

        //Adapter
        circulePetsAdapter = new CirculePetsAdapter(new ArrayList<Mascota>(),this,this);

        if (type.equals("1")){
            tvMyPetsOwner.setText(getResources().getString(R.string.my_pets));
            //TODO que pasa si quiere ver el profile de otro usuario
            fetchOwnerProfile(currentUser);
        }else {
            tvMyPetsOwner.setText(getResources().getString(R.string.my_owner));
        }

        //Recycler View
        rvMyPetsOwner.hasFixedSize();
        //LayoutManager
        LinearLayoutManager llm = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvMyPetsOwner.setLayoutManager(llm);
        //adaptador
        rvMyPetsOwner.setAdapter(circulePetsAdapter);

        //Boton de foto para cambiarla
        fabImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyImage.openChooserWithGallery(ProfileActivity.this,getResources().getString(R.string.take_profile_picture),KEY_CAMERA);
            }
        });

        //Boton to edit profile
        fabEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateProfileFragment updateProfileFragment = new UpdateProfileFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.containerProfile,updateProfileFragment);
                fragmentTransaction.commit();
            }
        });

    }

    public void saveAndCompleteProfileUpdates(String name, String dir, String birth, String sex, String about){
        tvName.setText(name);
        tvDir.setText(dir);
        tvAboutProfile.setText(about);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdates);

        MainActivity.updateProfile( name,  dir,  birth,  sex,  about);
    }

    public void fetchOwnerProfile(FirebaseUser user){
        if (user.getPhotoUrl()!=null) {
            String photo = user.getPhotoUrl().toString() + "?height=500";
            Glide.with(this).load(photo).into(ivProfile);
        }else {

        }
        tvName.setText(user.getDisplayName());
        checkDataBaseInfo(currentUser.getUid());

        PetsFromOwnerController petsFromOwnerController = new PetsFromOwnerController();
        petsFromOwnerController.giveOwnerPets(user.getUid(), this, new ResultListener<List<Mascota>>() {
            @Override
            public void finish(List<Mascota> resultado) {
                circulePetsAdapter.setMascotaList(resultado);
            }
        });
    }

    //CheckDataBaseInfo
    public void checkDataBaseInfo(final String userID){

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    Duenio duenio = childSnapShot.getValue(Duenio.class);
                    if (duenio.getDireccion()!=null){
                        tvDir.setText(duenio.getDireccion());
                    }else {
                        tvDir.setText(getResources().getString(R.string.address_profile));
                    }
                    if (duenio.getInfoDuenio()!=null){
                        tvAboutProfile.setText(duenio.getInfoDuenio());
                    }else {
                        tvAboutProfile.setText(getResources().getString(R.string.about_profile));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, ProfileActivity.this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource imageSource, int i) {

            }

            @Override
            public void onImagesPicked(@NonNull List<File> list, EasyImage.ImageSource imageSource, int i) {
                StorageReference raiz = mStorage.getReference();
                if (list.size() > 0) {
                    File file = list.get(0);
                    final Uri uri = Uri.fromFile(file);
                    final Uri uriTemp = Uri.fromFile(new File(uri.getPath()));

                    switch (i) {
                        case KEY_CAMERA:
                            final StorageReference nuevaFoto = raiz.child(currentUser.getUid()).child(uriTemp.getLastPathSegment());

                            Toast.makeText(ProfileActivity.this, "Espere mientras cargamos su foto", Toast.LENGTH_SHORT).show();

                            final UploadTask uploadTask = nuevaFoto.putFile(uriTemp);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Poner nueva foto
                                    Glide.with(ProfileActivity.this).load(uri).into(ivProfile);
                                    //Actualizar foto de Firebase User
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build();

                                    currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Cambiamos un metodo local por uno en el main
                                                MainActivity.updateProfilePicture(currentUser.getUid(),nuevaFoto.getName());
                                            }
                                        }
                                    });
                                }
                            });
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource imageSource, int i) {

            }
        });

    }

    @Override
    public void goToProfile(String idPet) {
        //TODO Go to Profile of my pets
        Toast.makeText(this, "En construccion", Toast.LENGTH_SHORT).show();
    }
}
