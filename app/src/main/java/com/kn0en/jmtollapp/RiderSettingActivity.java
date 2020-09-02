package com.kn0en.jmtollapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderSettingActivity extends AppCompatActivity {

    private TextInputEditText mNameRider,mPhoneRider,mCarNumberRider;

    private MaterialButton mConfirm;

    private CircleImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mRiderDatabase;

    private String userID,mName,mPhone,mCarNumber,mProfileImageUrl;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_setting);

        mNameRider = (TextInputEditText) findViewById(R.id.rider_name);
        mPhoneRider = (TextInputEditText) findViewById(R.id.rider_phone);
        mCarNumberRider = (TextInputEditText) findViewById(R.id.rider_nopol);

        mProfileImage = (CircleImageView) findViewById(R.id.profileImage);

        mConfirm = (MaterialButton) findViewById(R.id.btnConfirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mRiderDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(userID);

        getUserInfo();

        mProfileImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,1);
        });

        mConfirm.setOnClickListener(view -> saveUserInformation());

    }

    private void getUserInfo(){
        mRiderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null){
                        mName = map.get("name").toString();
                        mNameRider.setText(mName);
                    }
                    if (map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        mPhoneRider.setText(mPhone);
                    }
                    if (map.get("carNumber") != null){
                        mCarNumber = map.get("carNumber").toString();
                        mCarNumberRider.setText(mCarNumber);
                    }
                    if (map.get("profileImageUrl") != null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void saveUserInformation() {
        mName = mNameRider.getText().toString();
        mPhone = mPhoneRider.getText().toString();
        mCarNumber = mCarNumberRider.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("carNumber", mCarNumber);

        mRiderDatabase.updateChildren(userInfo);

        if (resultUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(e -> {
                finish();
                return;
            });

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();

                downloadUrl.addOnSuccessListener(uri -> {
                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", uri.toString());
                    mRiderDatabase.updateChildren(newImage);

                    finish();
                    return;
                });
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}