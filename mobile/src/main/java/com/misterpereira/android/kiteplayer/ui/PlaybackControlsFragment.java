/*
 * Original work Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified work Copyright (c) 2015 Rafael Pereira
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 *
 *      https://mozilla.org/MPL/2.0/
 *
 */
package com.misterpereira.android.kiteplayer.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.misterpereira.android.kiteplayer.MusicService;
import com.misterpereira.android.kiteplayer.R;
import com.misterpereira.android.kiteplayer.utils.LogHelper;
import com.misterpereira.android.kiteplayer.utils.ResourceHelper;

/**
 * A class that shows the Media Queue to the user.
 */
public class PlaybackControlsFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(PlaybackControlsFragment.class);
    private static ColorStateList sColorStateNotPlaying;
    private static ColorStateList sColorStatePlaying;

    private ImageButton mPlayPause;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mExtraInfo;
    private ImageView mAlbumArt;

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            LogHelper.d(TAG, "Received playback state change to state ", state.getState());
            PlaybackControlsFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            if (metadata == null) {
                return;
            }
            LogHelper.d(TAG, "Received metadata state change to mediaId=",
                    metadata.getDescription().getMediaId(),
                    " song=", metadata.getDescription().getTitle());
            PlaybackControlsFragment.this.onMetadataChanged(metadata);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(getActivity());
        }

        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

        mPlayPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);

        mTitle = (TextView) rootView.findViewById(R.id.title);
        mSubtitle = (TextView) rootView.findViewById(R.id.artist);
        mExtraInfo = (TextView) rootView.findViewById(R.id.extra_info);
        mAlbumArt = (ImageView) rootView.findViewById(R.id.album_art);
        rootView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FullScreenPlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            MediaMetadata metadata = getActivity().getMediaController().getMetadata();
            if (metadata != null) {
                intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION,
                        metadata.getDescription());
            }
            startActivity(intent);
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogHelper.d(TAG, "fragment.onStart");
        if (getActivity().getMediaController() != null) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LogHelper.d(TAG, "fragment.onStop");
        if (getActivity().getMediaController() != null) {
            getActivity().getMediaController().unregisterCallback(mCallback);
        }
    }

    public void onConnected() {
        MediaController controller = getActivity().getMediaController();
        LogHelper.d(TAG, "onConnected, mediaController==null? ", controller == null);
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    private void onMetadataChanged(MediaMetadata metadata) {
        LogHelper.d(TAG, "onMetadataChanged ", metadata);
        if (getActivity() == null) {
            LogHelper.w(TAG, "onMetadataChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (metadata == null) {
            return;
        }

        mTitle.setText(metadata.getDescription().getTitle());
        mSubtitle.setText(metadata.getDescription().getSubtitle());

        Bitmap albumThumbnail =
            metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);

        if (albumThumbnail != null) {
            mAlbumArt.setImageBitmap(albumThumbnail);
        } else {
            Glide
                    .with(this)
                    .load(metadata)
                    .error(R.drawable.ic_album_art)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAlbumArt);
        }
    }

    public void setExtraInfo(String extraInfo) {
        if (extraInfo == null) {
            mExtraInfo.setVisibility(View.GONE);
        } else {
            mExtraInfo.setText(extraInfo);
            mExtraInfo.setVisibility(View.VISIBLE);
        }
    }

    private void onPlaybackStateChanged(PlaybackState state) {
        LogHelper.d(TAG, "onPlaybackStateChanged ", state);
        if (getActivity() == null) {
            LogHelper.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackState.STATE_PAUSED:
            case PlaybackState.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackState.STATE_ERROR:
                LogHelper.e(TAG, "error playbackstate: ", state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        if (enablePlay) {
            mPlayPause.setImageDrawable(
                    getActivity().getDrawable(R.drawable.ic_play_arrow_black_36dp));
            mPlayPause.setImageTintList(sColorStateNotPlaying);
        } else {
            mPlayPause.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pause_black_36dp));
            mPlayPause.setImageTintList(sColorStatePlaying);
        }

        MediaController controller = getActivity().getMediaController();
        String extraInfo = null;
        if (controller != null && controller.getExtras() != null) {
            String castName = controller.getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);
            if (castName != null) {
                extraInfo = getResources().getString(R.string.casting_to_device, castName);
            }
        }
        setExtraInfo(extraInfo);
    }

    private final View.OnClickListener mButtonListener = v -> {
        PlaybackState stateObj = getActivity().getMediaController().getPlaybackState();
        final int state = stateObj == null ?
                PlaybackState.STATE_NONE : stateObj.getState();
        LogHelper.d(TAG, "Button pressed, in state ", state);
        switch (v.getId()) {
            case R.id.play_pause:
                LogHelper.d(TAG, "Play button pressed, in state ", state);
                if (state == PlaybackState.STATE_PAUSED ||
                        state == PlaybackState.STATE_STOPPED ||
                        state == PlaybackState.STATE_NONE) {
                    playMedia();
                } else if (state == PlaybackState.STATE_PLAYING ||
                        state == PlaybackState.STATE_BUFFERING ||
                        state == PlaybackState.STATE_CONNECTING) {
                    pauseMedia();
                }
                break;
        }
    };

    private void playMedia() {
        MediaController controller = getActivity().getMediaController();
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaController controller = getActivity().getMediaController();
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    static private void initializeColorStateLists(Context ctx) {
        sColorStateNotPlaying = ColorStateList.valueOf(ResourceHelper.getThemeColor(ctx,android.R.attr.textColorSecondary,R.color.secondary_text_light));
        sColorStatePlaying = ColorStateList.valueOf(ResourceHelper.getThemeColor(ctx,R.attr.colorAccent,R.color.app_accent));
    }

    public static final class Behavior extends CoordinatorLayout.Behavior<CardView> {
        public Behavior(Context context, AttributeSet attrs) {}

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, CardView child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, CardView child, View dependency) {
            parent.setMinimumHeight(child.getHeight() + dependency.getHeight());
            return true;
        }

        @Override
        public void onDependentViewRemoved(CoordinatorLayout parent, CardView child, View dependency) {
            parent.setMinimumHeight(child.getHeight());
        }
    }
}
