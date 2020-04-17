package com.example.hangout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageslist;
    private FirebaseAuth myauth;
    private DatabaseReference usersref;

    public MessageAdapter(List<Messages> userMessageslist){
        this.userMessageslist= userMessageslist;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView sendermessagetext,recievermessagetext;
        public CircleImageView recieverprofileimage;
        public ImageView messagesenderpicture,messagerecieverpicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sendermessagetext= (TextView) itemView.findViewById(R.id.send_message_text);
            recievermessagetext= (TextView) itemView.findViewById(R.id.reciever_message_text);
            recieverprofileimage= (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messagerecieverpicture=itemView.findViewById(R.id.messagerecieverimageview);
            messagesenderpicture=itemView.findViewById(R.id.messagesenderimageview);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent,false);
        myauth= FirebaseAuth.getInstance();

        return  new MessageViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messagesenderid = myauth.getCurrentUser().getUid();
        Messages messages = userMessageslist.get(position);

        String fromuserid = messages.getFrom();
        String frommessagetype = messages.getType();

        usersref= FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);

        usersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Image")){
                    String recieverimage = dataSnapshot.child("Image").getValue().toString();
                    Picasso.get().load(recieverimage).placeholder(R.drawable.profile_image).into( holder.recieverprofileimage);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.messagesenderpicture.setVisibility(View.GONE);
        holder.messagerecieverpicture.setVisibility(View.GONE);
        holder.recievermessagetext.setVisibility(View.GONE);
        holder.recieverprofileimage.setVisibility(View.GONE);
        holder.sendermessagetext.setVisibility(View.GONE);

        if(frommessagetype.equals("text")){

            if(fromuserid.equals(messagesenderid)){
                holder.sendermessagetext.setVisibility(View.VISIBLE);

                holder.sendermessagetext.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.sendermessagetext.setTextColor(Color.BLACK);
                holder.sendermessagetext.setText(messages.getMessage()+ "\n \n"+messages.getTime()+ " - "+ messages.getDate());
            }
            else{


                holder.recieverprofileimage.setVisibility(View.VISIBLE);
                holder.recievermessagetext.setVisibility(View.VISIBLE);

                holder.recievermessagetext.setBackgroundResource(R.drawable.reciever_messages_layout);
                holder.recievermessagetext.setTextColor(Color.BLACK);
                holder.recievermessagetext.setText(messages.getMessage()+ "\n \n"+messages.getTime()+ " - "+ messages.getDate());

            }

        }
        else if(frommessagetype.equals("image")){

            if (fromuserid.equals(messagesenderid)){

                holder.messagesenderpicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messagesenderpicture);

            }
            else{
                holder.recieverprofileimage.setVisibility(View.VISIBLE);
                holder.messagerecieverpicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messagerecieverpicture);

            }

        }
        else if(frommessagetype.equals("pdf") || frommessagetype.equals("docx")){
            if (fromuserid.equals(messagesenderid)){

                holder.messagesenderpicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/hangout-ebee2.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=92e993f6-5cdd-4649-be89-e7a24cd383e3")
                        .into(holder.messagesenderpicture);

               holder.itemView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageslist.get(position).getMessage()));
                       holder.itemView.getContext().startActivity(intent);
                   }
               });

            }
            else {
                holder.recieverprofileimage.setVisibility(View.VISIBLE);
                holder.messagerecieverpicture.setVisibility(View.VISIBLE);

                holder.messagerecieverpicture.setBackgroundResource(R.drawable.file);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageslist.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }

        }


    }




    @Override
    public int getItemCount() {
        return userMessageslist.size();
    }



}
