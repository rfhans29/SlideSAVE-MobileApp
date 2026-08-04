package com.example.slidesave.database;

import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {
    private static DatabaseClient instance;
    private final AppDatabase appDatabase;

    private DatabaseClient(Context context) {
        appDatabase =
                Room.databaseBuilder(context, AppDatabase.class, "slidesave_contacts")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if(instance == null){
            instance = new DatabaseClient(context);
        }
        return instance;
    }
    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}