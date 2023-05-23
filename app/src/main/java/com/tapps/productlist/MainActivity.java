package com.tapps.productlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    // Views
    RecyclerView recyclerViewProducts;
    View bottomToolbar;
    ImageButton ibtnToolbarSignIn;
    ImageButton ibtnToolbarSignOut;
    ImageButton ibtnToolbarGroups;

    EditText etDialogEmail;
    EditText etDialogPassword;
    EditText etDialogGroupsGroupName;

    TextView tvLogin;

    // Variables
    Context context = this;
    Dialog dialogSignInSignUp;
    Dialog dialogGroups;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewProducts = findViewById(R.id.mainActivityRecyclerView);
        bottomToolbar = findViewById(R.id.includedLayoutBottomToolbarMainActivity);

        ibtnToolbarSignIn = bottomToolbar.findViewById(R.id.ibtnToolbarSignIn);
        ibtnToolbarSignOut = bottomToolbar.findViewById(R.id.ibtnToolbarSignOut);
        ibtnToolbarGroups = bottomToolbar.findViewById(R.id.ibtnToolbarGroups);

        tvLogin = findViewById(R.id.tvMainActivityLogin);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        dialogSignInSignUp = new Dialog(this);
        dialogGroups = new Dialog(this);

        setClickListeners();
    }

    /** OnClicks **/
    public void onClickJoinGroup(View view) {
        // Edit data in firebase
        String groupName = String.valueOf(etDialogGroupsGroupName.getText());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.i("USERETAG", "USER: " + String.valueOf(currentUser));
        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(context, getString(R.string.groupNameMustNotBeEmpty), Toast.LENGTH_LONG).show();
        } else if (currentUser == null) {
            Log.i("USERTAG", "currentUser is null");
            Toast.makeText(context, getString(R.string.youMustBeLogged), Toast.LENGTH_SHORT).show();
        } else {
            //mDatabase.child("Users").child(currentUser.getUid()).setValue(groupName);
            mDatabase.child("Users").child("testUser").setValue(";skldjf;aslkdf");
        }

        dialogGroups.dismiss();

    }
    public void onClickSignUp(View view) {
        String email = String.valueOf(etDialogEmail.getText());
        String password = String.valueOf(etDialogPassword.getText());

        if (!TextUtils.isEmpty(email) & !TextUtils.isEmpty(password) & password.length() >= 6) {
            //
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    Log.d("TAG",""+task.getResult().getSignInMethods().size());
                    if (task.getResult().getSignInMethods().size() == 0){
                        // email not existed
                        createFirebaseUser(email, password);
                    }else {
                        // email existed
                        signInToFirebase(email, password);
                    }

                    tvLogin.setText(email);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
        dialogSignInSignUp.dismiss();

    }

    private void setClickListeners() {

        // Sign IN or sign Up
        ibtnToolbarSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSignInSignUp.setContentView(R.layout.sign_in_or_create_account_dialog);
                etDialogEmail = dialogSignInSignUp.findViewById(R.id.etDialogEmail);
                etDialogPassword = dialogSignInSignUp.findViewById(R.id.etDialogPassword);
                dialogSignInSignUp.show();

            }
        });

        //Sign OUT
        ibtnToolbarSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.getInstance().signOut();
                tvLogin.setText(getText(R.string.notLoggedYet));
            }
        });

        // Groups
        ibtnToolbarGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogGroups.setContentView(R.layout.groups_dialog);
                etDialogGroupsGroupName = dialogGroups.findViewById(R.id.etDialogGroupsGroupName);
                dialogGroups.show();

            }
        });


        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.i("USERTAG", "User: " + String.valueOf(currentUser));

    }

    /** auth functions **/
    private void createFirebaseUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signInToFirebase(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }
    // Other functions
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.i("USERTAG", "User: " + String.valueOf(currentUser));
        if(currentUser != null){
            reload();
        }
    }

    private void reload() {}

    private void updateUI(FirebaseUser user) {}
}