package com.example.nhs_blood_staff_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private TextView NameOutput, RoleOutput, A1Output, A2Output, AB1Output, AB2Output, B1Output, B2Output, O1Output, O2Output, DateInput, BloodStatus, EmailInput, PasswordInput, FirstInput, LastInput;
    private ImageView LogoutButton;
    private Button editRequestButton, createAccountButton;
    private Spinner BloodInput, StatusInput, RoleInput;
    private RequestAdapter ReqAdapter;
    private String currentDate;
    private CardView createView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private FirebaseAuth mAuth, mAuth2;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup fields for MainActivity
        mAuth = FirebaseAuth.getInstance();
        NameOutput = (TextView) findViewById(R.id.NameOutput);
        RoleOutput = (TextView) findViewById(R.id.RoleOutput);
        LogoutButton = (ImageView) findViewById(R.id.LogoutView);
        A1Output = (TextView) findViewById(R.id.A1Output);
        A2Output = (TextView) findViewById(R.id.A2Output);
        AB1Output = (TextView) findViewById(R.id.AB1Output);
        AB2Output = (TextView) findViewById(R.id.AB2Output);
        B1Output = (TextView) findViewById(R.id.B1Output);
        B2Output = (TextView) findViewById(R.id.B2Output);
        O1Output = (TextView) findViewById(R.id.O1Output);
        O2Output = (TextView) findViewById(R.id.O2Output);
        BloodInput = (Spinner) findViewById(R.id.bloodInput);
        StatusInput = (Spinner) findViewById(R.id.statusInput);
        DateInput = (TextView) findViewById(R.id.dateInput);
        editRequestButton = (Button) findViewById(R.id.editRequestButton);
        EmailInput = (TextView) findViewById(R.id.createEmailInput);
        PasswordInput = (TextView) findViewById(R.id.createPasswordInput);
        FirstInput = (TextView) findViewById(R.id.createFirstInput);
        LastInput = (TextView) findViewById(R.id.createLastInput);
        RoleInput = (Spinner) findViewById(R.id.createRoleInput);
        createView = (CardView) findViewById(R.id.createView);
        createAccountButton = (Button) findViewById(R.id.createAccountButton);

        // Gets Current Date
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = dateFormat.format(calendar.getTime());

        // Setup Arrays
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.BloodTypesChoice, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.StatusChoice, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.RoleChoice, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BloodInput.setAdapter(adapter1);
        StatusInput.setAdapter(adapter2);
        RoleInput.setAdapter(adapter3);

        // Button Listeners
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log user out, return to LoginActivity
                mAuth.signOut();
                finish();
            }
        });

        editRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit Request button clicked
                editRequest();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit Request button clicked
                createAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Functions to be completed on application load
        setupUserDetails();
        setupBloodStatus();
        setupSpinnerListener();
        DateInput.setText(currentDate);
        setupRecyclerView();
        // Allows for the previous Requests to be updated in real time
        ReqAdapter.startListening();
    }

    public void setupUserDetails() {
        // Function to request fields from user's document based on authentication ID then set them.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference docRef = db.collection("staff").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Set Details
                        NameOutput.setText(document.getString("first") + " " + document.getString("last"));
                        RoleOutput.setText(document.getString("role"));
                        if (document.getString("role").equals("Admin")) {
                            createView.setVisibility(View.VISIBLE);
                        } else {
                            createView.setVisibility(View.GONE);
                        }
                    } else {
                        // Failed to get document
                        //Toast.makeText(MainActivity.this, "No Document Found 1", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Failed to get collection
                    //Toast.makeText(MainActivity.this, "No collection Found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setupBloodStatus() {
        // Gets current status of blood and creates listener to keep updated in real-time
        String[] bloodTypes = {"A-", "A+", "AB-", "AB+", "B-", "B+", "O-", "O+"};
        for (int x = 0; x < bloodTypes.length; x++) {
            DocumentReference docRef = db.collection("requests").document(bloodTypes[x]);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        //Toast.makeText(MainActivity.this, "No document Found 2", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        // Checks if blood type is set to true
                        if (documentSnapshot.getString("Requested").equals("true")) {
                            if (documentSnapshot.getId().equals("A-")) {
                                A1Output.setText("Requested");
                                A1Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("A+")) {
                                A2Output.setText("Requested");
                                A2Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("AB-")) {
                                AB1Output.setText("Requested");
                                AB1Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("AB+")) {
                                AB2Output.setText("Requested");
                                AB2Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("B-")) {
                                B1Output.setText("Requested");
                                B1Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("B+")) {
                                B2Output.setText("Requested");
                                B2Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("O-")) {
                                AB1Output.setText("Requested");
                                AB1Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else if (documentSnapshot.getId().equals("O+")) {
                                O2Output.setText("Requested");
                                O2Output.setTextColor(getResources().getColor(R.color.ReqText));
                            } else {
                                Toast.makeText(MainActivity.this, "Blood Document Not Found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (documentSnapshot.getId().equals("A-")) {
                                A1Output.setText("Not Requested");
                                A1Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("A+")) {
                                A2Output.setText("Not Requested");
                                A2Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("AB-")) {
                                AB1Output.setText("Not Requested");
                                AB1Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("AB+")) {
                                AB2Output.setText("Not Requested");
                                AB2Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("B-")) {
                                B1Output.setText("Not Requested");
                                B1Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("B+")) {
                                B2Output.setText("Not Requested");
                                B2Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("O-")) {
                                AB1Output.setText("Not Requested");
                                AB1Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else if (documentSnapshot.getId().equals("O+")) {
                                O2Output.setText("Not Requested");
                                O2Output.setTextColor(getResources().getColor(R.color.NotReqText));
                            } else {
                                Toast.makeText(MainActivity.this, "Blood Document Not Found", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            });
        }
    }

    public void setupSpinnerListener() {
        // Listener for keeping the status of blood type updated when selecting them within drop-down
        BloodInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedBlood = BloodInput.getSelectedItem().toString();
                String selectedStatus = null;
                // Determines current status of blood
                if (selectedBlood.equals("A-")) {
                    selectedStatus = A1Output.getText().toString();
                } else if (selectedBlood.equals("A+")) {
                    selectedStatus = A2Output.getText().toString();
                } else if (selectedBlood.equals("AB-")) {
                    selectedStatus = AB1Output.getText().toString();
                } else if (selectedBlood.equals("AB+")) {
                    selectedStatus = AB2Output.getText().toString();
                } else if (selectedBlood.equals("B-")) {
                    selectedStatus = B1Output.getText().toString();
                } else if (selectedBlood.equals("B+")) {
                    selectedStatus = B2Output.getText().toString();
                } else if (selectedBlood.equals("O-")) {
                    selectedStatus = O1Output.getText().toString();
                } else if (selectedBlood.equals("O+")) {
                    selectedStatus = O2Output.getText().toString();
                }
                // Pastes current status into status spinner
                if (selectedStatus.equals("Requested")) {
                    StatusInput.setSelection(0);
                } else if (selectedStatus.equals("Not Requested")) {
                    StatusInput.setSelection(1);
                } else if (selectedStatus.equals(null)) {
                    Toast.makeText(MainActivity.this, "Failed to get blood Status", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    public void setupRecyclerView() {
        // Function to gather data to be put into recycler to show previous requests
        Query query = db.collection("previous");
        FirestoreRecyclerOptions<Request> options = new FirestoreRecyclerOptions.Builder<Request>().setQuery(query, Request.class).build();
        ReqAdapter = new RequestAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.requestsRecycler);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ReqAdapter);
    }

    public void editRequest() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        final String Blood = BloodInput.getSelectedItem().toString();
        final String Status = StatusInput.getSelectedItem().toString();
        DocumentReference docRef = db.collection("requests").document(Blood);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Check if Edit is copy of original status
                        if (Status.equals("Requested")) {
                            // Check if current status is true
                            if (document.getString("Requested").equals("false")) {
                                updateDatabase(Blood, "Requested", "true");
                                updateDatabase(Blood, "date", currentDate);
                                updateDatabase(Blood, "reqUser", currentUser.getUid());
                            }
                        } else if (Status.equals("Not Requested")) {
                            // Check if current status is true
                            if (document.getString("Requested").equals("true")) {
                                String reqDate = document.getString("date");
                                String reqUser = document.getString("reqUser");
                                createNewPrevious(Blood, reqDate, currentDate, reqUser, currentUser.getUid());
                                updateDatabase(Blood, "Requested", "false");
                                updateDatabase(Blood, "date", "");
                                updateDatabase(Blood, "reqUser", "");
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateDatabase(String document, String field, String content) {
        // Data sent to function includes field to be updated, and new content for the field.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //DocumentReference docRef = usersRef.document(currentUser.getUid());
        DocumentReference docRef = db.collection("requests").document(document);
        // Code edited from Firebase Assistant (Tools > Firebase > Firestore)
        docRef.update(field, content).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(MainActivity.this, "Update Successful", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(MainActivity.this, "Failed to Update", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void createNewPrevious(String blood, String start, String end, String userID1, String userID2) {
        // Create a new user data
        Map<String, Object> data = new HashMap<>();
        data.put("bloodType", blood);
        data.put("endDate", end);
        data.put("endUser", userID1);
        data.put("reqDate", start);
        data.put("reqUser", userID2);


        // Add a new document with authentication ID
        db.collection("previous").document().set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(MainActivity.this, "Creation Successful", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(MainActivity.this, "Creation Failed", Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void createAccount() {
        String StaffEmail = EmailInput.getText().toString();
        String StaffPassword = PasswordInput.getText().toString();
        final String StaffFirst = FirstInput.getText().toString();
        final String StaffLast = LastInput.getText().toString();
        final String StaffRole = RoleInput.getSelectedItem().toString();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("REVOKED")
                .setApiKey("REVOKED")
                .setApplicationId("REVOKED").build();
        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "NHS_Staff");
            mAuth2 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e) {
            mAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("NHS_Staff"));
        }
        if (validateInput(StaffEmail, StaffPassword, StaffFirst, StaffLast)) {
            mAuth2.createUserWithEmailAndPassword(StaffEmail, StaffPassword).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Toast.makeText(MainActivity.this, "Creation successful.", Toast.LENGTH_SHORT).show();
                        FirebaseUser createdUser = mAuth2.getCurrentUser();
                        String staffID = mAuth2.getUid();
                        // Add a new document with authentication ID
                        createNewUser(staffID, StaffFirst, StaffLast, StaffRole);
                        // Wipe fields
                        EmailInput.setText("");
                        PasswordInput.setText("");
                        FirstInput.setText("");
                        LastInput.setText("");
                        mAuth2.signOut();
                    } else if (!task.isSuccessful()) {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(MainActivity.this, "User with this email already exist.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    public void createNewUser(String userID, String first, String last, String role) {
        // Create staff data
        Map<String, Object> staffdata = new HashMap<>();
        staffdata.put("first", first);
        staffdata.put("last", last);
        staffdata.put("role", role);

        // Add a new document with authentication ID
        db.collection("staff").document(userID).set(staffdata)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Registration Success.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public boolean validateInput(String email, String password, String first, String last) {
        // Check if user inputted data into the fields
        int index = email.indexOf("@");
        if (email.isEmpty()) {
            EmailInput.setError("Enter Email Address");
        } else if (index == -1) {
            EmailInput.setError("Enter Valid Email Address");
        } else if (password.isEmpty()) {
            PasswordInput.setError("Enter Password");
        } else if (password.length() < 6) {
            PasswordInput.setError("Password too short");
            PasswordInput.setText("");
        } else if (first.isEmpty()) {
            FirstInput.setError("Enter First Name");
        } else if (last.isEmpty()) {
            LastInput.setError("Enter Last Name");
        } else {
            return true;
        }
        return false;
    }
}
