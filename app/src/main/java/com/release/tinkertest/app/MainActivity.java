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

package com.release.tinkertest.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.release.tinkertest.R;
import com.release.tinkertest.tinker.Utils;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

public class MainActivity extends AppCompatActivity {

    private TextView mTvMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvMessage = findViewById(R.id.tv_message);

        findViewById(R.id.btn_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "你好，延成，好好啊！！！！", Toast.LENGTH_SHORT).show();
            }
        });

        askForRequiredPermissions();

        findViewById(R.id.loadPatch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk");
            }
        });

        findViewById(R.id.cleanPatch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tinker.with(getApplicationContext()).cleanPatch();
            }
        });

        findViewById(R.id.killSelf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setBackground(false);
        if (hasRequiredPermissions()) {
            mTvMessage.setVisibility(View.GONE);
        } else {
            mTvMessage.setText(R.string.msg_no_permissions);
            mTvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            mTvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.setBackground(true);
    }

    private void askForRequiredPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= 16) {
            final int res = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return res == PackageManager.PERMISSION_GRANTED;
        } else {
            final int res = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return res == PackageManager.PERMISSION_GRANTED;
        }
    }



}
