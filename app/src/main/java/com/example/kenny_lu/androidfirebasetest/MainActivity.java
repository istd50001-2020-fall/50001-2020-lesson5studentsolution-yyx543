package com.example.kenny_lu.androidfirebasetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.View;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView textViewSampleNodeValue;
    ImageView imageViewSatisfied;
    TextView textViewTally;
    final static int REQUEST_IMAGE_GET = 2000;


    //TODO 13.0 Plan your database design in advance
    //TODO and create the necessary instance variables
    final String SAMPLE_NODE = "pokemon";
    final String SATISFIED = "satisfied";
    final String TALLY = "tally";
    final String NO_SATISFIED = "number_satisfied";

    DatabaseReference mRootDatabaseRef;
    DatabaseReference mNodeRefPokemon;
    DatabaseReference mNodeRefSatisfied;
    DatabaseReference mNodeRefTally;

    final String sharedPrefFile = "sharedPref";
    SharedPreferences sharedPreferences;
    final String SATISFIED_KEY = "key_satisfied";
    int satisfiedTallyValue;

    //TODO 13.0 Create your firebase realtime database
    /** FOLLOW THE INSTRUCTIONS IN FIREBASE
     * put the google-services.json in the necessary folder
     * update the project-level and module-level gradle file
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO 13.1 Get references to the widgets
        textViewSampleNodeValue = findViewById(R.id.textViewSampleNodeValue);
        textViewTally = findViewById(R.id.textViewTally);
        imageViewSatisfied = findViewById(R.id.imageViewSatisfied);

        //TODO 13.2 Get references to the nodes in the database
        /** This is to the root note
         /* The rest are to child nodes */
        mRootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mNodeRefPokemon = mRootDatabaseRef.child(SAMPLE_NODE);
        mNodeRefSatisfied = mRootDatabaseRef.child(SATISFIED);
        mNodeRefTally = mRootDatabaseRef.child(TALLY);

        Log.i("Pokemon","" + mRootDatabaseRef.toString());
        Log.i("Pokemon", "" + mNodeRefPokemon.toString());

        //TODO 13.3 When the satisfied button is clicked, push the info to the database
        /** executing mNodeRefSatisfied.push() creates child nodes with random ID
         *  mNoteRefTally.child("data") creates a child node if it didn't exist
         *  mNoteRefTally.child("data").setValue( ) assigns a value to the node
         *  explore what happens if you did this subsequently:
         *  mNoteRefTally.child("data").child("data1").setValue() */
        imageViewSatisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                mNodeRefSatisfied.push().setValue(timestamp.toString());
                mNodeRefTally.child(NO_SATISFIED).setValue(satisfiedTallyValue+1);
                Toast.makeText(MainActivity.this, "Thank you", Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 13.4 Listen out for changes in the "pokemon" node and update the TextView
        /** this is code to listen for changes in the value of any particular node */
        mNodeRefPokemon.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String change = snapshot.getValue(String.class);
                textViewSampleNodeValue.setText(change);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Pokemon","" + error.toString());
            }
        });

        // TODO 13.5 Listen out for changes in the "satisfied" node and update the TextView
        /**this is code to listen for changes in the child nodes of any node
         * here, I am counting the number of child nodes */
        mNodeRefSatisfied.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                satisfiedTallyValue = (int) dataSnapshot.getChildrenCount();
                textViewTally.setText(Integer.toString(satisfiedTallyValue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("Pokemon", databaseError.toString());

            }
        });

        // TODO 14.1 Get a references to the firebase storage

        final ImageView imageViewBackground = findViewById(R.id.imageViewBackground);
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // TODO 14.2 add a onClickListener to imageViewBackground, so that when the image is click, download the image "background/background2.jpg" to
        //  the imageViewBackground
        imageViewBackground.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // StorageReference bgImgRef = storageRef.child("background/background2.jpg");
                // FireBaseUtils.downloadToImageView(MainActivity.this, bgImgRef, imageViewBackground);

                // TODO 14.3 modify the onClick method definition in 14.2 such that
                //  it will first list all the images stored under the "background" folder in the Firebase Storage
                //  given the list result, pick one image randomly from the list and download it to imageViewBackround
                Task<ListResult> taskListResult = storageRef.child("background").listAll();
                taskListResult.continueWithTask(new Continuation<ListResult, Task<byte[]>>() {
                    @Override
                    public Task<byte[]> then(@NonNull Task<ListResult> task) throws Exception {
                        ListResult listResult = task.getResult();
                        ArrayList<StorageReference> refs = new ArrayList<>(listResult.getItems());
                        Random r = new Random();
                        int p = r.nextInt(refs.size()-1);
                        StorageReference ref = refs.get(p);
                        return FireBaseUtils.downloadToImageView(MainActivity.this, ref, imageViewBackground);
                    }
                });


            }
        });

        FloatingActionButton uploadButton = findViewById(R.id.uploadButton);

        // TODO 14.4 Add an onClickListener to the uploadButton
        //  in which the onClick method creates an implicit intent ot image gallery (refer to lesson 4: Pokedex app)

        uploadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                }
            }
        });

    }


    //TODO 13.6 If you lose internet access, the data is lost upon the app starting up
    //  Hence, implementing sharedPreferences could be useful

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        String storedTallyValue = sharedPreferences.getString(SATISFIED_KEY,"0");
        textViewTally.setText(storedTallyValue);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(SATISFIED_KEY, Integer.toString(satisfiedTallyValue));
        preferencesEditor.apply();
    }


    // TODO 14.5 Define an onActivityResult method to retrieve the image selected and returned
    //  by the implicit intent. (refer to lesson 4: Pokedex app)
    //  Given the image, convert it to Bitmap. Call FireBaseUtils.uploadImageToStorage to upload
    //  it to the firebase storage
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            try {
                String filename = FireBaseUtils.getFileName(MainActivity.this, fullPhotoUri);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child("/background/" + filename);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fullPhotoUri);
                FireBaseUtils.uploadImageToStorage(MainActivity.this, imageRef, bitmap);
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, R.string.io_error, Toast.LENGTH_LONG);
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.file_not_found, Toast.LENGTH_LONG);
        }

    }



}