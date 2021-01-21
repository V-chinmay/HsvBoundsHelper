package com.sirena.patchdetectorhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import  org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    String HSVUL_KEY = "hsvULbounds";

    Size k_Size = new Size(0,0);

    int blurThreshold = 50;



    TextView blurDisp;

    SeekBar blurSeekBar;


    File configDir ;

    final int reqCode =9898;




    private final String TAG = this.toString();
    String[] lowHsvUpperHsv = new String[]{"Lower Hue" ,"Lower Saturation", "Lower Value","Upper Hue" ,"Upper Saturation", "Upper Value"};
    int[] hsvULMax = new int[]{179,255,255,179,255,255};
    int[] hsvULBounds = new int[lowHsvUpperHsv.length];
    int[][] defaultHSVBounds =  new int[][]{ {0,123,122,8,255,255} , {55,109,69,72,255,255} , {82,92,65,126,255,255} , {26,122,113,100,255,255} };
    RecyclerView hsvRecycler;
    Spinner colorSpinner;
    hsvRecyAdap hsvRecyAdap;
    private CameraBridgeViewBase mOpenCvCameraView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==reqCode)
        {
            for(int i=0;i<permissions.length;i++)
            {
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED)
                {
                    Log.i(TAG, "onRequestPermissionsResult: Permission granted for " + permissions[i]);
                }
                else
                {
                    Log.e(TAG, "onRequestPermissionsResult: Permission denied for " + permissions[i]);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.toolbardone)
        {
            onToolbarDoneSelected();
        }
        return super.onOptionsItemSelected(item);
    }

    public  void onToolbarDoneSelected()
    {


        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configDir+File.separator+"hsvBounds.config.txt");
            fileOutputStream.write("hello".getBytes());
            fileOutputStream.close();
            Log.i(TAG, "onToolbarDoneSelected: Config  saved");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "onToolbarDoneSelected: Failed to write to config file");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        **will implement this now**
