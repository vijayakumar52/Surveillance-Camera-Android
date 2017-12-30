package org.opencv.samples.facedetect;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by vijay-3593 on 30/12/17.
 */

@Database(entities = {History.class}, version = 1)
public abstract class HistoryDatabase extends RoomDatabase {
    public abstract HistoryDao getHistoryDao();
}
