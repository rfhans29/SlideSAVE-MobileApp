package com.example.slidesave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slidesave.database.Contact;
import com.example.slidesave.database.ContactDao;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{
    private List<Contact> contactList;
    private ContactDao contactDao;
    private Context context;

    public ContactAdapter(List<Contact> contactList, ContactDao contactDao, Context context){
        this.contactList = contactList;
        this.contactDao = contactDao;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.contact_item,
                                parent,
                                false
                        );
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nameText.setText(contact.getName());
        holder.relationshipText.setText(contact.getRelationship());
        holder.telegramText.setText(contact.getTelegramChatId());
        holder.editButton.setOnClickListener(v -> {
            ContactActivity activity = (ContactActivity) context;

            activity.nameInput.setText(contact.getName());
            activity.relationshipInput.setText(contact.getRelationship());
            activity.telegramInput.setText(contact.getTelegramChatId());
            activity.selectedContact = contact;
            activity.addButton.setText("Update Contact");
        });

        holder.deleteButton.setOnClickListener(v -> {
            contactDao.delete(contact);
            contactList.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView relationshipText;
        TextView telegramText;
        Button deleteButton;
        Button editButton;

        public ContactViewHolder(
                @NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.contactName);
            relationshipText = itemView.findViewById(R.id.contactRelationship);
            telegramText = itemView.findViewById(R.id.contactTelegram);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}