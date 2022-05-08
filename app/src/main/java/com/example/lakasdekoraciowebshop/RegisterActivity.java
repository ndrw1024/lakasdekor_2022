package com.example.lakasdekoraciowebshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText passwordConfirmEditText;
    EditText phoneEditText;
    Spinner phone_spinner;
    EditText addressEditText;
    RadioGroup accountTypeGroup;

    private SharedPreferences preferences;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Bundle bundle = getIntent().getExtras();
        //int secret_key = bundle.getInt("SECRET_KEY");
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if(secret_key != 99){
            finish();
        }

        userNameEditText =  findViewById(R.id.userNameEditText);
        userEmailEditText =  findViewById(R.id.userEmailEditText);
        passwordEditText =  findViewById(R.id.passwordEditText);
        passwordConfirmEditText =  findViewById(R.id.passwordAgainEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        phone_spinner = findViewById(R.id.phoneSpinner);
        accountTypeGroup = findViewById(R.id.accountTypeGroup);
        accountTypeGroup.check(R.id.buyerRadioButton);


        preferences =  getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName","");
        String password = preferences.getString("password","");

        userNameEditText.setText(userName);
        passwordEditText.setText(password);
        passwordConfirmEditText.setText(password);

        phone_spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.phone_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phone_spinner.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        Log.i(LOG_TAG, "onCreate");

    }

    public void register(View view) {
        String userName = userNameEditText.getText().toString();
        String email = userEmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();

        if(!password.equals(passwordConfirm)){
            Log.e(LOG_TAG, "Nem egyező jelszavak!");
            return;
        }

        String phoneNumber = phoneEditText.getText().toString();
        String phoneType = phone_spinner.getSelectedItem().toString();
        String address = addressEditText.getText().toString();

        int checkedId = accountTypeGroup.getCheckedRadioButtonId();
        RadioButton radioButton = accountTypeGroup.findViewById(checkedId);
        String accountType = radioButton.getText().toString();

        Log.i(LOG_TAG, "Regisztrált: " + userName + ", email: " + email + ", jelszó: " + password);
        //TODO: a regisztráció megvalósítása
        //startShopping();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "USER CREATED SUCCESSFULLY");
                    startShopping();
                }else{
                    Log.d(LOG_TAG, "USER CREATION UNSUCCESSFUL");
                    Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció: " + task.getException().getMessage() ,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void startShopping(/* registered user data*/){
        Intent intent = new Intent(this, ShopListActivity.class);
        //intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedItem = adapterView.getItemAtPosition(i).toString();
        Log.i(LOG_TAG, selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //TODO
    }
}