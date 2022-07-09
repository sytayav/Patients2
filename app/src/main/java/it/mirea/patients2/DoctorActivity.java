package it.mirea.patients2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DoctorActivity extends AppCompatActivity {

    RelativeLayout root;
    TextView textEmailDoc, textNameDoc, textPhoneDoc;
    Button btnEdit, btnDelete;

    ///бд с юзерами
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    //бд с пациентами
    FirebaseFirestore dbFirestore;
    CollectionReference allPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        //получение данных из прошлой активити
        Bundle userID = getIntent().getExtras();
        String uID = userID.getString("userID");

        root = findViewById(R.id.doctor_element);
        textEmailDoc = findViewById(R.id.textEmailDoc);
        textNameDoc = findViewById(R.id.textNameDoc);
        textPhoneDoc = findViewById(R.id.textPhoneDoc);
        btnEdit = findViewById(R.id.edit2);
        btnDelete = findViewById(R.id.delete2);

        //с юзерами
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        //с пациентами
        dbFirestore = FirebaseFirestore.getInstance();
        allPatients = dbFirestore.collection("AllPatients");


        //кнопочки
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditWindow(uID);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteWindow(uID);
            }
        });


        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.child(uID).child("email").getValue(String.class);
                String name = snapshot.child(uID).child("name").getValue(String.class);
                String phone = snapshot.child(uID).child("phone").getValue(String.class);
                textEmailDoc.setText(email);
                textNameDoc.setText(name);
                textPhoneDoc.setText(phone);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showDeleteWindow(String uID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);

        LayoutInflater inflater = LayoutInflater.from(this);
        View deleteDoc_window = inflater.inflate(R.layout.delete_doc_window, null);
        dialog.setView(deleteDoc_window);

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                users.child(uID).removeValue();
                Toast.makeText(DoctorActivity.this, "Successful", Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(DoctorActivity.this, MainActivity.class));
            }
        });

        dialog.show();
    }

    private void showEditWindow(String uID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);
        dialog.setTitle("Edit");

        LayoutInflater inflater = LayoutInflater.from(this);
        View edit_window = inflater.inflate(R.layout.edit_doc_window, null);
        dialog.setView(edit_window);

        MaterialEditText nameDocField = edit_window.findViewById(R.id.nameDocField);
        MaterialEditText phoneDocField = edit_window.findViewById(R.id.phoneDocField);

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child(uID).child("name").getValue(String.class);
                String phone = snapshot.child(uID).child("phone").getValue(String.class);

                nameDocField.setText(name);
                phoneDocField.setText(phone);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(TextUtils.isEmpty(nameDocField.getText().toString())){
                    Snackbar.make(root, "Enter name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(phoneDocField.getText().toString())){
                    Snackbar.make(root, "Enter phone", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                users.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child(uID).child("email").getValue(String.class);
                        String pass = snapshot.child(uID).child("pass").getValue(String.class);
                        String level = snapshot.child(uID).child("level").getValue(String.class);

                        String sname = nameDocField.getText().toString();
                        String sphone = phoneDocField.getText().toString();
                        Map<String,Object> doctor = new HashMap<>();
                        doctor.put("email", email);
                        doctor.put("pass", pass);
                        doctor.put("name", sname);
                        doctor.put("phone", sphone);
                        doctor.put("level", level);

                        users.child(uID).setValue(doctor).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(DoctorActivity.this, "Successful", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DoctorActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        dialog.show();

    }
}
