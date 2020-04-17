package com.example.hangout;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContextFragment extends Fragment {

    private View Contactsview;
    private RecyclerView mycontactslist;

    private DatabaseReference contactsref, usersref;
    private FirebaseAuth myauth;
    private String currentuserID;

    public ContextFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Contactsview= inflater.inflate(R.layout.fragment_context, container, false);
        mycontactslist= (RecyclerView) Contactsview.findViewById(R.id.contacts_list);
        mycontactslist.setLayoutManager(new LinearLayoutManager(getContext()));

        myauth = FirebaseAuth.getInstance();
        currentuserID = myauth.getCurrentUser().getUid();
        contactsref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserID);
        usersref= FirebaseDatabase.getInstance().getReference().child("Users");


        return Contactsview;
    }
    public static class contactsviewholder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        CircleImageView profileimage;
        ImageView onlineicon;

        public contactsviewholder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.userprofilename);
            userstatus=itemView.findViewById(R.id.userstatus);
            profileimage=itemView.findViewById(R.id.userprofileimage);
            onlineicon=(ImageView) itemView.findViewById(R.id.useronlinestatus);

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options= new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsref, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, contactsviewholder > adapter= new FirebaseRecyclerAdapter<Contacts, contactsviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsviewholder contactsviewholder, int i, @NonNull Contacts contacts) {

                String usersIds = getRef(i).getKey();
                usersref.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            if (dataSnapshot.child("userstate").hasChild("state")){
                                String state= dataSnapshot.child("userstate").child("state").getValue().toString();
                                String date= dataSnapshot.child("userstate").child("date").getValue().toString();
                                String time= dataSnapshot.child("userstate").child("time").getValue().toString();

                                if(state.equals("online")){
                                    contactsviewholder.onlineicon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline")){
                                    contactsviewholder.onlineicon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else{
                                contactsviewholder.onlineicon.setVisibility(View.INVISIBLE);
                            }


                            if(dataSnapshot.hasChild("Image")){
                                String userimage= dataSnapshot.child("Image").getValue().toString();
                                String profilename= dataSnapshot.child("name").getValue().toString();
                                String profilestatus= dataSnapshot.child("status").getValue().toString();

                                contactsviewholder.username.setText(profilename);
                                contactsviewholder.userstatus.setText(profilestatus);
                                Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(contactsviewholder.profileimage);

                            }
                            else{
                                String profilename= dataSnapshot.child("name").getValue().toString();
                                String profilestatus= dataSnapshot.child("status").getValue().toString();

                                contactsviewholder.username.setText(profilename);
                                contactsviewholder.userstatus.setText(profilestatus);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public contactsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diisplay_layout, parent, false);
                contactsviewholder viewholder = new contactsviewholder(view);
                return viewholder;
            }
        };

        mycontactslist.setAdapter(adapter);
        adapter.startListening();

    }


}
