package org.opencv.samples.facedetect;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by vijay-3593 on 30/12/17.
 */

@Database(entities = {History.class}, version = 1)
public abstract class HistoryDatabase extends RoomDatabase {

    private static HistoryDatabase INSTANCE;

    public static HistoryDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), HistoryDatabase.class, "historyDB")
                            .build();
        }
        return INSTANCE;
    }

    public abstract HistoryDao getHistoryDao();
}
