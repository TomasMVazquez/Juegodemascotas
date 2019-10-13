package com.applications.toms.juegodemascotas.view.fragment;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.applications.toms.juegodemascotas.R;
import com.applications.toms.juegodemascotas.view.MyPetsActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import pl.aprilapps.easyphotopicker.EasyImage;

/*
 * A simple {@link Fragment} subclass.
 */
public class AddPetFragment extends Fragment {

    public static final int KEY_CAMERA_PET = 301;

    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;

    private String size="";
    private String sex="";
    private ImageView ivAddPetPhoto;
    private String photo="";
    private EditText etAddPetName;
    private static EditText etAddPetBirth;
    private EditText etAddPetRaza;
    private EditText etAddPetInfo;
    private Boolean uploadingPhoto;

    public AddPetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_pet, container, false);
        //instances from DataBase
        mStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //view from layout
        etAddPetName = view.findViewById(R.id.etAddPetName);
        etAddPetBirth = view.findViewById(R.id.etAddPetBirth);
        etAddPetRaza = view.findViewById(R.id.etAddPetRaza);
        etAddPetInfo = view.findViewById(R.id.etAddPetInfo);

        //Birth date picker
        etAddPetBirth.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                showTruitonDatePickerDialog(v);
            }
        });

        //Spinner to select the size of the Dog
        Spinner spinnerAddPetSize = view.findViewById(R.id.spinnerAddPetSize);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getResources().getString(R.string.add_pet_size));
        spinnerArray.add(getResources().getString(R.string.spinner_small));
        spinnerArray.add(getResources().getString(R.string.spinner_medium));
        spinnerArray.add(getResources().getString(R.string.spinner_large));
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item,spinnerArray);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddPetSize.setAdapter(adapterSpinner);
        spinnerAddPetSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                size =  parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Genre Radio Btn
        final RadioButton rbAddPetSexMasc = view.findViewById(R.id.rbAddPetSexMasc);
        final RadioButton rbAddPetSexFem = view.findViewById(R.id.rbAddPetSexFem);

        RadioGroup radioGroup = view.findViewById(R.id.rgAddPetSex);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (rbAddPetSexMasc.isChecked()){
                sex = Objects.requireNonNull(getContext()).getResources().getString(R.string.add_pet_sex_m);
            }
            if (rbAddPetSexFem.isChecked()){
                sex = Objects.requireNonNull(getContext()).getResources().getString(R.string.add_pet_sex_h);
            }
        });

        //Pet Avatar
        ivAddPetPhoto = view.findViewById(R.id.ivAddPetPhoto);

        FloatingActionButton fabAddPetPhoto = view.findViewById(R.id.fabAddPetPhoto);
        ivAddPetPhoto.setOnClickListener(v -> EasyImage.openChooserWithGallery(AddPetFragment.this,"Elige la mejor foto", KEY_CAMERA_PET));

        //Btn Add pet
        Button btnAddPet = view.findViewById(R.id.btnAddPet);
        btnAddPet.setOnClickListener(v -> {
            if (checkCompleteData()){
                MyPetsActivity.addPetToDataBase(etAddPetName.getText().toString(),etAddPetRaza.getText().toString(),size,etAddPetBirth.getText().toString(),sex,photo,etAddPetInfo.getText().toString());
                getActivity().getSupportFragmentManager().beginTransaction().remove(AddPetFragment.this).commit();
            } else {
                Toast.makeText(getActivity(), "Faltan completar datos", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    //Verification that all data is complete
    public Boolean checkCompleteData(){

        if (etAddPetName.getText().equals("")){
            return false;
        }
        if (etAddPetBirth.getText().equals("")){
            return false;
        }
        if (etAddPetRaza.getText().equals("")){
            return false;
        }
        if (etAddPetInfo.getText().equals("")){
            return false;
        }
        if (sex.equals("")){
            return false;
        }
        if (size.equals("")){
            return false;
        }
        if (photo.equals("")){
            return false;
        }
        return true;
    }

    //Avatar image Activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final StorageReference raiz = mStorage.getReference();

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource imageSource, int i) {

            }

            @Override
            public void onImagesPicked(@NonNull List<File> list, EasyImage.ImageSource imageSource, int i) {
                if (list.size() > 0) {
                    File file = list.get(0);
                    final Uri uri = Uri.fromFile(file);
                    final Uri uriTemp = Uri.fromFile(new File(uri.getPath()));

                    switch (i) {
                        case KEY_CAMERA_PET:
                            Glide.with(AddPetFragment.this).load(uri).into(ivAddPetPhoto);
                            photo = uriTemp.getLastPathSegment();

                            final StorageReference oneFoto = raiz.child(mAuth.getCurrentUser().getUid()).child(uriTemp.getLastPathSegment());
                            UploadTask uploadTask = oneFoto.putFile(uriTemp);
                            uploadTask
                                    .addOnProgressListener(taskSnapshot -> uploadingPhoto = false)
                                    .addOnSuccessListener(taskSnapshot -> uploadingPhoto = true);
                            break;
                    }
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource imageSource, int i) {

            }
        });

    }

    //Date picker
    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    //Date Picker Comands
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String dateChoosen=(dayOfMonth + "/" + (month + 1) + "/" + year);
            etAddPetBirth.setText(dateChoosen);
        }
    }

}
