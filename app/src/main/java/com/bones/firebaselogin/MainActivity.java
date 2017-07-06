package com.bones.firebaselogin;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private Button btnSignout;
    private TextView welcome;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private RecyclerView rv_Image;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FloatingActionButton btnAdd;
    private int PICK_IMAGE_REQUEST = 111;
    private Uri filePath;
    private FirebaseUser user;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<ImageModel> list;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = database.getReference("uploads");

        welcome = (TextView)findViewById(R.id.txtEmail);
        welcome.setText("Welcome\n"+user.getEmail());

        storageReference = storage.getReferenceFromUrl("gs://fir-login-83a76.appspot.com/");
        //Uri uri = storageReference.child("lul.jpg").getDownloadUrl().getResult();
        //Picasso.with(this).load(uri).resize(100,100).fit().into(profileImage);

        storageReference.child("Images/lul.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error",e.toString());
            }
        });


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                }
            }
        };

        setImage();

        rv_Image = (RecyclerView) findViewById(R.id.rv_image);
        rv_Image.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(MainActivity.this,2);
        rv_Image.setLayoutManager(layoutManager);

        btnSignout = (Button) findViewById(R.id.sign_out);

        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
            }
        });

        btnAdd = (FloatingActionButton) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

        databaseReference.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot!=null) {
                    list = new ArrayList<>();
                    list.clear();
                    for (DataSnapshot imageDataSnapshot : dataSnapshot.getChildren()) {
                        //String keyIm = imageDataSnapshot.getKey();
                        String imgUrl = imageDataSnapshot.getValue().toString();
                        ImageModel im = new ImageModel();
                        im.setImage_url(imgUrl);
                        list.add(im);
                    }
                    mAdapter = new RecyclerAdapter(list,MainActivity.this);
                    rv_Image.setAdapter(mAdapter);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setImage(){
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    list = new ArrayList<>();
                    list.clear();
                    for (DataSnapshot imageDataSnapshot : dataSnapshot.getChildren()) {
                        //String keyIm = imageDataSnapshot.getKey();
                        ImageModel im = imageDataSnapshot.getValue(ImageModel.class);
                        list.add(im);
                    }
                    mAdapter = new RecyclerAdapter(list,MainActivity.this);
                    rv_Image.setAdapter(mAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authListener != null){
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            if(filePath != null) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();

                StorageReference childRef = storageReference.child("Images/"+ts);



                //uploading the image
                UploadTask uploadTask = childRef.putFile(filePath);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri download_uri = taskSnapshot.getDownloadUrl();

                        ImageModel im = new ImageModel();
                        im.setImage_url(download_uri.toString());
                        String key = databaseReference.child(user.getUid()).push().getKey();

                        databaseReference.child(user.getUid()).child(key).setValue(im);

                        Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }



}
