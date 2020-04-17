package com.example.hangout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestsFragment extends Fragment {

    private View requestsfragmentview;
    private RecyclerView myrequestslist;

    private FirebaseAuth myauth;
    private DatabaseReference chatrequestsref,usersref, contactsref;
    private String currentuserId;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsfragmentview= inflater.inflate(R.layout.fragment_requests, container, false);

        myauth= FirebaseAuth.getInstance();
        currentuserId= myauth.getCurrentUser().getUid();
        chatrequestsref = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsref=FirebaseDatabase.getInstance().getReference().child("Contacts");

        myrequestslist= (RecyclerView) requestsfragmentview.findViewById(R.id.chat_requests_list);
        myrequestslist.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestsfragmentview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatrequestsref.child(currentuserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, requestsviewholder> adapter =
                new FirebaseRecyclerAdapter<Contacts, requestsviewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final requestsviewholder requestsviewholder, int i, @NonNull Contacts contacts) {

                        requestsviewholder.itemView.findViewById(R.id.requestsacceptbutton).setVisibility(View.VISIBLE);
                        requestsviewholder.itemView.findViewById(R.id.requestscancelbutton).setVisibility(View.INVISIBLE);

                        final String listuserid= getRef(i).getKey();

                        DatabaseReference gettyperef = getRef(i).child("request_type").getRef();

                        gettyperef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){
                                    String type= dataSnapshot.getValue().toString();
                                    if(type.equals("recieved")){

                                        usersref.child(listuserid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Image")){
                                                    final String requestprofileimage = dataSnapshot.child("Image").getValue().toString();

                                                    Picasso.get().load(requestprofileimage).into(requestsviewholder.profileimage);

                                                }
                                                final String requestusername = dataSnapshot.child("name").getValue().toString();
                                                final String requestuserstatus = dataSnapshot.child("status").getValue().toString();

                                                requestsviewholder.username.setText(requestusername);
                                                requestsviewholder.userstatus.setText("wants to connect with you.");

                                                requestsviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestusername + " Message Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if (which==0){
                                                                    contactsref.child(currentuserId).child(listuserid).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                contactsref.child(listuserid).child(currentuserId).child("Contact")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            chatrequestsref.child(currentuserId).child(listuserid)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                chatrequestsref.child(listuserid).child(currentuserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    Toast.makeText(getContext(), "New friend added ", Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                                if(which==1){

                                                                    chatrequestsref.child(currentuserId).child(listuserid)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        chatrequestsref.child(listuserid).child(currentuserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Toast.makeText(getContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    else if (type.equals("sent")){

                                        Button request_sent_button= requestsviewholder.itemView.findViewById(R.id.requestsacceptbutton);
                                        request_sent_button.setText("Request Sent!");

                                        requestsviewholder.itemView.findViewById(R.id.requestscancelbutton).setVisibility(View.INVISIBLE);

                                        usersref.child(listuserid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Image")){
                                                    final String requestprofileimage = dataSnapshot.child("Image").getValue().toString();

                                                    Picasso.get().load(requestprofileimage).into(requestsviewholder.profileimage);

                                                }
                                                final String requestusername = dataSnapshot.child("name").getValue().toString();
                                                final String requestuserstatus = dataSnapshot.child("status").getValue().toString();

                                                requestsviewholder.username.setText(requestusername);
                                                requestsviewholder.userstatus.setText("you have sent a request to" + requestusername);

                                                requestsviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already sent request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if(which==0){

                                                                    chatrequestsref.child(currentuserId).child(listuserid)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        chatrequestsref.child(listuserid).child(currentuserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Toast.makeText(getContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

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
                    public requestsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diisplay_layout, parent, false);
                        requestsviewholder holder = new requestsviewholder(view);
                        return holder;
                    }
                };

        myrequestslist.setAdapter(adapter);
        adapter.startListening();

    }

    public static class requestsviewholder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        CircleImageView profileimage;
        Button acceptbutton, cancelbutton;

        public requestsviewholder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.userprofilename);
            userstatus=itemView.findViewById(R.id.userstatus);
            profileimage=itemView.findViewById(R.id.userprofileimage);
            acceptbutton=itemView.findViewById(R.id.requestsacceptbutton);
            cancelbutton=itemView.findViewById(R.id.requestscancelbutton);

        }
    }

}
