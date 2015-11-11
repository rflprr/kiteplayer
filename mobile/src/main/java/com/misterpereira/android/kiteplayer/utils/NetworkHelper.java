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

package com.misterpereira.android.kiteplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Generic reusable network methods.
 */
public class NetworkHelper {
    /**
     * @param context to use to check for network connectivity.
     * @return true if connected, false otherwise.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isNetworkMetered(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.isActiveNetworkMetered();
    }

    public static boolean canStream(Context context) {
        return isOnline(context) &&
                (PrefUtils.isStreamingOverCellularAllowed(context) || !isNetworkMetered(context));
    }

    public static boolean canSync(Context context) {
        return isOnline(context) &&
                (PrefUtils.isSyncOverCellularAllowed(context) || !isNetworkMetered(context));
    }
}