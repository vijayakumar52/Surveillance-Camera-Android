package org.opencv.samples.facedetect

import android.Manifest
import android.arch.persistence.room.Room
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.CheckBox
import android.widget.Toast
import com.adsonik.surveillancecamera.R
import com.greysonparrelli.permiso.Permiso
import com.vijay.androidutils.ActivityHolder
import com.vijay.androidutils.PrefUtils

class MainActivity : AppCompatActivity() {
    private lateinit var database: HistoryDatabase;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.getInstance().activity = this;
        Permiso.getInstance().setActivity(this)
        database = Room.databaseBuilder(this, HistoryDatabase::class.java, DATABASE_NAME).build()
        setContentView(R.layout.activity_main)

        Permiso.getInstance().requestPermissions(object : Permiso.IOnPermissionResult {
            override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                if (resultSet.areAllPermissionsGranted()) {

                } else {
                    Toast.makeText(this@MainActivity, R.string.toast_app_wont_work, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onRationaleRequested(callback: Permiso.IOnRationaleProvided, vararg permissions: String) {
                callback.onRationaleProvided()
            }
        }, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)


        val alarmPref: Boolean = PrefUtils.getPrefValueBoolean(this, PREF_MAKE_ALARM)
        val makeAlarmCheckBox = findViewById<CheckBox>(R.id.cbMakeAlarm)
        makeAlarmCheckBox.isChecked = alarmPref

        DBloader().execute()
    }

    class DBloader : AsyncTask<Object, Object, Object?>() {
        override fun doInBackground(vararg params: Object?): Object? {
            val recyclerView = ActivityHolder.getInstance().activity.findViewById<RecyclerView>(R.id.rvIntruderHistory)
            val activity = ActivityHolder.getInstance().activity as MainActivity
            val database = activity.getDatabase()
            val allDatas = database.historyDao.all
            val historyAdapter = HistoryRecyclerViewAdapter(ActivityHolder.getInstance().activity, allDatas);
            recyclerView.adapter = historyAdapter;
            val hi : Object? = null
            return hi
        }

    }

    fun getDatabase(): HistoryDatabase {
        return database
    }

    companion object {
        val MAKE_ALARM = "makeAlarm"
        val PREF_MAKE_ALARM: String = "makeAlarm"
        val HISTORY: String = "history"
        val DATABASE_NAME = "historyDB"
    }

    override fun onResume() {
        super.onResume()
        Permiso.getInstance().setActivity(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults)
    }

}
