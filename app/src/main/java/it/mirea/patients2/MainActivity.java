package it.mirea.patients2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import it.mirea.patients2.User;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);

        root = findViewById(R.id.root_element);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistarWindow();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
            }
        });

        //Map<String, Object> claims = new HashMap<>();
        //claims.put("admin", true);
        //auth.setCustomUserClaims("87oUNP4RTHdKmN0zEoats34echj1", claims);
    }

    private void showSignInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);


        dialog.setTitle("SIGN IN");

        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(sign_in_window);

        MaterialEditText email = sign_in_window.findViewById(R.id.emailField);
        MaterialEditText pass = sign_in_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(root, "Enter your email", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(pass.getText().toString().length() < 5){
                    Snackbar.make(root, "Please enter a password greater than 5 characters", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String userID = auth.getCurrentUser().getUid();

                                users.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String level = snapshot.child(userID).child("level").getValue(String.class);
                                        if(level.equals ("2")){
                                            Intent intent = new Intent(MainActivity.this, SearchableActivity.class);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);
                                            finish();
                                        } else if(level.equals ("1")){
                                            Intent intent = new Intent(MainActivity.this, SearchableActivity1.class);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Intent intent = new Intent(MainActivity.this, SearchableActivity0.class);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root ,"Authorisation Error. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void showRegistarWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogStyle);
        dialog.setTitle("SIGN UP");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        MaterialEditText email = register_window.findViewById(R.id.emailField);
        MaterialEditText pass = register_window.findViewById(R.id.passField);
        MaterialEditText name = register_window.findViewById(R.id.nameField);
        MaterialEditText phone = register_window.findViewById(R.id.phoneField);

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(root, "Enter your email", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(pass.getText().toString().length() < 5){
                    Snackbar.make(root, "Please enter a password greater than 5 characters", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(name.getText().toString())){
                    Snackbar.make(root, "Enter your name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(phone.getText().toString())){
                    Snackbar.make(root, "Enter your phone", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                //Регистрация

                auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setPass(pass.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPhone(phone.getText().toString());
                                user.setLevel("0");

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Snackbar.make(root, "Registration completed", Snackbar.LENGTH_SHORT).show();
                                                auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                                                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                            @Override
                                                            public void onSuccess(AuthResult authResult) {
                                                                String userID = auth.getCurrentUser().getUid();
                                                                Intent intent = new Intent(MainActivity.this, SearchableActivity0.class);
                                                                intent.putExtra("userID", userID);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root, "Authorisation Error. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();

                    }
                });
            }
        });

        dialog.show();
    }
}