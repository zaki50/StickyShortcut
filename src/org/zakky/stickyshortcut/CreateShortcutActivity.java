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

import static org.zakky.stickyshortcut.LauncherActivity.EXTRA_TARGET_FQCN;
import static org.zakky.stickyshortcut.LauncherActivity.EXTRA_TARGET_LABEL;
import static org.zakky.stickyshortcut.LauncherActivity.EXTRA_TARGET_PACKAGE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import yanzm.products.quickaction.lib.ActionItem;
import yanzm.products.quickaction.lib.QuickAction;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ショートカット作成時に呼び出され、ユーザが選択したアプリを起動するショートカットをホームに 作成します。
 * 
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
public final class CreateShortcutActivity extends Activity implements OnItemClickListener {
    private static final String TAG = CreateShortcutActivity.class.getSimpleName();

    /**
     * バッジ無しを表す定数。
     */
    private static final int NO_BADGE = -1;

    /**
     * バッジアイコンリスト。
     */
    private static final int[] BADGE_RES_IDS = {
            NO_BADGE, //
            R.drawable.badge1, //
            R.drawable.badge2, //
            R.drawable.badge3, //
            R.drawable.badge4, //
            R.drawable.badge5, //
            R.drawable.badge6
    };

    /**
     * アプリ一覧表示用グリッド。
     */
    private GridView appGrid_;

    /**
     * アプリ一覧グリッド構築時のプログレス
     *
     * <p>
     * UI スレッドからのみアクセスすること。
     * </p>
     */
    @CheckForNull
    private ProgressDialog progressDialog_ = null;

    /**
     * アプリ一覧のグリッドを用意します。
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * アプリ一覧をユーザに提示するためのグリッドを用意します。
         */
        setContentView(R.layout.grid);

