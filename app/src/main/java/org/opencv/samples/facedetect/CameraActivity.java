package org.opencv.samples.facedetect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.adsonik.surveillancecamera.R;
import com.vijay.androidutils.ActivityHolder;
import com.vijay.androidutils.IOUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    MediaPlayer mediaPlayer;
    boolean makeAlarm = false;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private MenuItem mItemFace50;
    private MenuItem mItemFace40;
    private MenuItem mItemFace30;
    private MenuItem mItemFace20;

    private Mat mGray;
    private Mat mRgba;
    MatOfRect faces;

    private CascadeClassifier cascadeClassifier;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Handler handler = new Handler();
    private Runnable runnable;
    Rect[] facesArray;
    boolean timerCompleted = true;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    File mCascadeFile = IOUtils.getFileFromRaw(CameraActivity.this, "hog.xml", R.raw.hogcascade_pedestrians);
                    // Load the cascade classifier
                    cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    cascadeClassifier.load(mCascadeFile.getAbsolutePath());
                    if (cascadeClassifier.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        cascadeClassifier = null;
                    }

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        makeAlarm = getIntent().getBooleanExtra(MainActivity.Companion.getINTENT_EXTRA_MAKE_ALARM(), false);
        String toneUri = getIntent().getStringExtra(MainActivity.Companion.getINTENT_EXTRA_ALARM_URI());

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        if (!"".equals(toneUri)) {
            mediaPlayer = MediaPlayer.create(CameraActivity.this, R.raw.alarm);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        faces = new MatOfRect();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        faces.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        if (cascadeClassifier != null)
            cascadeClassifier.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());


        facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {

            Rect r = facesArray[i];
            r.x += Math.abs(r.width * 0.1);
            r.width = (int) Math.abs(r.width * 0.8);
            r.y += Math.abs(r.height * 0.06);
            r.height = (int) Math.abs(r.height * 0.9);


            Core.putText(mRgba, " " + (i + 1), new Point((r.tl().x + r.br().x) / 2, (facesArray[i].tl().y + facesArray[i].br().y) / 2), 3, 1, new Scalar(255, 0, 0), 2);

            Core.rectangle(mRgba, r.tl(), r.br(), FACE_RECT_COLOR, 3);
        }
        Core.putText(mRgba, "No. of people: " + facesArray.length, new Point(40, 40), 3, 1, new Scalar(133, 200, 13), 2);


        runnable = new Runnable() {
            @Override
            public void run() {
                timerCompleted = true;
            }
        };

        if (timerCompleted) {
            if (facesArray.length > 0) {
                new SaveTask(mRgba).execute();
            }
            timerCompleted = false;
            handler.postDelayed(runnable, 1000);
        }

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Human size 50%");
        mItemFace40 = menu.add("Human size 40%");
        mItemFace30 = menu.add("Human size 30%");
        mItemFace20 = menu.add("Human size 20%");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        return super.onOptionsItemSelected(item);
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    class SaveTask extends AsyncTask<String, Integer, String> {
        Mat mRgba;

        public SaveTask(Mat mRgba) {
            this.mRgba = mRgba.clone();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRgba, bitmap);
                String folderPath = HistoryRecyclerViewAdapter.getHistoryPath(CameraActivity.this);
                long fileName = System.currentTimeMillis();

                //Add in DB
                MainActivity activity = (MainActivity) ActivityHolder.getInstance().getActivity();
                History history = new History();
                history.setCreatedTime(fileName);
                activity.getViewModel().addItem(history);

                //Add in file store
                File newFile = new File(folderPath + File.separator + fileName + ".jpeg");
                FileOutputStream fOut = new FileOutputStream(newFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.mRgba.release();
        }
    }
}
