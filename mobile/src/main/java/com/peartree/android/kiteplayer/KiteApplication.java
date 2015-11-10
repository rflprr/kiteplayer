/*
 * Copyright (c) 2015 Rafael Pereira
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 *     https://mozilla.org/MPL/2.0/.
 */

package com.peartree.android.kiteplayer;

import android.app.Application;
import android.os.Build;

import com.facebook.stetho.Stetho;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.peartree.android.kiteplayer.ui.FullScreenPlayerActivity;
import com.peartree.android.kiteplayer.utils.LogHelper;

import javax.inject.Inject;

import dagger.Lazy;

import static com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager.FEATURE_DEBUGGING;
import static com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager.FEATURE_WIFI_RECONNECT;


public class KiteApplication extends Application {

    private static final String TAG = LogHelper.makeLogTag(KiteApplication.class);

    protected KiteApplicationComponent mApplicationComponent;

    private VideoCastManager mCastManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerKiteApplicationComponent.builder()
                .kiteApplicationModule(new KiteApplicationModule(this))
                .build();

        mCastManager = VideoCastManager.initialize(
                this, this.getResources().getString(R.string.cast_application_id), FullScreenPlayerActivity.class, null);

        mCastManager.enableFeatures(FEATURE_WIFI_RECONNECT | FEATURE_DEBUGGING);

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

    }

    public KiteApplicationComponent getComponent() { return this.mApplicationComponent; }
}
