package org.zakky.stickyshortcut;

import java.util.List;
import java.util.Locale;

import org.zakky.stickyshortcut.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 通常起動されたときは使用方法を表示し、起動対象情報を含むインテントから起動された場合は
 * 起動対象を実際に呼び出すアクティビティです。
 * 
 * @author zaki
 */
public class MainActivity extends Activity {

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
        if (targetPackage_ == null) {
            setContentView(R.layout.main);

            // AppVersion取得
            final TextView versionView = (TextView) findViewById(R.id.info_txt_ver);
            versionView.setText("ver " + getAppVersion());

            // WebView
            final WebView separator = (WebView) findViewById(R.id.info_separator);
            separator.loadData("<body bgcolor=\"#FFFFFF\"><hr/></body>",
                    "text/html", "utf-8");

            final WebView info = (WebView) findViewById(R.id.info);
            if (Locale.getDefault().equals(Locale.JAPAN)) {
                info.loadUrl("file:///android_asset/index_ja.html");
            } else {
                info.loadUrl("file:///android_asset/index.html");
            }
        } else {
            setContentView(R.layout.launcher);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (targetPackage_ == null) {
            // 通常起動の場合なので、使用方法の表示のみ。なにもしない。
            return;
        }

        final Intent launchIntent = buildLaunchIntent();

        if (!isTargetInstalled(launchIntent)) {
            // 起動対象アプリがインストールされていない場合

            // TODO Toast ではなく、アクティビティ自信に表示を行う
            final String message = getString(R.string.target_app_not_installed,
                    targetLabel_);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    /**
     * アプリケーションの表示用バージョン番号文字列を返します。
     *
     * @return
     * 表示用バージョン番号文字列。取得に失敗した場合は "{@code unknon}" を返します。
     */
    private String getAppVersion() {
        try {
            final String version = getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName;
            return version;
        } catch (NameNotFoundException e) {
            return "unknown";
        }
    }
}
