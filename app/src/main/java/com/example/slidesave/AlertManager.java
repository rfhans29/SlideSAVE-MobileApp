package com.example.slidesave;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slidesave.database.Contact;
import com.example.slidesave.database.ContactDao;
import com.example.slidesave.database.DatabaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

    public class AlertManager {
        private Context context;
        private DatabaseReference alertRef;
        private String previousLevel = "";
        public AlertManager(Context context){
            this.context = context;
        }

    public void startListening(){
        alertRef = FirebaseDatabase.getInstance().getReference("Alert");

        alertRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String level =snapshot.child("alert_level").getValue(String.class);
                        Toast.makeText(context, "Alert Changed: " + level, Toast.LENGTH_SHORT).show();
                        String alertDateTime = snapshot.child("alert_dateTime").getValue(String.class);

                        if(level == null)
                            return;

                        if (!level.equals(previousLevel) && !level.equals("SAFE")) {
                            if (level.equals("WARNING") || level.equals("DANGER")) {
                                sendEmergencyAlert(level, alertDateTime);
                            }
                            previousLevel = level;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void sendEmergencyAlert(String level, String alertDateTime){
        ContactDao dao = DatabaseClient
                        .getInstance(context)
                        .getAppDatabase()
                        .contactDao();

        String userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        List<Contact> contacts = dao.getAllContacts(userId);
        Toast.makeText(context,"Contacts: " + contacts.size(), Toast.LENGTH_LONG).show();

        if(contacts.size() == 0){
            Toast.makeText(context, "No emergency contacts added", Toast.LENGTH_LONG).show();
            return;
        }

        for(Contact contact : contacts){
            String header;
            if (level.equals("WARNING")) {
                header = "⚠️ SlideSAVE LANDSLIDE WARNING ⚠️";
            } else {
                header = "🚨 SlideSAVE LANDSLIDE ALERT 🚨";
            }

            String message = header + "\n\nRisk Level: " + level + "\n\nDate & Time: " + alertDateTime + "\n\n";

            if(level.equals("WARNING")){
                message += "Potential landslide risk detected.\n\n" + "Please remain alert and monitor the situation.";
            }
            else{
                message += "A landslide risk has been detected.\n\n" + "Please contact the resident immediately and advise them to move to a safe location if necessary.";
            }

            message += "\n\nThis is an automated message from SlideSAVE.";
            TelegramSender.sendMessage(contact.getTelegramChatId(), message);
            Toast.makeText(context, "Alert sent to " + contact.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}