//        HashMap<String,ArrayList<HashMap<String,int[]>>> toBeJson = new HashMap<String,ArrayList<HashMap<String,int[]>>>();
//
//        HashMap<String,int[]> redHsvBounds = new HashMap<String, int[]>();
//        HashMap<String,int[]> yellowHsvBounds = new HashMap<String, int[]>();
//        HashMap<String,int[]> greenHsvBounds = new HashMap<String, int[]>();
//        HashMap<String,int[]> blueHsvBounds = new HashMap<String, int[]>();
//
//        ArrayList<HashMap<String,int[]>> red = new ArrayList<HashMap<String, int[]>>();
//        ArrayList<HashMap<String,int[]>> yellow = new ArrayList<HashMap<String, int[]>>();
//        ArrayList<HashMap<String,int[]>> green = new ArrayList<HashMap<String, int[]>>();
//        ArrayList<HashMap<String,int[]>> blue = new ArrayList<HashMap<String, int[]>>();
//
//        redHsvBounds.put("lower_hsv",new int[]{1,2,3,4});
//        redHsvBounds.put("upper_hsv",new int[]{1,2,3,4});
//
//        yellowHsvBounds.put("lower_hsv",new int[]{1,2,3,4});
//        yellowHsvBounds.put("upper_hsv",new int[]{1,2,3,4});
//
//        greenHsvBounds.put("lower_hsv",new int[]{1,2,3,4});
//        greenHsvBounds.put("upper_hsv",new int[]{1,2,3,4});
//
//        blueHsvBounds.put("lower_hsv",new int[]{1,2,3,4});
//        blueHsvBounds.put("upper_hsv",new int[]{1,2,3,4});
//
//
//        red.add(redHsvBounds);
//        green.add(greenHsvBounds);
//        yellow.add(yellowHsvBounds);
//        blue.add(blueHsvBounds);
//
//        toBeJson.put("red",red);
//        toBeJson.put("yellow",yellow);
//        toBeJson.put("blue",blue);
//        toBeJson.put("green",green);
//
//        Gson gson = new Gson();
//
//        Log.i(TAG, "onCreate: the json is "+gson.toJson(toBeJson));


        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if((ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) & (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
        {
            Log.e(TAG, "onCreate: permissions  granted ");

            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                Log.i(TAG, "onCreate: Permission is given to write the config file");

                configDir = new File(Environment.getExternalStorageDirectory()+File.separator+getResources().getString(R.string.app_name));

                Log.i(TAG, "onCreate: Config directory is " + configDir.getPath());

                if(!configDir.exists())
                {
                    if(!configDir.mkdir()) Log.e(TAG, "onCreate: Failed creating the file" );
                    else Log.i(TAG, "onCreate: Successfully created " +configDir.getPath());
                }
                else Log.i(TAG, "onCreate: Config directory already exists");
            }
            else
            {
                Log.e(TAG, "onRequestPermissionsResult: storage is not mounted" );
            }
        }
        else
        {
            Log.e(TAG, "onCreate: permissions not granted ");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},reqCode);
        }

        

        if(savedInstanceState!=null)
        {
            hsvULBounds = savedInstanceState.getIntArray(HSVUL_KEY);
        }

        hsvRecyAdap= new hsvRecyAdap();

        blurSeekBar = (SeekBar) findViewById(R.id.blurPerSeekbar);

        blurDisp = (TextView) findViewById(R.id.blurPer);

        blurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                {

                    k_Size.height = blurThreshold*(seekBar.getProgress()/100.0);
                    k_Size.width=k_Size.height;

                    blurDisp.setText(String.valueOf(seekBar.getProgress())+"%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hsvRecycler = (RecyclerView) findViewById(R.id.hsvList);
        hsvRecycler.setLayoutManager(new LinearLayoutManager(this));
        hsvRecycler.setAdapter(hsvRecyAdap);

        colorSpinner = (Spinner) findViewById(R.id.colorSelector);

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hsvULBounds = defaultHSVBounds[position];

                Log.e(TAG, "onItemSelected:  hsv ul bounds" + Arrays.toString(hsvULBounds)) ;
                hsvRecyAdap.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);


        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener() {
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(Mat inputFrame) {


                Imgproc.cvtColor(inputFrame,inputFrame,Imgproc.COLOR_RGB2HSV);


                try {
                    Imgproc.blur(inputFrame,inputFrame,k_Size);
                }

                catch (Exception e)
                {
                    Log.e(TAG, "onCameraFrame: Failed to blur the image");
                }




                Core.inRange(inputFrame,new Scalar(hsvULBounds[0],hsvULBounds[1],hsvULBounds[2]),new Scalar(hsvULBounds[3],hsvULBounds[4],hsvULBounds[5]),inputFrame);

                return inputFrame;
            }
        });
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }

    }




    public class hsvRecyAdap extends RecyclerView.Adapter<hsvRecyAdap.viewholder>
    {
        String TAG = this.toString();
        
        @NonNull
        @Override
        public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hsv_list_recycler,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull viewholder holder, int position) {
            Log.i(TAG, "onBindViewHolder: Adding items");
            holder.hsvLabel.setText(lowHsvUpperHsv[position]);

            holder.hsvValue.setText(String.valueOf(hsvULBounds[position]));


            holder.hsvSeekbar.setMax(hsvULMax[position]);

            holder.hsvSeekbar.setProgress(hsvULBounds[position]);



            holder.hsvSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if(fromUser)
                    {
                        hsvULBounds[position]=progress;
                        holder.hsvValue.setText(String.valueOf(progress));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        @Override
        public int getItemCount() {

            return lowHsvUpperHsv.length;
        }

        private  class viewholder extends RecyclerView.ViewHolder
        {
            TextView hsvLabel;
            SeekBar hsvSeekbar;
            TextView hsvValue;

            public viewholder(@NonNull View itemView) {
                super(itemView);

                hsvLabel = (TextView) itemView.findViewById(R.id.hsvType);
                hsvSeekbar = (SeekBar) itemView.findViewById(R.id.hsvSeekBar);
                hsvValue = (TextView) itemView.findViewById(R.id.hsvValue);

            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void onDestroy() {
        super.onDestroy();


        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putIntArray("hsvULBounds",hsvULBounds);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
}