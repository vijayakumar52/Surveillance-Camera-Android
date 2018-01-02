package org.opencv.samples.facedetect;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.vijay.androidutils.FileUtils;

import java.io.File;
import java.util.List;

public class HistoryListViewModel extends AndroidViewModel {
    public final LiveData<List<History>> allItems;
    private HistoryDatabase appDatabase;

    public HistoryListViewModel(Application application) {
        super(application);
        appDatabase = HistoryDatabase.getDatabase(this.getApplication());
        allItems = appDatabase.getHistoryDao().getAll();
    }


    public LiveData<List<History>> getAllItems() {
        return allItems;
    }

    public void deleteItem(Context context, History borrowModel) {
        File file = new File(HistoryRecyclerViewAdapter.getHistoryPath(context), borrowModel.getCreatedTime() + ".jpeg");
        FileUtils.deleteFile(file.getPath());

        new deleteTask(appDatabase).execute(borrowModel);
    }

    public void addItem(History history) {
        new InsertTask(appDatabase).execute(history);
    }

    public void deleteAllItems(Context context) {
        File file = new File(HistoryRecyclerViewAdapter.getHistoryPath(context));
        FileUtils.deleteFile(file.getPath());
        new deleteAllTask(appDatabase).execute();
    }

    private static class deleteTask extends AsyncTask<History, Void, Void> {

        private HistoryDatabase db;

        deleteTask(HistoryDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final History... params) {
            db.getHistoryDao().delete(params[0]);
            return null;
        }

    }

    private static class deleteAllTask extends AsyncTask<History, Void, Void> {

        private HistoryDatabase db;

        deleteAllTask(HistoryDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final History... params) {
            db.getHistoryDao().nukeTable();
            return null;
        }
    }

    private static class InsertTask extends AsyncTask<History, Void, Void> {

        private HistoryDatabase db;

        InsertTask(HistoryDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final History... params) {
            db.getHistoryDao().insert(params[0]);
            return null;
        }
    }

}