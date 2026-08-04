package com.example.slidesave.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {Contact.class},
        version = 2,
        exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
}