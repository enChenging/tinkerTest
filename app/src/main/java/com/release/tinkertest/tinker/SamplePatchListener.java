/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.release.tinkertest.tinker;

import android.app.ActivityManager;
import android.content.Context;

import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;


/**
 * Created by zhangshaowen on 16/4/30.
 * optional, you can just use DefaultPatchListener
 * we can check whatever you want whether we actually send a patch request
 * such as we can check rom space or apk channel
 */
public class SamplePatchListener extends DefaultPatchListener {
    private static final String TAG = "Tinker.SamplePatchListener";

    private final int maxMemory;

    public SamplePatchListener(Context context) {
        super(context);
        maxMemory = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        TinkerLog.i(TAG, "application maxMemory:" + maxMemory);
    }

    /**
     * because we use the defaultCheckPatchReceived method
     * the error code define by myself should after {@code ShareConstants.ERROR_RECOVER_INSERVICE
     *
     * @param path
     * @param newPatch
     * @return
     */
    @Override
    public int patchCheck(String path, String patchMd5) {
        //可以在这个方法中做一些自定义行为，比如文件的合法性等
        //然后再加载patch文件的时候调用该方法
        Tinker manager = Tinker.with(context);
        //check SharePreferences also
        if (!manager.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            return ShareConstants.ERROR_PATCH_DISABLE;
        }
        File file = new File(path);

        if (!SharePatchFileUtil.isLegalFile(file)) {
            return ShareConstants.ERROR_PATCH_NOTEXIST;
        }

        //patch service can not send request
        if (manager.isPatchProcess()) {
            return ShareConstants.ERROR_PATCH_INSERVICE;
        }

        //if the patch service is running, pending
        if (TinkerServiceInternals.isTinkerPatchServiceRunning(context)) {
            return ShareConstants.ERROR_PATCH_RUNNING;
        }
        if (ShareTinkerInternals.isVmJit()) {
            return ShareConstants.ERROR_PATCH_JIT;
        }

        Tinker tinker = Tinker.with(context);

        if (tinker.isTinkerLoaded()) {
            TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();
            if (tinkerLoadResult != null && !tinkerLoadResult.useInterpretMode) {
                String currentVersion = tinkerLoadResult.currentVersion;
                if (patchMd5.equals(currentVersion)) {
                    return ShareConstants.ERROR_PATCH_ALREADY_APPLY;
                }
            }
        }

        if (!UpgradePatchRetry.getInstance(context).onPatchListenerCheck(patchMd5)) {
            return ShareConstants.ERROR_PATCH_RETRY_COUNT_LIMIT;
        }

        return ShareConstants.ERROR_PATCH_OK;
    }
}
