package org.opencv.samples.facedetect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.adsonik.surveillancecamera.R;
import com.greysonparrelli.permiso.Permiso;

public class Front_Page extends Activity implements OnCheckedChangeListener {
    RadioGroup rg;
    String name;
    Button btn1;
    TextView t1, t2, t3;
    RadioButton rb1, rb2, rb3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Permiso.getInstance().setActivity(this);

        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    setContentView(R.layout.front_page);
                    Typeface tf = Typeface.createFromAsset(Front_Page.this.getAssets(), "title.TTF");
                    t1 = (TextView) findViewById(R.id.textView1);
                    t2 = (TextView) findViewById(R.id.textView2);
                    t3 = (TextView) findViewById(R.id.textView3);

                    t1.setTypeface(tf);
                    t2.setTypeface(tf);
                    t3.setTypeface(tf);

                    rg = (RadioGroup) findViewById(R.id.radioGroup1);
                    rb1 = (RadioButton) findViewById(R.id.radio0);
                    rb2 = (RadioButton) findViewById(R.id.radio1);
                    rb3 = (RadioButton) findViewById(R.id.radio2);

                    rb1.setTypeface(tf);
                    rb2.setTypeface(tf);
                    rb3.setTypeface(tf);
                    rg.setOnCheckedChangeListener(Front_Page.this);
                    btn1 = (Button) findViewById(R.id.button1);
                    btn1.setTypeface(tf);
                    btn1.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            int id = rg.getCheckedRadioButtonId();
                            switch (id) {
                                case R.id.radio0:
                                    name = "first";
                                    break;
                                case R.id.radio1:
                                    name = "second";
                                    break;
                                case R.id.radio2:
                                    name = "third";
                                    break;
                            }
                            Intent i = new Intent(Front_Page.this, FdActivity.class);
                            i.putExtra("radio", name);
                            startActivity(i);
                        }
                    });
                } else {

                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                callback.onRationaleProvided();
            }
        }, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);


    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {

            case R.id.radio0:
                name = "first";
                break;
            case R.id.radio1:
                name = "second";
                break;
            case R.id.radio2:
                name = "third";
                break;
        }
    }


}
