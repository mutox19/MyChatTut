package com.example.danilo.mychattut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {


    private CircleImageView settingsDisplayProfileImage;
    private TextView settingsDisplayName;
    private TextView settingsDisplayStatus;
    private Button settingsChangeProfileImageBtn;
    private Button settingsChangeStatus;
    private final static int GALLARY_PICK = 1;
    Bitmap thumb_bitMap = null;
    private ProgressDialog loadingBar;

    private DatabaseReference getUserDataReference;
    private StorageReference thumbImageStorageRef;
    private FirebaseAuth mAuth;
    private StorageReference storeProfileImageStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        //get a ref to the image ref
        storeProfileImageStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumbImageStorageRef = FirebaseStorage.getInstance().getReference().child("thumb_images");


        loadingBar = new ProgressDialog(this);
        settingsDisplayProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        settingsDisplayName = (TextView) findViewById(R.id.settings_UserName);
        settingsDisplayStatus = (TextView) findViewById(R.id.settings_UserStatus);
        settingsChangeProfileImageBtn = (Button) findViewById(R.id.settingsChangeProfileImageBtn);
        settingsChangeStatus = (Button) findViewById(R.id.settingsChangeProfileStatus);


        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                final String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();


                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);

                //if the image from the database does not equal default profile then we will display user profile
                if(!thumb_image.equals("default_profile"))
                {
                    //Picasso.with(getBaseContext()).load(thumb_image).placeholder(R.drawable.default_profile).into(settingsDisplayProfileImage);
                    //to load images offline
                    Picasso.with(getBaseContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(settingsDisplayProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                            //will load images offline
                        }

                        @Override
                        public void onError() {
                            //to load images only when online
                            Picasso.with(getBaseContext()).load(thumb_image).placeholder(R.drawable.default_profile).into(settingsDisplayProfileImage);
                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingsChangeProfileImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send the user to his gallery
                Intent galleryIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLARY_PICK);
            }
        });


        settingsChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldStatus = settingsDisplayStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("user_status", oldStatus);
                startActivity(statusIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Updating Profile Image");
                loadingBar.setMessage("Plese Wait While We Are Updating Your Profile Image...");
                loadingBar.show();
                Uri resultUri = result.getUri();

                //get the path of the users image
                File thumb_file_Path_URi = new File(resultUri.getPath());


                String userID = mAuth.getCurrentUser().getUid();

                try
                {
                    thumb_bitMap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_file_Path_URi);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitMap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte  = byteArrayOutputStream.toByteArray();

                //store profile image as userID and jpg
                StorageReference filePath = storeProfileImageStorageRef.child(userID + ".jpg");
                final StorageReference thumb_FilePath = thumbImageStorageRef.child(userID + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {

                            //get the firebase url of the profile image
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_FilePath.putBytes(thumb_byte);
                            //upload thumb image to firebase storage
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_download_URl = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful())
                                    {
                                        Map update_User_Data = new HashMap();
                                        update_User_Data.put("user_image",downloadUrl);
                                        update_User_Data.put("user_thumb_image",thumb_download_URl);

                                        getUserDataReference.updateChildren(update_User_Data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SettingsActivity.this, "Image Uploaded Successfully...",Toast.LENGTH_LONG).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Eror Occured While Uploading Your Profile Image...",Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //send user to main activity after pressing the back button
        //and if they press back on main activity dont send them back here instead close the app
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}
