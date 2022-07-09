package it.mirea.patients2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SearchableActivity extends AppCompatActivity {

    Button btnPage, btnAdd;
    RelativeLayout root;

    ///бд с юзерами
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    //бд с пациентами
    FirebaseFirestore dbFirestore;
    CollectionReference allPatients;

    //списки и адаптор
    ListView lv;
    public ArrayAdapter<String> adapter;
    ArrayList<String> arrayListID;

    //поиск
    androidx.appcompat.widget.SearchView sv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        //получение данных из прошлой активити
        Bundle userID = getIntent().getExtras();
        String uID = userID.getString("userID");

        //кнопочки
        btnPage = findViewById(R.id.page);
        btnAdd = findViewById(R.id.add);
        root = findViewById(R.id.search_element);

        //с юзерами
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        //с пациентами
        dbFirestore = FirebaseFirestore.getInstance();
        allPatients = dbFirestore.collection("AllPatients");

        //списки и адаптер
        lv = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<>(SearchableActivity.this, R.layout.my_list_item);
        lv.setAdapter(adapter);

        //поиск
        sv = (androidx.appcompat.widget.SearchView) findViewById(R.id.search);
        sv.clearFocus();


        //поиск
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        ArrayList<String> arrayListID = new ArrayList<>();


        //кнопочки
        btnPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(uID);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddWindow(arrayListID, uID);
            }
        });

        showList(arrayListID, uID);

    }

    private void showList(ArrayList<String> arrayListID, String uID) {
        // показывает лист
        allPatients.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    ArrayList<String> al = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String pName = document.getString("Name");
                        String docID = document.getId();

                        al.add(pName);
                        arrayListID.add(docID);
                    }
                    adapter.clear();
                    adapter.addAll(al);

                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Intent intent = new Intent(SearchableActivity.this, PatientActivity.class);
                            intent.putExtra("id", arrayListID);
                            intent.putExtra("position", position);
                            intent.putExtra("userID", uID);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }


    private void showPage(String uID) {
        Intent intent = new Intent(SearchableActivity.this, DoctorActivity.class);
        intent.putExtra("userID", uID);
        startActivity(intent);
    }


    //add
    private void showAddWindow(ArrayList<String> arrayListID, String uID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);

        LayoutInflater inflater = LayoutInflater.from(this);
        View add_window = inflater.inflate(R.layout.add_window, null);
        dialog.setView(add_window);

        MaterialEditText name = add_window.findViewById(R.id.nameField);
        MaterialEditText age = add_window.findViewById(R.id.ageField);

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
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

                //Передача базе
                String sname = name.getText().toString();
                String sage = age.getText().toString();
                Map<String,Object> patient = new HashMap<>();
                patient.put("Name", sname);
                patient.put("Age", sage);


                //Рандомный числовой id
                int min = 100000000;
                int max = 999999999;
                int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
                String num = String.valueOf(randomNum);


                allPatients.document(num).set(patient).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showList(arrayListID, uID);
                        Toast.makeText(SearchableActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();

    }
}