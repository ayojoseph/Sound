package com.danielkim.soundrecorder;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.danielkim.soundrecorder.fragments.CameraFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Lorand on 3/07/17.
 *
 * Handles Camera Preview, picture and video taking from the preview
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Button buttonSwitch;
    private Button buttonPicture;
    private int camId;
    private Camera.Parameters params;
    private SurfaceView mSurfaceView;
    private Camera.PictureCallback mPicture;
    private List<Camera.Size> sizes;

    private MediaRecorder mMediaRecorder;
    private ToggleButton buttonVideo;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mSurfaceView = (SurfaceView) CameraFragment.videoView.findViewById(R.id.camera_preview);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        camId = Camera.CameraInfo.CAMERA_FACING_BACK;
        buttonSwitch = (Button) CameraFragment.videoView.findViewById(R.id.button_switch);
        buttonPicture = (Button) CameraFragment.videoView.findViewById(R.id.button_picture);
        buttonVideo = (ToggleButton) CameraFragment.videoView.findViewById(R.id.button_video);

        if (mCamera == null) {
            mCamera = Camera.open();
        }

        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }

        params = mCamera.getParameters();
        params.setRotation(90);
        sizes = params.getSupportedPictureSizes();

        params.setPictureSize(1024, 768);
        mCamera.setParameters(params);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.stopPreview();
                mCamera.release();

                if(camId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    camId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mCamera = Camera.open(camId);
                    params = mCamera.getParameters();
                    params.setRotation(270);
                    params.setPictureSize(1024, 768);
                    mCamera.setParameters(params);
                }
                else {
                    camId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    mCamera = Camera.open(camId);
                    params = mCamera.getParameters();
                    params.setRotation(90);
                    params.setPictureSize(1024, 768);
                    mCamera.setParameters(params);
                }

                try {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
                    mCamera.setDisplayOrientation(90);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        buttonVideo.setOnClickListener(new View.OnClickListener() {
            String videoFileName;
            String fileParts[];
            long timeStart = 0;
            long timeElapsed = 0;

            @Override
            public void onClick(View view) {
                if (((ToggleButton)view).isChecked()) {
                    try {
                        videoFileName = createFileName();
                        initRecorder(mHolder.getSurface(), videoFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.unlock();
                    mMediaRecorder.start();
                    timeStart = System.nanoTime();
                } else {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mCamera.lock();
                    Toast.makeText(getContext(), "Video saved to " + videoFileName, Toast.LENGTH_SHORT).show();
                    fileParts = parseFileName(videoFileName);
                    timeElapsed = (System.nanoTime() - timeStart) / 1000000; //Nanoseoncds to milliseconds
                    addToDatabase(fileParts[1], fileParts[0], timeElapsed);
                }
            }
        });

        mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile();
                String fileParts[];

                if (pictureFile == null) {
                    Toast.makeText(getContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Toast.makeText(getContext(), "Picture saved to " + pictureFile, Toast.LENGTH_SHORT).show();
                    fileParts = parseFileName(pictureFile.toString());
                    addToDatabase(fileParts[1], fileParts[0], 0);
                    mCamera.startPreview();
                } catch (FileNotFoundException e) {
                    Toast.makeText(getContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void initRecorder(Surface surface, String videoFileName) throws IOException {
        if(mCamera == null) {
            mCamera = Camera.open();
            mCamera.unlock();
        }

        if(mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setOutputFile(videoFileName);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public String createFileName () {
        File folder;
        String fileName;
        String file;
        String state = Environment.getExternalStorageState();

        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getContext(), "No media to save to", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            file = folder + "/" + fileName + ".3GPP";

            return file;
        }
    }

    private File getOutputMediaFile () {
        String state = Environment.getExternalStorageState();
        String timeStamp;
        File outfile;

        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getContext(), "No media to save to", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            if (folder.exists()) {
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                outfile = new File(folder, timeStamp + ".jpeg");
            } else {
                return null;
            }

            return outfile;
        }
    }

    //Destroy camera on exit
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();

        mMediaRecorder.reset();
        mMediaRecorder.release();

        mMediaRecorder = null;
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addToDatabase(String fileName, String filePath, long duration) {
        DBHelper db = new DBHelper(getContext());

        try {
            db.addRecording(fileName, filePath, duration);
        } catch (Exception e){

        }
        db.close();
    }

    public String [] parseFileName(String path) {
        String[] fileParts;
        String[] toReturn = new String[2];
        int count = 0;

        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                count++;
            }
        }
        fileParts = path.split("/");

        toReturn[0] = "";
        for (int j = 0; j < count; j++) {
            toReturn[0] += fileParts[j];
            toReturn[0] += "/";
        }
        toReturn[1] = fileParts[count];

        return toReturn;
    }
}

