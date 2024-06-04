package com.example.nhs_blood_staff_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    private TextView userEmail, userPassword, ErrorMsg;
    private Button LoginButton;
    private FirebaseAuth mAuth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        LoginButton = (Button) findViewById(R.id.LoginButton);
        userEmail = (TextView) findViewById(R.id.EmailInput);
        userPassword = (TextView) findViewById(R.id.PasswordInput);
        ErrorMsg = (TextView) findViewById(R.id.ErrorMsg);

        //DELETE THIS LATER
        //startActivity(new Intent(LoginActivity.this, MainActivity.class));

        // Buttons
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirm SignIn
                signIn();
            }
        });
    }

    private void signIn() {
        // Take text from input and pass through validate
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if (!validateInput(email, password)) {
            return;
        }

        // Code edited from Firebase Assistant (Tools > Firebase > Authentication)
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success
                    checkRole();
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    userPassword.setText("");
                }
            }
        });
    }

    public void checkRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference docRef = db.collection("staff").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Launch MainActivity
                        userEmail.setText("");
                        userPassword.setText("");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        // Failed to get document
                        userPassword.setText("");
                        ErrorMsg.setVisibility(View.VISIBLE);
                        mAuth.signOut();
                    }
                } else {
                    // Failed to get collection
                    Toast.makeText(LoginActivity.this, "No collection Found", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public boolean validateInput(String email, String password) {
        // Check if user inputted data into the fields
        int index = email.indexOf("@");
        if (email.isEmpty()) {
            userEmail.setError("Enter Email Address");
        } else if (index == -1) {
            userEmail.setError("Enter Valid Email Address");
        } else if (password.isEmpty()) {
            userPassword.setError("Enter Password");
        } else {
            return true;
        }
        return false;
    }
}
