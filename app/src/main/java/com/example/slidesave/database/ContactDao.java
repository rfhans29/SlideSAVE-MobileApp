package com.example.slidesave.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY name ASC")
    List<Contact> getAllContacts(String userId);

    @Query("SELECT * FROM contacts WHERE id=:id LIMIT 1")
    Contact getContactById(int id);
}