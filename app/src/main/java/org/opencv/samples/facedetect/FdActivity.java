package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.adsonik.surveillancecamera.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class FdActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    Bitmap bitmap;
    MediaPlayer mp;
    String file_path;
    File dir;
    FileOutputStream fOut;
    Calendar c;
    String id = null;
    int fCount = 0;
    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;

    private MenuItem mItemFace50;
    private MenuItem mItemFace40;
    private MenuItem mItemFace30;
    private MenuItem mItemFace20;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier cascadeClassifier;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("detection_based_tracker");

                    try {
                        InputStream is = getResources().openRawResource(R.raw.hogcascade_pedestrians);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "hogcascade_pedestrians.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);


                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1)
                        {
                            os.write(buffer, 0, bytesRead);
                            Log.d(TAG, "buffer: " + buffer.toString());
                        }
                        is.close();
                        os.close();
// Load the cascade classifier
                        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        cascadeClassifier.load(mCascadeFile.getAbsolutePath());
                        if (cascadeClassifier.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            cascadeClassifier = null;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
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

    public FdActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mp = MediaPlayer.create(FdActivity.this, R.raw.tone1);
        id = getIntent().getExtras().getString("radio");

        c = Calendar.getInstance();

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
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

            if (cascadeClassifier != null)
                cascadeClassifier.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());


        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {


            Rect r = facesArray[i];
            r.x += Math.abs(r.width * 0.1);
            r.width = (int) Math.abs(r.width * 0.8);
            r.y += Math.abs(r.height * 0.06);
            r.height = (int) Math.abs(r.height * 0.9);


            Imgproc.putText(mRgba, " " + (i + 1), new Point((r.tl().x + r.br().x) / 2, (facesArray[i].tl().y + facesArray[i].br().y) / 2), 3, 1, new Scalar(255, 0, 0), 2);

            Imgproc.rectangle(mRgba, r.tl(), r.br(), FACE_RECT_COLOR, 3);
        }
        Imgproc.putText(mRgba, "No. of people: " + facesArray.length, new Point(40, 40), 3, 1, new Scalar(133, 200, 13), 2);

        if (facesArray.length > 0) {

            if (id.contentEquals("first")) {
                if (!mp.isPlaying()) {
                    mp.start();
                }
            } else if (id.contentEquals("second")) {
                bitmap = Bitmap.createBitmap(mOpenCvCameraView.getWidth() / 4, mOpenCvCameraView.getHeight() / 4, Bitmap.Config.ARGB_8888);
                try {
                    bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mRgba, bitmap);
                    file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Surveillance Camera";
                    dir = new File(file_path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    int year_ = c.get(Calendar.YEAR);
                    int date_ = c.get(Calendar.DATE);
                    int minutes_ = c.get(Calendar.MINUTE);
                    int seconds = c.get(Calendar.SECOND);
                    File file = new File(dir, "screenshot" + "_" + year_ + "_" + date_ + "_" + minutes_ + "_" + String.valueOf(fCount++) + ".png");
                    fOut = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            } else if (id.contentEquals("third")) {
                if (!mp.isPlaying()) {
                    mp.start();
                }
                bitmap = Bitmap.createBitmap(mOpenCvCameraView.getWidth() / 4, mOpenCvCameraView.getHeight() / 4, Bitmap.Config.ARGB_8888);
                try {
                    bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mRgba, bitmap);
                    file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Surveillance Camera";
                    dir = new File(file_path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    int year_ = c.get(Calendar.YEAR);
                    int date_ = c.get(Calendar.DATE);
                    int minutes_ = c.get(Calendar.MINUTE);
                    int seconds = c.get(Calendar.SECOND);
                    File file = new File(dir, "screenshot" + "_" + year_ + "_" + date_ + "_" + minutes_ + "_" + String.valueOf(fCount++) + ".png");
                    fOut = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        return mRgba;

        //return inputFrame.rgba();
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
}
