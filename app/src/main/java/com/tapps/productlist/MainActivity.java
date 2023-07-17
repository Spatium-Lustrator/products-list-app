package com.tapps.productlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements onCheckBoxClick,  onOftenProductClick {

    /** Views **/
    RecyclerView recyclerViewProducts;
    RecyclerView recyclerViewOftenProducts;

    EditText etDialogEmail;
    EditText etDialogPassword;
    EditText etDialogGroupsGroupName;
    EditText etProductName;

    TextView tvLogin;

    /** Variables **/
    ArrayList<String> listProducts = new ArrayList<String>();
    ArrayList<String> listOftenProducts = new ArrayList<String>();
    RecyclerViewAdapter recyclerViewAdapter;
    Context context = this;
    Dialog dialogSignInSignUp;
    Dialog dialogGroups;
    Dialog dialogAddProduct;
    private SharedPreferences settings;
    private ArrayList<String> nowSelectedItems = new ArrayList<String>();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewProducts = findViewById(R.id.mainActivityRecyclerView);

        tvLogin = findViewById(R.id.tvMainActivityLogin);
        /** Initialize variables **/
        settings = getSharedPreferences(Consts.APP_PREFERENCES, Context.MODE_PRIVATE);

        dialogSignInSignUp = new Dialog(this);
        dialogGroups = new Dialog(this);
        dialogAddProduct = new Dialog(this);

        dialogGroups.setCancelable(false);
        dialogSignInSignUp.setCancelable(false);

        //usersGroupName = settings.getString(Consts.APP_PREFERENCES_GROUP_NAME, "");
        recyclerViewAdapter = new RecyclerViewAdapter(this, listProducts, this);



        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            updateUI(getString(R.string.notLoggedYet));
        } else {
            updateUI(currentUser.getEmail());
            getUserGroupFromFirebase(currentUser.getUid());
            //getProductsFromFirebase(usersGroupName);
        }

        /** Ini functions **/
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewProducts.setAdapter(recyclerViewAdapter);
    }
    /** Implementations **/
    @Override
    public void onClickOften(String pressedItem) {
        etProductName.setText(pressedItem);
        Log.i("D:LFKJ", "ONCLICKOFTEN");
    }
    // For checkbox click
    @Override
    public void onClick(ArrayList<String> selectedItems) {
        Log.i("DEVELOP-DELETING", "MAIN ACTIVITY: " + String.valueOf(selectedItems));
        nowSelectedItems = selectedItems;
    }

    /** OnClicks **/
    public void onClickMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.popup_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (getString(R.string.addOftenProduct).equals(String.valueOf(menuItem))) {
                    Intent startOftenProductActivityIntent = new Intent(MainActivity.this, OftenProductsActivity.class);
                    startActivity(startOftenProductActivityIntent);


                } else if (getString(R.string.joinGroup).equals(String.valueOf(menuItem))) {

                    dialogGroups.setContentView(R.layout.groups_dialog);
                    etDialogGroupsGroupName = dialogGroups.findViewById(R.id.etDialogGroupsGroupName);
                    dialogGroups.setCancelable(false);
                    dialogGroups.show();

                } else if (getString(R.string.signIn).equals(String.valueOf(menuItem))) {

                    dialogSignInSignUp.setContentView(R.layout.sign_in_or_create_account_dialog);
                    etDialogEmail = dialogSignInSignUp.findViewById(R.id.etDialogEmail);
                    etDialogPassword = dialogSignInSignUp.findViewById(R.id.etDialogPassword);
                    dialogSignInSignUp.show();

                } else if (getString(R.string.signOut).equals(String.valueOf(menuItem))) {

                    mAuth.getInstance().signOut();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Consts.APP_PREFERENCES_GROUP_NAME, "");
                    editor.apply();
                    updateUI(getString(R.string.notLoggedYet));
                }

                return false;
            }
        });

        popupMenu.show();
    }
    public void onClickReload(View view){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getUserGroupFromFirebase(currentUser.getUid());
        } else {
            Toast.makeText(context, getString(R.string.notLoggedYet), Toast.LENGTH_SHORT).show();
        }
    }
    public void onClickDeleteProducts(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Integer index = 0; index < nowSelectedItems.size(); index++) {
                    listProducts.remove(nowSelectedItems.get(index));
                }

                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            Log.i("GETPRODUCTSDAMN", "Group gotten");
                            mDatabase
                                    .child("Groups")
                                    .child(String.valueOf(task.getResult().getValue()))
                                    .setValue(String.join(";", listProducts));
                            //Log.d("firebase", String.valueOf(task.getResult().getValue()));
                            //g//etProductsFromFirebase(String.valueOf(task.getResult().getValue()));
                            onClickReload(findViewById(R.id.floabReload));
                        }
                    }
                });

            }
        }).start();
    }
    public void onClickConfirmAddingProduct(View view){
        String productName = String.valueOf(etProductName.getText());
        listProducts.add(productName);
        recyclerViewAdapter.notifyDataSetChanged();
        // Edit data in firebase
        // TODO: This is a crutch
        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.i("GETPRODUCTSDAMN", "Group gotten");
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    String products = String.join(";", listProducts);
                    if (products.startsWith(";")) {
                        products = products.substring(1);
                    }
                    mDatabase.child("Groups").child(String.valueOf(task.getResult().getValue())).setValue(products);
                    Toast.makeText(context, getString(R.string.successful), Toast.LENGTH_SHORT).show();
                    dialogAddProduct.dismiss();
                }
            }
        });


    }
    public void onClickAddProduct(View view) {
        listOftenProducts.clear();

        dialogAddProduct.setContentView(R.layout.add_product_dialog);
        etProductName = dialogAddProduct.findViewById(R.id.etDialogAddProductProductName);
        recyclerViewOftenProducts = dialogAddProduct.findViewById(R.id.rvAddProductDialogOftenProducts);

        String[] list = settings.getString(Consts.APP_PREFERENCES_OFTEN_PRODUCTS, "").split(";");
        listOftenProducts.addAll(Arrays.asList(list));
        listOftenProducts.remove("");

        recyclerViewOftenProducts.setLayoutManager(new LinearLayoutManager(context));
        RecyclerViewOftenProductsAdapter recyclerViewOftenProductsAdapter = new RecyclerViewOftenProductsAdapter(context, listOftenProducts, this);
        recyclerViewOftenProducts.setAdapter(recyclerViewOftenProductsAdapter);

        dialogAddProduct.show();

    }
    public void onClickJoinGroup(View view) {
        // Edit data in firebase
        String groupName = String.valueOf(etDialogGroupsGroupName.getText());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(context, getString(R.string.groupNameMustNotBeEmpty), Toast.LENGTH_LONG).show();
        } else if (currentUser == null) {
            Toast.makeText(context, getString(R.string.youMustBeLogged), Toast.LENGTH_SHORT).show();
        } else {
            mDatabase.child("Users").child(currentUser.getUid()).setValue(groupName);
            DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
            groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChild(groupName)) {
                        groupsRef.child(groupName).setValue("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            dialogGroups.dismiss();
        }




        //SharedPreferences.Editor editor = settings.edit();
        //editor.putString(Consts.APP_PREFERENCES_GROUP_NAME, groupName);
        //editor.apply();



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

                    updateUI(email);

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

    /** get data **/
    private void getProductsFromFirebase(String groupName) {
        listProducts.clear();
        mDatabase.child("Groups").child(groupName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.i("GETPRODUCTSDAMN", "GetPROUDECT~!~!~");
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    String[] list = String.valueOf(task.getResult().getValue()).split(";");
                    listProducts.addAll(Arrays.asList(list));
                    listProducts.remove("");
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void getUserGroupFromFirebase(String userUID) {
        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.i("GETPRODUCTSDAMN", "Group gotten");
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    getProductsFromFirebase(String.valueOf(task.getResult().getValue()));
                }
            }
        });
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
                            mDatabase.child("Users").child(user.getUid()).setValue("");
                            dialogGroups.setContentView(R.layout.groups_dialog);
                            etDialogGroupsGroupName = dialogGroups.findViewById(R.id.etDialogGroupsGroupName);
                            dialogGroups.setCancelable(false);
                            dialogGroups.show();
                            updateUI(user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(getString(R.string.notLoggedYet));
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
                            updateUI(user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(getString(R.string.notLoggedYet));
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

    private void updateUI(String textForLoginTextView) {
        tvLogin.setText(textForLoginTextView);
    }



}