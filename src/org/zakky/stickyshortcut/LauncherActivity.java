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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 起動対象を実際に呼び出すアクティビティです。
 * スティッキーショートカットから呼び出されることを想定しています。
 * 
 * @author zaki
 */
public class LauncherActivity extends Activity {

    /** 起動対象アプリのパッケージ名 */
    private String targetPackage_;
    /** 起動対象アプリのクラス名 */
    private String targetFqcn_;
    /** 起動対象アプリのラベル */
    private String targetLabel_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        targetPackage_ = getTargetPackage(getIntent());
        targetFqcn_ = getTargetFqcn(getIntent());
        targetLabel_ = getTargetLabel(getIntent());
        setContentView(R.layout.launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (targetPackage_ == null) {
            // 起動対象が不明なので終了
            finish();
            return;
        }

        final Intent launchIntent = buildLaunchIntent();

        if (!isTargetInstalled(launchIntent)) {
            // 起動対象アプリがインストールされていない場合

            final String message = getString(R.string.target_app_not_installed,
                    targetLabel_);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            final Intent istallIntent = buildInstallIntent();
            startActivity(istallIntent);

            finish();
            return;
        }

        // 起動対象アプリを実際に呼び出す。
        startActivity(launchIntent);
        finish();
    }

    /**
     * 指定されたインテントからターゲットパッケージ名を取得します。
     * 
     * @param intent
     * 取得元インテント。
     * @return
     * ターゲットパッケージ名。 取得元インテントが {@code null} の場合や、取得元インテントが
     * ターゲットパッケージ名情報を保持していない場合は {@code null} を返します。
     */
    private String getTargetPackage(Intent intent) {
        if (intent == null) {
            return null;
        }
        final String targetPackage = intent
                .getStringExtra(OpenShortcutActivity.EXTRA_TARGET_PACKAGE);
        return targetPackage;
    }

    /**
     * 指定されたインテントからターゲットクラス名を取得します。
     * 
     * @param intent
     * 取得元インテント。
     * @return
     * ターゲットクラス名。 取得元インテントが {@code null} の場合や、取得元インテントが
     * ターゲットクラス名情報を保持していない場合は {@code null} を返します。
     */
    private String getTargetFqcn(Intent intent) {
        if (intent == null) {
            return null;
        }
        final String targetFqcn = intent
                .getStringExtra(OpenShortcutActivity.EXTRA_TARGET_FQCN);
        return targetFqcn;
    }

    /**
     * 指定されたインテントからターゲットラベルを取得します。
     * 
     * @param intent
     * 取得元インテント。
     * @return
     * ターゲットラベル。 取得元インテントが {@code null} の場合や、取得元インテントが
     * ターゲットラベル情報を保持していない場合は {@code null} を返します。
     */
    private String getTargetLabel(Intent intent) {
        if (intent == null) {
            return null;
        }
        final String targetLabel = intent
                .getStringExtra(OpenShortcutActivity.EXTRA_TARGET_LABEL);
        return targetLabel;
    }

    /**
     * ターゲットアプリを起動するためのインテントを構築します。
     * 
     * @return
     * ターゲットアプリ起動用インテント。
     */
    private Intent buildLaunchIntent() {
        final Intent launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchIntent.setClassName(targetPackage_, targetFqcn_);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return launchIntent;
    }

    /**
     * ターゲットアプリをインストールするためのインテントを構築します。
     * 
     * @return
     * ターゲットアプリインストール用インテント。
     */
    private Intent buildInstallIntent() {
        final Uri uri = Uri.parse("market://details?id=" + targetPackage_);
        final Intent installIntent = new Intent(Intent.ACTION_VIEW, uri);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return installIntent;
    }

    /**
     * 指定されたインテントを送った際に、レシーバが存在するかどうかを返します。
     *
     * @param intent
     * インテント。
     * @return
     * レシーバが存在すれば {@code true}、存在しなければ {@code false} を返します。
     */
    private boolean isTargetInstalled(Intent intent) {
        if (intent == null) {
            return false;
        }

        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        final boolean result = (!resolveInfo.isEmpty());
        return result;
    }
}
