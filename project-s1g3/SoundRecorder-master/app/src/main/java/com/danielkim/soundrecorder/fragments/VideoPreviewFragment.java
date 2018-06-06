package com.danielkim.soundrecorder.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;

/**
 * Created by Lorand on 4/05/17.
 *
 * Video playback
 */

public class VideoPreviewFragment extends DialogFragment {
    private static final String ARG_ITEM = "recording_item";
    private RecordingItem item;
    private VideoView videoView;
    private ToggleButton toggleButton;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private boolean videoStarted = false;
    private TextView fileName = null;

    public VideoPreviewFragment newInstance(RecordingItem item) {
        VideoPreviewFragment f = new VideoPreviewFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ITEM, item);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(ARG_ITEM);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_video_playback, null);

        videoView = (VideoView) view.findViewById(R.id.video_view);
        toggleButton = (ToggleButton) view.findViewById(R.id.play_pause);
        seekBar = (SeekBar) view.findViewById(R.id.video_seekbar);
        fileName = (TextView) view.findViewById(R.id.file_name_video);

        fileName.setText(item.getName());

        videoView.setVideoURI(Uri.parse(item.getFilePath() + item.getName()));
        videoView.seekTo(1);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((ToggleButton)view).isChecked()) {
                    if (videoStarted == false) {
                        startVideo();
                    } else {
                        videoView.start();
                    }
                } else {
                    videoView.pause();
                    videoStarted = true;
                }
            }});

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mRunnable);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mRunnable);
                videoView.seekTo(seekBar.getProgress());
                //System.out.println("Seek bar: " + seekBar.getProgress());
                //System.out.println("Video time: " + videoView.getCurrentPosition());
                updateProgressBar();
            }
        });

        builder.setView(view);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mRunnable, 100);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            //System.out.println("Poisition: " + videoView.getCurrentPosition());
            seekBar.setProgress(videoView.getCurrentPosition());
            mHandler.postDelayed(this, 100);
        }
    };

    public void startVideo () {
        videoView.setVideoURI(Uri.parse(item.getFilePath() + item.getName()));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()  {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(videoView.getDuration());

                videoView.requestFocus();
                videoView.start();
                updateProgressBar();
                videoStarted = true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.stopPlayback();
                videoStarted = false;
                toggleButton.toggle();
            }
        });
    }
}
