package com.kn0en.jmtollapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class UpdatePasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mOfficerDatabase;

    private MaterialButton mConfirm;

    private TextInputEditText newPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        mConfirm = (MaterialButton) findViewById(R.id.btnConfirm);

        newPwd  = (TextInputEditText) findViewById(R.id.new_password);

        mConfirm.setOnClickListener(view -> {
            if (TextUtils.isEmpty(newPwd.getText().toString())) {
                Toast.makeText(this,
                        "Please enter new password",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            else if(!validateResetPassword(newPwd.getText().toString())){
                Toast.makeText(UpdatePasswordActivity.this,
                        "Invalid password, Minimal password is 6 characters.",
                        Toast.LENGTH_SHORT).show();
                return;
            }else {


                String newpassword = newPwd.getText().toString();
                assert user != null;
                user.updatePassword(newpassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdatePasswordActivity.this,
                                        "Password has been updated, Please log in again!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.e("TAG", "Error in updating password",
                                        task.getException());
                                Toast.makeText(UpdatePasswordActivity.this,
                                        "Failed to update password.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private boolean validateResetPassword(String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(password) || password.length() <= 6) {
            valid = false;
        }
        return valid;
    }

}