        appGrid_ = (GridView) findViewById(R.id.grid);
        appGrid_.setOnItemClickListener(this);
    }

    /**
     * アクティビティ開始処理として、アプリ一覧を取得してグリッドにセットするためのタスクを 実行します。
     */
    @Override
    protected void onStart() {
        super.onStart();

        final LoadAppListTask task = new LoadAppListTask();
        task.execute();
        startProgress();
    }

    @Override
    protected void onStop() {
        super.onStop();

        dismissProgress();
    }

    /**
     * プログレスダイアログを作成して表示します。
     */
    private void startProgress() {
        dismissProgress();

        final ProgressDialog progress = new ProgressDialog(this);
        final String message = getString(R.string.shortcut_progressdialog_title);
        progress.setMessage(message);
        progress.setCancelable(false);
        progress.show();

        progressDialog_ = progress;
    }

    /**
     * プログレスダイアログが表示されていれば中止します。
     *
     * <p>
     * このメソッドが正常に完了した後は、 {@link #progressDialog_} が {@code null} に
     * なります。
     * </p>
     */
    private void dismissProgress() {
        final ProgressDialog progress = progressDialog_;
        if (progress != null) {
            progress.dismiss();
        }
        progressDialog_ = null;
    }

    /**
     * アプリ一覧で、あるアプリがクリックされたときのアクションです。
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final AppInfo appInfo = (AppInfo) parent.getItemAtPosition(position);

        // QuickAction を表示し、ユーザにアイコンを選択してもらう。
        final QuickAction qa = new QuickAction(view);
        for (int badgeResId : BADGE_RES_IDS) {
            final ActionItem chart = buildCandidate(appInfo, badgeResId);
            qa.addActionItem(chart);
        }
        setItemListGravity(qa, Gravity.CENTER);
        qa.show();
    }

    /**
     * 無理やりショートカットアイコンリストをセンタリングします。
     * <p>
     * {@code quickaction.xml} に含まれている、{@code id} が {@code tracks} な
     * {@link LinearLayout} に対して、 layout_gravity をセットするメソッドです。
     * </p>
     * 
     * @param qa ショートカットアイコンリストを表示する {@link QuickAction}。
     * @param gravity {@link Gravity} に定義された定数。
     */
    private void setItemListGravity(QuickAction qa, int gravity) {
        try {
            final Field mTrackField = qa.getClass().getDeclaredField("mTrack");
            mTrackField.setAccessible(true);
            final LinearLayout tracks = (LinearLayout) mTrackField.get(qa);
            ((FrameLayout.LayoutParams) tracks.getLayoutParams()).gravity = gravity;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "failed to set icon list gravity. ignored.", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "failed to set icon list gravity. ignored.", e);
        } catch (SecurityException e) {
            Log.e(TAG, "failed to set icon list gravity. ignored.", e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "failed to set icon list gravity. ignored.", e);
        }
    }

    /**
     * {@link QuickAction} に表示する、ショートカットアイコン候補を構築します。
     * 候補は、クリックされるとショートカット作成インテントをリザルトとしてセットして {@link CreateShortcutActivity}
     * を終了します。
     * 
     * @param appInfo 対象アプリ情報。
     * @param badgeResId バッジリソースID. {@link #NO_BADGE} はバッジなしを表します。
     * @return {@link ActionItem}。
     */
    private ActionItem buildCandidate(final AppInfo appInfo, int badgeResId) {
        final BitmapDrawable bd = (BitmapDrawable) appInfo.getIcon();
        final Bitmap originalIcon = bd.getBitmap();

        final Bitmap shortcutIcon = createShortcutIcon(originalIcon, badgeResId);

        final ActionItem item = new ActionItem();
        item.setIcon(new BitmapDrawable(shortcutIcon));
        item.setOnClickListener(new View.OnClickListener() {
            /**
             * アイコンが選択されたので、選択されたアイコンでショートカットを作成し アクティビティ自体を終了する。
             */
            @Override
            public void onClick(View v) {
                final Intent result = buildResultIntent(appInfo, shortcutIcon);
                CreateShortcutActivity.this.setResult(RESULT_OK, result);
                CreateShortcutActivity.this.finish();
            }
        });

        return item;
    }

    /**
     * 指定されたバッジ付きのショートカットアイコンを作成します。
     * 
     * @param originalIcon 対象アプリのオリジナルアイコン。
     * @param badgeResId バッジに使用するリソースの識別子。 {@link #NO_BADGE} が
     *            渡された場合はバッジなしでアイコンを作成します。
     * @return ショートカットアイコンお {@link Bitmap} オブジェクト。 必ず新たに作成された {@link Bitmap}
     *         オブジェクトが返ります。
     */
    private Bitmap createShortcutIcon(Bitmap originalIcon, int badgeResId) {
        final Bitmap shortcutIcon = Bitmap.createBitmap(originalIcon.getWidth(),
                originalIcon.getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(shortcutIcon);
        canvas.drawBitmap(originalIcon, 0, 0, null);

        if (badgeResId == NO_BADGE) {
            // バッジなしなので、そのまま帰す。
            return shortcutIcon;
        }

        // バッジを重ねる
        final Bitmap badge = BitmapFactory.decodeResource(getResources(), badgeResId);
        try {
            final Matrix m = new Matrix();
            final float originalX = originalIcon.getWidth();
            final float badgeX = badge.getWidth();
            final float ratioX = originalX / badgeX;
            final float originalY = originalIcon.getHeight();
            final float badgeY = badge.getHeight();
            final float ratioY = originalY / badgeY;

            // 縦横比を維持するため、比が小さい方を縦横両方に採用してスケーリングする
            final float ratio = Math.min(ratioX, ratioY);
            m.postScale(ratio, ratio);
            canvas.drawBitmap(badge, m, null);
        } finally {
            badge.recycle();
        }

        return shortcutIcon;
    }

    /**
     * このアクティビティの {@code result} として使用される、ショートカット作成インテントを 構築して返します。
     * 
     * @param appInfo 作成するショートカットが対象とするアプリ情報。
     * @param icon ショートカットセットするアイコン。
     * @return {@code result} インテント。
     */
    private Intent buildResultIntent(AppInfo appInfo, Bitmap icon) {
        // ショートカット作成
        final Intent shortcutIntent = new Intent("org.zakky.stickyshortcut.LAUNCH");
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(getApplicationContext().getPackageName(),
                LauncherActivity.class.getCanonicalName());

        shortcutIntent.putExtra(EXTRA_TARGET_PACKAGE, appInfo.getPackageName());
        shortcutIntent.putExtra(EXTRA_TARGET_FQCN, appInfo.getActivityFqcn());
        shortcutIntent.putExtra(EXTRA_TARGET_LABEL, appInfo.getLabel());

        // 作成したショートカットを設定するIntent。ここでショートカット名とアイコンも設定。
        final Intent result = new Intent();
        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        result.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.getLabel());

        return result;
    }

    /**
     * アプリ一覧を取得し、 {@value CreateShortcutActivity#appGrid_} にセットするタスクです。
     * <p>
     * 取得処理中はキャンセル不可なプログレスダイアログを表示します。
     * </p>
     * 
     * @author zaki
     */
    private final class LoadAppListTask extends AsyncTask<Void, Void, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(Void... v) {
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager pm = getPackageManager();
            final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
            final List<AppInfo> appList = new ArrayList<AppInfo>(apps.size());
            for (ResolveInfo info : apps) {
                final String packageName = info.activityInfo.packageName;
                if (packageName == null) {
                    continue;
                }
                final String activityFqcn = info.activityInfo.name;
                final CharSequence label = info.loadLabel(pm);
                final Drawable icon = info.activityInfo.loadIcon(pm);

                final AppInfo appInfo = new AppInfo(label, icon, activityFqcn, packageName);
                appList.add(appInfo);
            }

            return appList;
        }

        @Override
        protected final void onPostExecute(List<AppInfo> appList) {
            final AppsAdapter adapter = new AppsAdapter(getApplicationContext(), appList);
            appGrid_.setAdapter(adapter);
            dismissProgress();
        }
    }

    /**
     * アプリ一覧に表示される１つのアプリの情報を保持するクラスです。
     * 
     * @author zaki
     */
    private static final class AppInfo {
        private final CharSequence label_;

        private final Drawable icon_;

        private final String activityFqcn_;

        private final String packageName_;

        public AppInfo(CharSequence label, Drawable icon, String activityFqcn, String packageName) {
            super();
            label_ = label;
            icon_ = icon;
            activityFqcn_ = activityFqcn;
            packageName_ = packageName;
        }

        public CharSequence getLabel() {
            return label_;
        }

        public Drawable getIcon() {
            return icon_;
        }

        public String getActivityFqcn() {
            return activityFqcn_;
        }

        public String getPackageName() {
            return packageName_;
        }

    }

    /**
     * {@link GridView} に対してアプリ一覧を提供するアダプタです。
     * 
     * @author zaki
     */
    @DefaultAnnotation(NonNull.class)
    public static final class AppsAdapter extends BaseAdapter {

        private final List<AppInfo> apps_;

        private final LayoutInflater inflater_;

        private final LinearLayout.LayoutParams params_;

        public AppsAdapter(Context context, List<AppInfo> apps) {
            apps_ = apps;
            inflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 画面サイズを取得
            int wallpaperSizeY = context.getWallpaperDesiredMinimumHeight();
            if (wallpaperSizeY >= 960) {
                params_ = new LinearLayout.LayoutParams(72, 72);
            } else if (wallpaperSizeY >= 800) {
                params_ = new LinearLayout.LayoutParams(60, 60);
            } else if (wallpaperSizeY >= 480) {
                params_ = new LinearLayout.LayoutParams(44, 44);
            } else {
                params_ = new LinearLayout.LayoutParams(32, 32);
            }
        }

        /**
         * アプリ1つ分を表現する {@link View} を返します。
         * 
         * @param position
         * アイテムのインデックス。 0 ベース。
         * @param convertView
         * これまで使用されていた {@link View} オブジェクト。 {@code null} の可能性あり。
         * 可能なかぎり再利用すること。
         * @param parent
         * 対象とする {@link View} の親。
         * @return {@link View} オブジェクト。
         */
        public View getView(int position, @CheckForNull View convertView, ViewGroup parent) {
            final View v = (convertView == null) ? inflater_.inflate(R.layout.grid_row, null)
                    : convertView;
            final GridRowData rowData = (v.getTag() == null) ? createRowData(v) : (GridRowData) v
                    .getTag();

            final AppInfo info = getItem(position);
            rowData.getTextView().setText(info.getLabel());
            rowData.getImageView().setImageDrawable(info.getIcon());

            v.setTag(rowData);
            return v;
        }

        private GridRowData createRowData(View rowView) {
            final TextView text = (TextView) rowView.findViewById(R.id.grid_row_txt);
            final ImageView image = (ImageView) rowView.findViewById(R.id.grid_row_img);
            image.setLayoutParams(params_);

            final GridRowData rowData = new GridRowData(text, image);
            return rowData;
        }

        public final int getCount() {
            return apps_.size();
        }

        public final AppInfo getItem(int position) {
            return apps_.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }

        private static final class GridRowData {
            private final TextView text_;

            private final ImageView image_;

            public GridRowData(TextView text, ImageView image) {
                text_ = text;
                image_ = image;
            }

            public TextView getTextView() {
                return text_;
            }

            public ImageView getImageView() {
                return image_;
            }
        }
    }
}
