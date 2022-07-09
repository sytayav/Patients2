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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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


public class PatientActivity extends AppCompatActivity {

    RelativeLayout root;
    TextView tName, tAge;
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
        setContentView(R.layout.activity_patient);

        //получение данных из прошлой активити
        Bundle userID = getIntent().getExtras();
        String uID = userID.getString("userID");

        root = findViewById(R.id.patient_element);
        tName = findViewById(R.id.textName);
        tAge = findViewById(R.id.textAge);
        btnEdit = findViewById(R.id.edit);
        btnDelete = findViewById(R.id.delete);

        //с юзерами
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        //с пациентами
        dbFirestore = FirebaseFirestore.getInstance();
        allPatients = dbFirestore.collection("AllPatients");



        //получение данных из прошлой активити
        Bundle array = getIntent().getExtras();
        Bundle pos = getIntent().getExtras();
        ArrayList<String> arrayListID = array.getStringArrayList("id");
        int position = pos.getInt("position");
        //айдишечка
        String ID = arrayListID.get(position);
        //новое
        //Bundle idPat = getIntent().getExtras();
        //String IDpat = idPat.getString("idPat");
        //if (IDpat != null){
            //Toast.makeText(PatientActivity.this, IDpat, Toast.LENGTH_SHORT).show();
        //}


        //кнопочки
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditWindow(ID, uID);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteWindow(ID, uID);
            }
        });


        allPatients.document(ID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        tName.setText(document.getString("Name"));
                        tAge.setText(document.getString("Age"));
                    }

                }
            }
        });
    }

    private void showDeleteWindow(String ID, String uID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);

        LayoutInflater inflater = LayoutInflater.from(this);
        View delete_window = inflater.inflate(R.layout.delete_window, null);
        dialog.setView(delete_window);

        TextView nameD = delete_window.findViewById(R.id.textDelete);
        allPatients.document(ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                nameD.setText(documentSnapshot.getString("Name"));
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                allPatients.document(ID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PatientActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PatientActivity.this, SearchableActivity.class);
                        intent.putExtra("userID", uID);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error deleting document", e);
                    }
                });
            }
        });

        dialog.show();
    }

    private void showEditWindow(String ID, String uID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);
        dialog.setTitle("Edit");

        LayoutInflater inflater = LayoutInflater.from(this);
        View edit_window = inflater.inflate(R.layout.edit_window, null);
        dialog.setView(edit_window);

        MaterialEditText name = edit_window.findViewById(R.id.nameField);
        MaterialEditText age = edit_window.findViewById(R.id.ageField);

        allPatients.document(ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name.setText(documentSnapshot.getString("Name"));
                age.setText(documentSnapshot.getString("Age"));
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
                if(TextUtils.isEmpty(name.getText().toString())){
                    Snackbar.make(root, "Enter name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(age.getText().toString())){
                    Snackbar.make(root, "Enter age", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String sname = name.getText().toString();
                String sage = age.getText().toString();
                Map<String,Object> patient = new HashMap<>();
                patient.put("Name", sname);
                patient.put("Age", sage);

                allPatients.document(ID).set(patient).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PatientActivity.this, "Successful", Toast.LENGTH_LONG).show();
                        recreate();
                    }
                });
            }
        });

        dialog.show();

    }
}
