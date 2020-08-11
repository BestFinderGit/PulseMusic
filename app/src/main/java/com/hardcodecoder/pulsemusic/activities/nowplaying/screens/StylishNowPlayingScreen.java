package com.hardcodecoder.pulsemusic.activities.nowplaying.screens;

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.GenericTransitionOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.nowplaying.base.BaseNowPlayingScreen;
import com.hardcodecoder.pulsemusic.utils.DimensionsUtil;

public class StylishNowPlayingScreen extends BaseNowPlayingScreen {

    public static final String TAG = StylishNowPlayingScreen.class.getSimpleName();
    private Slider mProgressSlider;
    private ShapeableImageView mAlbumCover;
    private ImageView mFavoriteBtn;
    private ImageView mRepeatBtn;
    private ImageView mPlayPauseBtn;
    private MaterialTextView mTitle;
    private MaterialTextView mStartTime;
    private MaterialTextView mEndTime;
    private MaterialTextView mUpNext;

    public static StylishNowPlayingScreen getInstance() {
        return new StylishNowPlayingScreen();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmen_now_playing_stylish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAlbumCover = view.findViewById(R.id.stylish_nps_album_cover);
        mTitle = view.findViewById(R.id.stylish_nps_title);
        mProgressSlider = view.findViewById(R.id.stylish_nps_slider);
        mStartTime = view.findViewById(R.id.stylish_nps_start_time);
        mEndTime = view.findViewById(R.id.stylish_nps_end_time);
        mRepeatBtn = view.findViewById(R.id.stylish_nps_repeat_btn);
        mPlayPauseBtn = view.findViewById(R.id.stylish_nps_play_pause_btn);
        mFavoriteBtn = view.findViewById(R.id.stylish_nps_favourite_btn);
        mUpNext = view.findViewById(R.id.stylish_nps_up_next);
        view.findViewById(R.id.stylish_nps_close_btn).setOnClickListener(v -> {
            if (null != getActivity())
                getActivity().finish();
        });
        setUpSliderControls(mProgressSlider);
        setUpSkipControls(
                view.findViewById(R.id.stylish_nps_prev_btn),
                view.findViewById(R.id.stylish_nps_next_btn));
        mRepeatBtn.setOnClickListener(v -> toggleRepeatMode());
        mPlayPauseBtn.setOnClickListener(v -> togglePlayPause());
        mFavoriteBtn.setOnClickListener(v -> toggleFavorite());
        mAlbumCover.setShapeAppearanceModel(mAlbumCover.getShapeAppearanceModel().withCornerSize(DimensionsUtil.getRoundingRadiusPixelSize16dp()));
    }

    @Override
    public void onRepeatStateChanged(boolean repeat) {
        mRepeatBtn.setImageResource(repeat ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);
    }

    @Override
    public void onFavoriteStateChanged(boolean isFavorite) {
        mFavoriteBtn.setSelected(isFavorite);
    }

    @Override
    public void onMetadataDataChanged(MediaMetadata metadata) {
        super.onMetadataDataChanged(metadata);
        if (null == metadata) return;
        long seconds = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION) / 1000;
        resetSliderValues(mProgressSlider, seconds);
        mStartTime.setText(getFormattedElapsedTime(0));
        mEndTime.setText(getFormattedElapsedTime(seconds));
        mTitle.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));

        GlideApp.with(this)
                .load(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART))
                .transition(GenericTransitionOptions.with(R.anim.now_playing_album_card))
                .into(mAlbumCover);
        mUpNext.setText(getUpNextText());
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState state) {
        super.onPlaybackStateChanged(state);
        if (state == null) return;
        togglePlayPauseAnimation(mPlayPauseBtn, state);
    }

    @Override
    public void onProgressChanged(int progressInSec) {
        mProgressSlider.setValue(progressInSec);
        mStartTime.setText(getFormattedElapsedTime(progressInSec));
    }
}
