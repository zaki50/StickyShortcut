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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * 起動対象を実際に呼び出すアクティビティです。 スティッキーショートカットから呼び出されることを想定しています。
 *
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
public class LauncherActivity extends Activity {
    private static final String TAG = LauncherActivity.class.getSimpleName();

    /*
     * スティッキーショートカットに保持させる EXTRA のキーのための定数群
     */

    /** 起動対象アプリのパッケージ名のためのキー */
    public static final String EXTRA_TARGET_PACKAGE = "EXTRA_TARGET_PACKAGE";

    /** 起動対象アプリのクラス名のためのキー */
    public static final String EXTRA_TARGET_FQCN = "EXTRA_TARGET_FQCN";

    /** 起動対象アプリのラベルのためのキー */
    public static final String EXTRA_TARGET_LABEL = "EXTRA_TARGET_LABEL";

    /** 起動対象アプリのパッケージ名 */
    @CheckForNull
    private String targetPackage_;

    /** 起動対象アプリのクラス名 */
    @CheckForNull
    private String targetFqcn_;

    /** 起動対象アプリのラベル */
    @CheckForNull
    private String targetLabel_;

    @Override
    public void onCreate(@CheckForNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @CheckForNull
        final Intent intent = getIntent();

        targetPackage_ = getTargetPackage(intent);
        targetFqcn_ = getTargetFqcn(intent);
        targetLabel_ = getTargetLabel(intent);
        setContentView(R.layout.launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final String targetPackage = targetPackage_;
        if (targetPackage == null) {
            // 起動対象が不明なので終了
            finish();
            return;
        }

        final Intent launchIntent = buildLaunchIntent();

        if (!isTargetInstalled(launchIntent)) {
            // 起動対象アプリがインストールされていない場合

            final String message = getString(R.string.target_app_not_installed, targetLabel_);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            final Intent istallIntent = buildInstallIntent(targetPackage);
            try {
                startActivity(istallIntent);
            } catch (ActivityNotFoundException e) {
                final Toast toast = Toast.makeText(this, R.string.failed_to_open_market,
                        Toast.LENGTH_LONG);
                toast.show();
            }
            finish();
            return;
        }

        // 起動対象アプリを実際に呼び出す。
        try {
            startActivity(launchIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "failed to start activity. package=" + targetPackage + ", fqcn="
                    + targetFqcn_ + ", label=" + targetLabel_, e);
        }
        finish();
    }

    /**
     * 指定されたインテントからターゲットパッケージ名を取得します。
     *
     * @param intent 取得元インテント。
     * @return ターゲットパッケージ名。 取得元インテントが {@code null} の場合や、取得元インテントが
     *         ターゲットパッケージ名情報を保持していない場合は {@code null} を返します。
     */
    @CheckForNull
    private String getTargetPackage(@CheckForNull Intent intent) {
        if (intent == null) {
            return null;
        }
        final String targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE);
        return targetPackage;
    }

    /**
     * 指定されたインテントからターゲットクラス名を取得します。
     *
     * @param intent 取得元インテント。
     * @return ターゲットクラス名。 取得元インテントが {@code null} の場合や、取得元インテントが
     *         ターゲットクラス名情報を保持していない場合は {@code null} を返します。
     */
    @CheckForNull
    private String getTargetFqcn(@CheckForNull Intent intent) {
        if (intent == null) {
            return null;
        }
        final String targetFqcn = intent.getStringExtra(EXTRA_TARGET_FQCN);
        return targetFqcn;
    }

    /**
     * 指定されたインテントからターゲットラベルを取得します。
     *
     * @param intent 取得元インテント。
     * @return ターゲットラベル。 取得元インテントが {@code null} の場合や、取得元インテントが
     *         ターゲットラベル情報を保持していない場合は {@code null} を返します。
     */
    @CheckForNull
    private String getTargetLabel(@CheckForNull Intent intent) {
        if (intent == null) {
            return null;
        }
        final String targetLabel = intent.getStringExtra(EXTRA_TARGET_LABEL);
        return targetLabel;
    }

    /**
     * ターゲットアプリを起動するためのインテントを構築します。
     *
     * @return ターゲットアプリ起動用インテント。
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
     * @return ターゲットアプリインストール用インテント。
     */
    private Intent buildInstallIntent(String targetPackage) {
        final Uri uri = Uri.parse("market://details?id=" + targetPackage);
        final Intent installIntent = new Intent(Intent.ACTION_VIEW, uri);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return installIntent;
    }

    /**
     * 指定されたインテントを送った際に、レシーバが存在するかどうかを返します。
     *
     * @param intent インテント。
     * @return レシーバが存在すれば {@code true}、存在しなければ {@code false} を返します。
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
