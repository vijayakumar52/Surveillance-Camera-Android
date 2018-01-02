package org.opencv.samples.facedetect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adsonik.surveillancecamera.R;
import com.vijay.androidutils.ActivityHolder;
import com.vijay.androidutils.IOUtils;
import com.vijay.androidutils.Logger;
import com.vijay.androidutils.PrefUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    MediaPlayer mediaPlayer;
    boolean makeAlarm = false;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private Mat mGray;
    private Mat mRgba;

    private MatOfRect peoples;
    private MatOfDouble mMargins;

    private CascadeClassifier faceClassifier;
    private CascadeClassifier upperBodyClassifier;
    private CascadeClassifier fullBodyClassifier;
    private HOGDescriptor hogDescriptor;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Handler handler = new Handler();
    private Runnable runnable;
    Rect[] peopleSize;
    boolean timerCompleted = true;
    String toneUri;
    TextView countView;
    private String PREF_CLOSE = "closeBtnState";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    //Initializing face cascade classifier
                    File faceFile = IOUtils.getFileFromRaw(CameraActivity.this, "faceClassifier.xml", R.raw.lbpcascade_frontalface);
                    faceClassifier = new CascadeClassifier(faceFile.getAbsolutePath());
                    faceClassifier.load(faceFile.getAbsolutePath());
                    if (faceClassifier.empty()) {
                        Log.e(TAG, "Failed to load face classifier");
                        faceClassifier = null;
                    }

                    //Initializing upper body cascade classifier
                    File upperBody = IOUtils.getFileFromRaw(CameraActivity.this, "upperBody.xml", R.raw.haarcascade_upperbody);
                    upperBodyClassifier = new CascadeClassifier(upperBody.getAbsolutePath());
                    upperBodyClassifier.load(upperBody.getAbsolutePath());
                    if (upperBodyClassifier.empty()) {
                        Log.e(TAG, "Failed to load upperbody classifier");
                        upperBodyClassifier = null;
                    }


                    //Initializing full body cascade classifier
                    File fullBody = IOUtils.getFileFromRaw(CameraActivity.this, "fullBody.xml", R.raw.hogcascade_pedestrians);
                    fullBodyClassifier = new CascadeClassifier(fullBody.getAbsolutePath());
                    fullBodyClassifier.load(fullBody.getAbsolutePath());
                    if (fullBodyClassifier.empty()) {
                        Log.e(TAG, "Failed to load fullbody classifier");
                        fullBodyClassifier = null;
                    }

                    //Initializing HOG Descriptor
                    hogDescriptor = new HOGDescriptor();
                    hogDescriptor.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

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
        toneUri = getIntent().getStringExtra(MainActivity.Companion.getINTENT_EXTRA_ALARM_URI());

        setContentView(R.layout.face_detect_surface_view);
        countView = findViewById(R.id.people_count);

        RelativeLayout closeLayout = findViewById(R.id.closeLayout);
        boolean closeStatus = PrefUtils.getPrefValueBoolean(this, PREF_CLOSE);
        if(!closeStatus){
            closeLayout.setVisibility(View.VISIBLE);
            ImageView closeBtn = findViewById(R.id.closeBtn);
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrefUtils.setPrefValueBoolean(CameraActivity.this, PREF_CLOSE, true);
                    RelativeLayout closeLayout = findViewById(R.id.closeLayout);
                    closeLayout.setVisibility(View.GONE);
                }
            });
        }else{
            closeLayout.setVisibility(View.GONE);
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setCvCameraViewListener(this);
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
        peoples = new MatOfRect();
        mMargins = new MatOfDouble();
        mRgba = new Mat();
        if (!"".equals(toneUri)) {
            mediaPlayer = MediaPlayer.create(CameraActivity.this, Uri.parse(toneUri));
        }
    }

    public void onCameraViewStopped() {
        mGray.release();
        peoples.release();
        mRgba.release();
        mMargins.release();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //Detecting faces;
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        Logger.d(TAG, "Detecting face...");
        if (faceClassifier != null) {
            faceClassifier.detectMultiScale(mGray, peoples, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        peopleSize = peoples.toArray();
        if (peopleSize.length == 0) {
            Logger.d(TAG, "Face not found.");
            Logger.d(TAG, "Detecting upper body...");
            if (upperBodyClassifier != null) {
                upperBodyClassifier.detectMultiScale(mGray, peoples, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            }

            peopleSize = peoples.toArray();
            if (peopleSize.length == 0) {
                Logger.d(TAG, "Upper body not found.");
                Logger.d(TAG, "Detecting full body with cascade...");
                if (fullBodyClassifier != null) {
                    fullBodyClassifier.detectMultiScale(mGray, peoples, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                }

               /* peopleSize = peoples.toArray();
                if (peopleSize.length == 0) {
                    Logger.d(TAG, "Detecting full body with cascade not found.");
                    Logger.d(TAG, "Detecting full body with HOG...");
                    hogDescriptor.detectMultiScale(mGray, peoples, mMargins, 0, new Size(8, 8), new Size(32, 32), 1.05, 2, false);
                }*/
            }
        }


        drawPoeples(mRgba, peopleSize);

        runnable = new Runnable() {
            @Override
            public void run() {
                timerCompleted = true;
            }
        };

        if (timerCompleted) {
            if (peopleSize.length > 0) {
                new SaveTask(mRgba).execute();
                if (makeAlarm) {
                    mediaPlayer.start();
                }
            }
            timerCompleted = false;
            handler.postDelayed(runnable, 1000);
        }

        return mRgba;
    }

    private void drawPoeples(Mat mRgba, final Rect[] peopleArray) {
        for (int i = 0; i < peopleArray.length; i++) {

            Rect r = peopleArray[i];
            r.x += Math.abs(r.width * 0.1);
            r.width = (int) Math.abs(r.width * 0.8);
            r.y += Math.abs(r.height * 0.06);
            r.height = (int) Math.abs(r.height * 0.9);


            Core.putText(mRgba, " " + (i + 1), new Point((r.tl().x + r.br().x) / 2, (peopleArray[i].tl().y + peopleArray[i].br().y) / 2), 3, 1, new Scalar(255, 0, 0), 2);

            Core.rectangle(mRgba, r.tl(), r.br(), FACE_RECT_COLOR, 3);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countView.setText(getResources().getString(R.string.ui_no_of_people) + " " + peopleArray.length);
            }
        });

        //Core.putText(mRgba, "No. of people: " + peopleArray.length, new Point(20, 20), 3, 1, new Scalar(133, 200, 13), 2);
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
