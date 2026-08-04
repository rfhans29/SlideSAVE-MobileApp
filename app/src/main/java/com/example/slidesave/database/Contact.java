package com.example.slidesave.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

//SQLite database table 
@Entity(tableName = "contacts")
public class Contact {
    //Auto generate the unique id for each contact
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String relationship;
    private String telegramChatId;
    private String userId;

    public Contact() {
    }

    @Ignore
    public Contact(String name, String relationship, String telegramChatId, String userId) {
        this.name = name;
        this.relationship = relationship;
        this.telegramChatId = telegramChatId;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getRelationship() {
        return relationship;
    }
    public String getTelegramChatId() {
        return telegramChatId;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}