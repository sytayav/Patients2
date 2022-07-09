package it.mirea.patients2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchableActivity1 extends AppCompatActivity {

    Button btnPage;
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
        setContentView(R.layout.activity_searchable1);

        //получение данных из прошлой активити
        Bundle userID = getIntent().getExtras();
        String uID = userID.getString("userID");

        //кнопочки
        btnPage = findViewById(R.id.page1);
        root = findViewById(R.id.search1_element);

        //с юзерами
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        //с пациентами
        dbFirestore = FirebaseFirestore.getInstance();
        allPatients = dbFirestore.collection("AllPatients");

        //списки и адаптер
        lv = (ListView) findViewById(R.id.list1);
        adapter = new ArrayAdapter<>(SearchableActivity1.this, R.layout.my_list_item);
        lv.setAdapter(adapter);

        //поиск
        sv = (androidx.appcompat.widget.SearchView) findViewById(R.id.search1);
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
                            Intent intent = new Intent(SearchableActivity1.this, PatientActivity1.class);
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
        Intent intent = new Intent(SearchableActivity1.this, DoctorActivity.class);
        intent.putExtra("userID", uID);
        startActivity(intent);
    }

}