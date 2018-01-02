package org.opencv.samples.facedetect

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.*
import com.adsonik.surveillancecamera.R
import com.greysonparrelli.permiso.Permiso
import com.vijay.androidutils.*
import java.io.File


class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null;
    private lateinit var viewModel: HistoryListViewModel;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.getInstance().activity = this
        Permiso.getInstance().setActivity(this)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(HistoryListViewModel::class.java);

        //Checkbox behaviour
        val alarmPrefValue: Boolean = PrefUtils.getPrefValueBoolean(this, PREF_MAKE_ALARM)
        val makeAlarmCheckBox = findViewById<CheckBox>(R.id.cbMakeAlarm)
        makeAlarmCheckBox.isChecked = alarmPrefValue
        makeAlarmCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            PrefUtils.setPrefValueBoolean(this@MainActivity, PREF_MAKE_ALARM, isChecked)
        }

        validateAlarmText()


        val tvAlarmPlay = findViewById<ImageView>(R.id.tvAlarmTonePlay)
        tvAlarmPlay.setOnClickListener {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                tvAlarmPlay.setImageDrawable(resources.getDrawable(R.drawable.play))
            } else {
                val alarmToneUriPrefValueUpdated = PrefUtils.getPrefValueString(this, PREF_ALARM_TONE_URI)
                mediaPlayer = MediaPlayer.create(this@MainActivity, Uri.parse(alarmToneUriPrefValueUpdated))
                mediaPlayer!!.start()
                mediaPlayer!!.setOnCompletionListener {
                    tvAlarmPlay.setImageDrawable(resources.getDrawable(R.drawable.play))
                }
                tvAlarmPlay.setImageDrawable(resources.getDrawable(R.drawable.pause))
            }
        }

        val changeAlarm = findViewById<TextView>(R.id.tvChangeAlarm)
        changeAlarm.setOnClickListener {
            Permiso.getInstance().requestPermissions(object : Permiso.IOnPermissionResult {
                override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                    if (resultSet.areAllPermissionsGranted()) {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "audio/*"
                        startActivityForResult(Intent.createChooser(intent, "Music File"), CHOOSE_TONE_CALLBACK)
                    } else {
                        Toast.makeText(this@MainActivity, R.string.toast_app_wont_work, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onRationaleRequested(callback: Permiso.IOnRationaleProvided, vararg permissions: String) {
                    callback.onRationaleProvided()
                }
            }, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val startMonitoring = findViewById<Button>(R.id.btnStartMonitoring)
        startMonitoring.setOnClickListener {
            Permiso.getInstance().requestPermissions(object : Permiso.IOnPermissionResult {
                override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                    if (resultSet.isPermissionGranted(Manifest.permission.CAMERA)) {
                        val makeAlarm = PrefUtils.getPrefValueBoolean(this@MainActivity, PREF_MAKE_ALARM)
                        val alarmToneUri = PrefUtils.getPrefValueString(this@MainActivity, PREF_ALARM_TONE_URI)

                        val cameraActivity = Intent(this@MainActivity, CameraActivity::class.java)
                        cameraActivity.putExtra(INTENT_EXTRA_MAKE_ALARM, makeAlarm)
                        cameraActivity.putExtra(INTENT_EXTRA_ALARM_URI, alarmToneUri)
                        startActivity(cameraActivity)
                    } else {
                        Toast.makeText(this@MainActivity, R.string.toast_app_wont_work, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onRationaleRequested(callback: Permiso.IOnRationaleProvided, vararg permissions: String) {
                    callback.onRationaleProvided()
                }
            }, Manifest.permission.CAMERA)
        }

        val tvClear = findViewById<TextView>(R.id.tvClear);
        tvClear.setOnClickListener {
            viewModel.deleteAllItems(this)
        }
        val gridLayoutManager = GridLayoutManager(this, 3)
        val recyclerView = findViewById<RecyclerView>(R.id.rvIntruderHistory)
        recyclerView.layoutManager = gridLayoutManager

        viewModel.allItems.observe(this, Observer { histories ->
            if(recyclerView.adapter == null) {
                val historyAdapter = HistoryRecyclerViewAdapter(this@MainActivity, histories)
                recyclerView.adapter = historyAdapter
            }else{
                val historyAdapter = recyclerView.adapter as HistoryRecyclerViewAdapter
                historyAdapter.notifyDataChanged(histories)
            }

        })
    }

    /* fun isPagerShowing(): Boolean {
         val manager = supportFragmentManager
         val backStackCount = manager.backStackEntryCount
         if (backStackCount > 0) {
             if ("pagerFragment".equals(manager.getBackStackEntryAt(0).name)) {
                 return true
             }
         }
         return false;
     }*/

    fun getViewModel(): HistoryListViewModel {
        return viewModel;
    }

    fun validateAlarmText() {

        //Alarm tone text behaviour
        val alarmToneUriPrefValue = PrefUtils.getPrefValueString(this, PREF_ALARM_TONE_URI)
        if ("".equals(alarmToneUriPrefValue)) {
            val newFile = File(filesDir, "Alarm default.mp3")
            if (!newFile.exists()) {
                newFile.createNewFile()
            }
            IOUtils.getFileFromRaw(this@MainActivity, newFile, R.raw.alarm)
            PrefUtils.setPrefValueString(this, PREF_ALARM_TONE_URI, Uri.fromFile(newFile).toString())
        }
        val alarmToneUriPrefValueUpdated = PrefUtils.getPrefValueString(this, PREF_ALARM_TONE_URI)
        val tvAlarmTone = findViewById<TextView>(R.id.tvAlarmToneText)
        tvAlarmTone.text = FileUtils.getFileName(Uri.parse(alarmToneUriPrefValueUpdated).path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_TONE_CALLBACK) {
            if (resultCode == Activity.RESULT_OK) {
                val file = URIUtils.getFileFromUri(this@MainActivity, data?.data)
                if (file != null) {
                    val fileUri = Uri.fromFile(file)
                    PrefUtils.setPrefValueString(this@MainActivity, PREF_ALARM_TONE_URI, fileUri.toString())
                    validateAlarmText()
                }
            }
        }
    }

    companion object {
        val PREF_MAKE_ALARM: String = "makeAlarm"
        val PREF_ALARM_TONE_URI = "alarmToneUri"
        val HISTORY: String = "history"
        val DATABASE_NAME = "historyDB"
        val CHOOSE_TONE_CALLBACK = 1010

        val INTENT_EXTRA_MAKE_ALARM = "makeAlarm"
        val INTENT_EXTRA_ALARM_URI = "alarmUri"
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
