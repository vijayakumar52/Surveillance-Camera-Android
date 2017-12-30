package org.opencv.samples.facedetect;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by vijay-3593 on 30/12/17.
 */

@Entity
public class History {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "createdTime")
    private Long createdTime;

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
