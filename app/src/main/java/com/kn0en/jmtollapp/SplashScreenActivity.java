package com.kn0en.jmtollapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    private FirebaseDatabase database;
    private DatabaseReference riderRef;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener(listener);

        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();
    }

    private void init() {

        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        riderRef = database.getReference().child("Users").child("Riders");
        firebaseAuth = FirebaseAuth.getInstance();

        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null)
               {
                   Intent intent = new Intent(SplashScreenActivity.this,RiderMapsActivity.class);
                   startActivity(intent);
                   finish();
               }else {
                showLogRegLayout();
            }

        };
    }

     private void  showLogRegLayout() {
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_sign_up, null);

        TextInputEditText edt_email = (TextInputEditText) itemView.findViewById(R.id.edt_email);
        TextInputEditText edt_password = (TextInputEditText) itemView.findViewById(R.id.edt_password);

        MaterialButton btn_signIn = (MaterialButton) itemView.findViewById(R.id.btn_signIn);
        MaterialButton btn_signUp = (MaterialButton) itemView.findViewById(R.id.btn_signUp);

        AlertDialog builder = new AlertDialog.Builder(this).create();
                builder.setView(itemView);

                builder.show();

        btn_signIn.setOnClickListener(view -> {
            if (TextUtils.isEmpty(edt_email.getText().toString()) || TextUtils.isEmpty(edt_password.getText().toString()))
            {
                Toast.makeText(this, "Please fill email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(edt_email.getText().toString()))
            {
                Toast.makeText(this, "Please enter valid email.", Toast.LENGTH_SHORT).show();
                return;
            }

            else if (TextUtils.isEmpty(edt_password.getText().toString()))
            {
                Toast.makeText(this, "Please enter valid password.", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                final String email = edt_email.getText().toString();
                final String password = edt_password.getText().toString();
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()){
                        Toast.makeText(SplashScreenActivity.this, "Failed to sign in! Please check email and password correctly.", Toast.LENGTH_SHORT).show();
                        builder.dismiss();
                        showLogRegLayout();
                    }else {
                        Toast.makeText(SplashScreenActivity.this, "Sign in successfully!", Toast.LENGTH_SHORT).show();
                        builder.dismiss();
                    }
                });
            }

        });

        btn_signUp.setOnClickListener(view -> {

            if (TextUtils.isEmpty(edt_email.getText().toString()) || TextUtils.isEmpty(edt_password.getText().toString()))
            {
                Toast.makeText(this, "Please fill email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(edt_email.getText().toString()))
            {
                Toast.makeText(this, "Please enter valid email.", Toast.LENGTH_SHORT).show();
                return;
            }

            else if (TextUtils.isEmpty(edt_password.getText().toString()))
            {
                Toast.makeText(this, "Please enter valid password.", Toast.LENGTH_SHORT).show();
                return;
            }

            else if (!validateResetPassword(edt_password.getText().toString())){
                Toast.makeText(this, "Invalid password, Minimal password is 6 characters.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            else
            {
                final String email = edt_email.getText().toString();
                final String password = edt_password.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()){
                        Toast.makeText(SplashScreenActivity.this, "Failed to sign up! or User already registered!", Toast.LENGTH_SHORT).show();
                        builder.dismiss();
                        showLogRegLayout();
                    }else {
                        String user_id = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(user_id);
                        current_user_db.setValue(email);
                        Toast.makeText(SplashScreenActivity.this, "Sign up successfully!", Toast.LENGTH_SHORT).show();
                        builder.dismiss();
                    }
                });
            }

        });
    }

    private boolean validateResetPassword(String password) {
        boolean valid = true;
        if (password.length() <= 6) {
            valid = false;
        }
        return valid;
    }

    private void delaySplashScreen() {

        progressBar.setVisibility(View.GONE);

        Completable.timer(1, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(() ->

                        //After show Splashscreen ,ask login if not login
                        firebaseAuth.addAuthStateListener(listener)


                        );
    }


}