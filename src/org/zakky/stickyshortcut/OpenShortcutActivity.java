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

import java.util.ArrayList;
import java.util.List;

import org.zakky.stickyshortcut.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ショートカット作成時に呼び出され、ユーザが選択したアプリを起動するショートカットをホームに
 * 作成します。
 * 
 * @author zaki
 */
public final class OpenShortcutActivity extends Activity implements
        OnItemClickListener {

    public static final String EXTRA_TARGET_PACKAGE = "EXTRA_TARGET_PACKAGE";
    public static final String EXTRA_TARGET_FQCN = "EXTRA_TARGET_FQCN";
    public static final String EXTRA_TARGET_LABEL = "EXTRA_TARGET_LABEL";

    /**
     * アプリ一覧をユーザに提示するためのグリッドです。
     */
    private static GridView appGrid_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid);

        appGrid_ = (GridView) findViewById(R.id.grid);
        appGrid_.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final LoadAppListTask task = new LoadAppListTask();
        task.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        final AppInfo appInfo = (AppInfo) parent.getItemAtPosition(position);

        final BitmapDrawable bd = (BitmapDrawable) appInfo.getIcon();
        final Bitmap bmpBack = bd.getBitmap();

        final Bitmap shortcutIcon = Bitmap.createBitmap(bmpBack.getWidth(),
                bmpBack.getHeight(), Bitmap.Config.ARGB_8888);

        // TODO アイコンにバッヂを合成
        //final Bitmap badge = BitmapFactory.decodeResource(getResources(),
        //        R.drawable.icon);
        final Canvas canvas = new Canvas(shortcutIcon);
        canvas.drawBitmap(bmpBack, 0, 0, null);

        // ショートカット作成
        final Intent shortcutIntent = new Intent("org.zakky.stickyshortcut.LAUNCH");
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(getApplicationContext().getPackageName(),
                LauncherActivity.class.getCanonicalName());

        shortcutIntent.putExtra(EXTRA_TARGET_PACKAGE, appInfo.getPackageName());
        shortcutIntent.putExtra(EXTRA_TARGET_FQCN, appInfo.getActivityFqcn());
        shortcutIntent.putExtra(EXTRA_TARGET_LABEL, appInfo.getLabel());

        // 作成したショートカットを設定するIntent。ここでショートカット名とアイコンも設定。
        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, shortcutIcon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.getLabel());

        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * アプリ一覧を取得し、 {@value OpenShortcutActivity#appGrid_} にセットするタスクです。
     * 
     * <p>
     * 取得処理中はキャンセル不可なプログレスダイアログを表示します。
     * </p>
     * 
     * @author zaki
     */
    private final class LoadAppListTask extends
            AsyncTask<Void, Void, List<AppInfo>> {
        private final ProgressDialog progressDialog_;

        public LoadAppListTask() {
            progressDialog_ = new ProgressDialog(OpenShortcutActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog_.setMessage(getResources().getString(
                    R.string.shortcut_progressdialog_title));
            progressDialog_.setCancelable(false);
            progressDialog_.show();
        }

        @Override
        protected List<AppInfo> doInBackground(Void... v) {
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager pm = getPackageManager();
            final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent,
                    0);
            final List<AppInfo> appList = new ArrayList<AppInfo>(apps.size());
            for (ResolveInfo info : apps) {
                final CharSequence label = info.loadLabel(pm);
                final Drawable icon = info.activityInfo.loadIcon(pm);
                final String activityFqcn = info.activityInfo.name;
                final String packageName = info.activityInfo.packageName;

                final AppInfo appInfo = new AppInfo(label, icon, activityFqcn,
                        packageName);
                appList.add(appInfo);
            }

            return appList;
        }

        @Override
        protected final void onPostExecute(List<AppInfo> appList) {
            final AppsAdapter adapter = new AppsAdapter(
                    getApplicationContext(), appList);
            appGrid_.setAdapter(adapter);
            progressDialog_.dismiss();
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

        public AppInfo(CharSequence label, Drawable icon, String activityFqcn,
                String packageName) {
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
    public static final class AppsAdapter extends BaseAdapter {

        private final List<AppInfo> apps_;

        private final LayoutInflater inflater_;
        private final LinearLayout.LayoutParams params_;

        public AppsAdapter(Context context, List<AppInfo> apps) {
            apps_ = apps;
            inflater_ = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 画面サイズを取得
            int wallpaperSizeY = context.getWallpaperDesiredMinimumHeight();
            if (wallpaperSizeY >= 800) {
                params_ = new LinearLayout.LayoutParams(60, 60);
            } else if (wallpaperSizeY >= 480) {
                params_ = new LinearLayout.LayoutParams(44, 44);
            } else {
                params_ = new LinearLayout.LayoutParams(32, 32);
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final View v = (convertView == null) ? inflater_.inflate(
                    R.layout.grid_row, null) : convertView;
            final GridRowData rowData = (v.getTag() == null) ? createRowData(v)
                    : (GridRowData) v.getTag();

            final AppInfo info = getItem(position);
            rowData.getTextView().setText(info.getLabel());
            rowData.getImageView().setImageDrawable(info.getIcon());

            v.setTag(rowData);
            return v;
        }

        private GridRowData createRowData(View rowView) {
            final TextView text = (TextView) rowView
                    .findViewById(R.id.grid_row_txt);
            final ImageView image = (ImageView) rowView
                    .findViewById(R.id.grid_row_img);
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