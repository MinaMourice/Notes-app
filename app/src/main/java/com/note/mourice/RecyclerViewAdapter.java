package com.note.mourice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mina Mourice on 7/24/2019.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<QueryDocumentSnapshot> mNoteIds=new ArrayList<>();
    //private ArrayList<String> mNoteContents=new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<QueryDocumentSnapshot> mNoteId, Context mContext) {
        this.mNoteIds = mNoteId;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // This is called when an item is added to list
        Log.d(TAG,"onBindViewHolder: called");

        holder.image.setImageResource(R.drawable.note);
        holder.noteName.setText(mNoteIds.get(position).getString("Title"));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Log.d(TAG,"onClick: clicked on:"+mNoteIds.get(position));
            //Toast.makeText(mContext,mNoteIds.get(position).getString("Title"), Toast.LENGTH_SHORT).show();
            String Id=mNoteIds.get(position).getId();
            String Title = mNoteIds.get(position).getString("Title");
            String Content = mNoteIds.get(position).getString("Content");
            Intent intent = new Intent(mContext,AddEditNoteActivity.class);
            intent.putExtra(AddEditNoteActivity.EXTRA_ID,Id);
            intent.putExtra(AddEditNoteActivity.EXTRA_TITLE,Title);
            intent.putExtra(AddEditNoteActivity.EXTRA_CONTENT,Content);
            ((Activity) mContext).startActivityForResult
                    (intent,MainActivity.EDIT_NOTE_REQUEST);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mNoteIds.size();
    }

    // Hold List item vieew
    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image ;
        TextView noteName;
        RelativeLayout parentLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.image);
            noteName=itemView.findViewById(R.id.note_name);
            parentLayout=itemView.findViewById(R.id.parent_layout);

        }
    }


}
