package it.mirea.patients2;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class PatientActivity1 extends AppCompatActivity {

    RelativeLayout root;
    TextView tName, tAge;

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
        setContentView(R.layout.activity_patient1);

        //получение данных из прошлой активити
        Bundle userID = getIntent().getExtras();
        String uID = userID.getString("userID");

        root = findViewById(R.id.patient1_element);
        tName = findViewById(R.id.textName);
        tAge = findViewById(R.id.textAge);

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
}
