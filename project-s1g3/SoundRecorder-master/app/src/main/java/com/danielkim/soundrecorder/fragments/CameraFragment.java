package com.danielkim.soundrecorder.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danielkim.soundrecorder.CameraPreview;
import com.danielkim.soundrecorder.R;

/**
 * Created by Lorand on 3/19/17.
 */

public class CameraFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = CameraFragment.class.getSimpleName();
    private Camera mCamera = null;
    private int position;
    public static View videoView;

    public static CameraFragment newInstance(int position) {
        CameraFragment f = new CameraFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        videoView = inflater.inflate(R.layout.fragment_video, container, false);
        cameraOpenInView();

        return videoView;
    }

    public void cameraOpenInView() {
        openCamera();
        new CameraPreview(getActivity().getBaseContext(), mCamera);
    }

    public void openCamera() {
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){

        }
    }
}


