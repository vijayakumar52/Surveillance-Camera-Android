package org.opencv.samples.facedetect;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by vijay-3593 on 30/12/17.
 */

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM history")
    LiveData<List<History>> getAll();

    @Insert(onConflict = REPLACE)
    void insert(History history);

    @Delete
    void delete(History history);

    @Query("DELETE FROM history")
    void nukeTable();

}
