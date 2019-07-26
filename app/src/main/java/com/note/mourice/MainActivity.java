package com.note.mourice;
/**
 * Created by Mina Mourice on 7/22/2019.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int SIGNED_IN_REQUEST = 1;
    public static final int ADD_NOTE_REQUEST = 2;
    public static final int EDIT_NOTE_REQUEST = 3;
    private ArrayList<QueryDocumentSnapshot> noteIds = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private long backPressedTime;
    private RecyclerViewAdapter mNoteRecyclerViewAdapter;
    private ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        initFAB();
        if (user == null) {
            noteIds.clear();
            Log.d(TAG, "onCreate: Not signed in");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, SIGNED_IN_REQUEST);
            // For Test only
        }
        else {
            getNotesFireStore();
        }
        /*
        else {
            Log.d(TAG, "onCreate: Signed in");

            getNotesFireStore();
            //initNotes();
            //initRecyclerView();
        }*/
    }

    private void initFAB() {
        FloatingActionButton buttonAddNote = findViewById(R.id.fab_add);
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
                //Toast.makeText(MainActivity.this, "Add clicked",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SIGNED_IN_REQUEST): {
                if (resultCode == Activity.RESULT_OK) {
                    String userId = data.getStringExtra("userId");
                    //if(userId != null && !userId.isEmpty())
                    // TODO : get User Info from firestore
                    getNotesFireStore();
                }
                break;
            }
            case (ADD_NOTE_REQUEST): {
                if (resultCode == Activity.RESULT_OK) {
                    String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                    String content = data.getStringExtra(AddEditNoteActivity.EXTRA_CONTENT);
                    //noteIds.add(0,title);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Toast t = Toast.makeText(this, "Note Added", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.BOTTOM, 0, 50);
                    t.show();
                    // get timestamp
                    Long tsLong = System.currentTimeMillis() / 1000;

                    // add to database
                    Map<String, Object> notes = new HashMap<>();
                    notes.put("Title", title);
                    notes.put("Content", content);
                    notes.put("Timestamp", tsLong.toString());
                    // Add a new document with a generated ID
                    /*
                    db.collection("users").document(user.getUid()).collection("notes").add(notes)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.w(TAG, "notes successfully written!");
                                    getNotesFireStore();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing notes", e);

                                }
                            });
                    */
                    db.collection("users").document(user.getUid()).collection("notes").add(notes);
                    // Restart Activity
                    recreate();
                } else {
                    Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show();
                }
                break;

            }
            case (EDIT_NOTE_REQUEST): {
                if (resultCode == Activity.RESULT_OK) {
                    String id = data.getStringExtra(AddEditNoteActivity.EXTRA_ID);
                    String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                    String content = data.getStringExtra(AddEditNoteActivity.EXTRA_CONTENT);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    // TODO  : Update firebase

                    db.collection("users").document(user.getUid()).collection("notes").document(id).update("Title", title);
                    db.collection("users").document(user.getUid()).collection("notes").document(id).update("Content", content);
                    Toast t = Toast.makeText(this, "Note Edited", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.BOTTOM, 0, 50);
                    t.show();
                    //getNotesFireStore();
                    recreate();
                } else {
                    Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //2 seconds
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            //clear list
            noteIds.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, SIGNED_IN_REQUEST);
            //Sign out
            FirebaseAuth.getInstance().signOut();

        } else {
            Toast.makeText(this, "Press back again to sign out", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }



    private void getNotesFireStore() {
        progressDialog = new ProgressDialog(MainActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Getting your notes...");
        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").document(user.getUid()).collection("notes").orderBy("Timestamp")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "onComplete: get Notes Firestore");
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "onComplete: get Notes Firestore task success");
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (!noteIds.contains(document)) {
                                    noteIds.add(document);
                                }

                            }
                            initRecyclerView();
                            progressDialog.dismiss();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    private void initRecyclerView() {
        Log.d(TAG, "RecyclerView Init");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mNoteRecyclerViewAdapter = new RecyclerViewAdapter(noteIds, this);
        recyclerView.setAdapter(mNoteRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHeplerCallback).attachToRecyclerView(recyclerView);
    }

    // Swipe Right or left to delete
    ItemTouchHelper.SimpleCallback itemTouchHeplerCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void setDefaultSwipeDirs(int defaultSwipeDirs) {
            super.setDefaultSwipeDirs(defaultSwipeDirs);
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final QueryDocumentSnapshot to_be_deleted = noteIds.get(viewHolder.getAdapterPosition());
            final int to_be_deleted_position=viewHolder.getAdapterPosition();
            // remove from list
            noteIds.remove(viewHolder.getAdapterPosition());
            mNoteRecyclerViewAdapter.notifyDataSetChanged();


            // show snack bar
            Snackbar.make(findViewById(R.id.main_activity_layout), "Note deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            noteIds.add(to_be_deleted_position,to_be_deleted);
                            mNoteRecyclerViewAdapter.notifyDataSetChanged();

                        }
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {

                            if(event != Snackbar.Callback.DISMISS_EVENT_ACTION)
                            {
                                // remove from database
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                Log.d(TAG, "onDismissed: "+to_be_deleted.getId());
                                db.collection("users").document(user.getUid()).collection("notes").document(to_be_deleted.getId()).delete();
                            }
                        }


                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_blue_light
                    ))
                    .show();


        }
    };
}
