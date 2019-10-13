package com.applications.toms.juegodemascotas.dao;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.applications.toms.juegodemascotas.R;
import com.applications.toms.juegodemascotas.model.Owner;
import com.applications.toms.juegodemascotas.util.ResultListener;
import com.applications.toms.juegodemascotas.view.ProfileActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DaoOwner {

    private List<Owner> ownerList = new ArrayList<>();
    private FirebaseFirestore mDatabase;

    //DAO to look for owners and owners data
    public DaoOwner() {
        this.mDatabase = mDatabase;
    }

    //return all owners from DataBase
    public void fetchOwnerList(Context context, ResultListener<List<Owner>> listResultListener){
        //DataBase Collection of owners/users
        CollectionReference ownerRef = mDatabase.collection(context.getString(R.string.collection_users));
        //extract list of owners
        ownerRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            ownerList.addAll(queryDocumentSnapshots.toObjects(Owner.class));
            listResultListener.finish(ownerList);
        });
    }

    //return one owner based on their user id
    public void fetchOwner(String ownerId,Context context, ResultListener<Owner> ownerResultListener){
        //extract single owner data
        DocumentReference ownerRef = mDatabase.collection(context.getString(R.string.collection_users))
                .document(ownerId);

        ownerRef.get().addOnSuccessListener(documentSnapshot -> {
            Owner owner = documentSnapshot.toObject(Owner.class);
            ownerResultListener.finish(owner);
        });
    }

    //return avatars owner from storage
    public void fetchOwnerAvatar(String userId, String avatar, Context context, ResultListener<Uri> uriResultListener){
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = mStorage.getReference().child(userId).child(avatar);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> uriResultListener.finish(uri));
    }
}
