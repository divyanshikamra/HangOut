package com.example.hangout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class findFriendsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView findfriendsrecycler;
    private DatabaseReference usersref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersref= FirebaseDatabase.getInstance().getReference().child("Users");

        findfriendsrecycler= (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        findfriendsrecycler.setLayoutManager(new LinearLayoutManager(this));

        mtoolbar= (Toolbar) findViewById(R.id.findfriendstoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersref, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,findfriendviewholder> adapter = new FirebaseRecyclerAdapter<Contacts, findfriendviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findfriendviewholder holder, final int position, @NonNull Contacts model) {

                holder.username.setText(model.getName());
                holder.userstatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileimage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visituserid= getRef(position).getKey();

                        Intent profileintent = new Intent(findFriendsActivity.this, profileActivity.class);
                        profileintent.putExtra("visituserid", visituserid);
                        startActivity(profileintent);

                    }
                });

            }

            @NonNull
            @Override
            public findfriendviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diisplay_layout, parent, false);
                findfriendviewholder viewholder = new findfriendviewholder(view);
                return viewholder;

            }
        };
        findfriendsrecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class findfriendviewholder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        CircleImageView profileimage;


        public findfriendviewholder(@NonNull View itemView) {
            super(itemView);

            username= itemView.findViewById(R.id.userprofilename);
            userstatus= itemView.findViewById(R.id.userstatus);
            profileimage= itemView.findViewById(R.id.userprofileimage);
        }
    }
}
