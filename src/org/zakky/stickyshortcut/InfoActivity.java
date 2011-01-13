/*
 * Copyright 2011 YAMAZAKI Makoto<makoto1975@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zakky.stickyshortcut;

import java.util.Locale;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * 使用方法を表示するアクティビティです。
 * 
 * @author zaki
 */
public class InfoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info);

        // AppVersion取得
        final TextView versionView = (TextView) findViewById(R.id.info_txt_ver);
        versionView.setText("ver " + getAppVersion());

        // WebView
        final WebView separator = (WebView) findViewById(R.id.info_separator);
        separator.loadData("<body bgcolor=\"#FFFFFF\"><hr/></body>", "text/html", "utf-8");

        final WebView info = (WebView) findViewById(R.id.info);
        if (Locale.getDefault().equals(Locale.JAPAN)) {
            info.loadUrl("file:///android_asset/index_ja.html");
        } else {
            info.loadUrl("file:///android_asset/index.html");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * アプリケーションの表示用バージョン番号文字列を返します。
     * 
     * @return 表示用バージョン番号文字列。取得に失敗した場合は "{@code unknown}" を返します。
     */
    private String getAppVersion() {
        try {
            final String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            return version;
        } catch (NameNotFoundException e) {
            return "unknown";
        }
    }
}
