package org.zakky.openappshortcut;

import java.util.List;

import org.zakky.openappshortcut.shortcut.OpenShortcutActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

    private String targetPackage_;
    private String targetFqcn_;
    private String targetLabel_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        targetPackage_ = getTargetPackage(getIntent());
        targetFqcn_ = getTargetFqcn(getIntent());
        targetLabel_ = getTargetLabel(getIntent());
        if (targetPackage_ == null) {
            setContentView(R.layout.main);
        } else {
            setContentView(R.layout.launcher);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (targetPackage_ == null) {
            return;
        }

        final Intent launchIntent = buildLaunchIntent();

        if (!isTargetInstalled(launchIntent)) {
            final String message = getString(R.string.target_app_not_installed,
                    targetLabel_);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }

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

}