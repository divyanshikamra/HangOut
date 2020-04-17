package com.example.hangout;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ChatsFragment extends Fragment {

    private View privatechatsview;
    private RecyclerView chatslist;

    private FirebaseAuth myauth;
    private DatabaseReference chatsref,usersref;
    private  String currentuserId;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatechatsview = inflater.inflate(R.layout.fragment_chats, container, false);

        myauth= FirebaseAuth.getInstance();
        currentuserId= myauth.getCurrentUser().getUid();
        chatsref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserId);
        usersref= FirebaseDatabase.getInstance().getReference().child("Users");

        chatslist= (RecyclerView) privatechatsview.findViewById(R.id.chats_list);
        chatslist.setLayoutManager(new LinearLayoutManager(getContext()));

        return privatechatsview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsref, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,chatsviewholder> adapter=
                new FirebaseRecyclerAdapter<Contacts, chatsviewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsviewholder chatsviewholder, int i, @NonNull Contacts contacts) {

                        final String userids= getRef(i).getKey();
                        final String[] retimage = {"default_image"};

                        usersref.child(userids).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){

                                    if(dataSnapshot.hasChild("Image")){
                                        retimage[0] = dataSnapshot.child("Image").getValue().toString();
                                        Picasso.get().load(retimage[0]).into(chatsviewholder.profileimage);
                                    }

                                    final String retname = dataSnapshot.child("name").getValue().toString();
                                    final String retstatus = dataSnapshot.child("status").getValue().toString();

                                    chatsviewholder.username.setText(retname);


                                    if (dataSnapshot.child("userstate").hasChild("state")){
                                        String state= dataSnapshot.child("userstate").child("state").getValue().toString();
                                        String date= dataSnapshot.child("userstate").child("date").getValue().toString();
                                        String time= dataSnapshot.child("userstate").child("time").getValue().toString();

                                        if(state.equals("online")){
                                            chatsviewholder.userstatus.setText("online");
                                        }
                                        else if (state.equals("offline")){
                                            chatsviewholder.userstatus.setText("Last seen: "  + date +"   " +time);
                                        }
                                    }
                                    else{
                                        chatsviewholder.userstatus.setText("offline");
                                    }

                                    chatsviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatintent = new Intent(getContext(), ChatActivity.class);
                                            chatintent.putExtra("visit_user_id",userids);
                                            chatintent.putExtra("visit_user_name",retname);
                                            chatintent.putExtra("visit_image", retimage[0]);
                                            startActivity(chatintent);
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public chatsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diisplay_layout, parent, false);
                        return  new chatsviewholder(view);

                    }
                };

        chatslist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatsviewholder extends RecyclerView.ViewHolder{

        CircleImageView profileimage;
        TextView username,userstatus;


        public chatsviewholder(@NonNull View itemView) {
            super(itemView);

            profileimage= itemView.findViewById(R.id.userprofileimage);
            username= itemView.findViewById(R.id.userprofilename);
            userstatus= itemView.findViewById(R.id.userstatus);


        }
    }

}
