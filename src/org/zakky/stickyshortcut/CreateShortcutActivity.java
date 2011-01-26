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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.zakky.stickyshortcut.icon.BadgeIconCreator;
import org.zakky.stickyshortcut.icon.SandwichIconCreator;
import org.zakky.stickyshortcut.icon.SandwichIconCreator.IconInfo;
import org.zakky.stickyshortcut.icon.ShortcutIconCreator;

import yanzm.products.quickaction.lib.ActionItem;
import yanzm.products.quickaction.lib.QuickAction;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * ショートカット作成時に呼び出され、ユーザが選択したアプリを起動するショートカットをホームに 作成します。
 *
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
public final class CreateShortcutActivity extends Activity implements OnItemClickListener {

    /** ドロイド君ショートカットアイコンを作成する際の、オリジナルアイコンの拡大率 */
    private static final float SCALE_FOR_DROID = 0.9f;

    /** 矢印ショートカットアイコンを作成する際の、オリジナルアイコンの拡大率 */
    private static final float SCALE_FOR_ARROW = 0.92f;

    /**
     * バッジアイコンリスト。
     */
    private static final IconInfo[] ICON_INFO_LIST = {
            new IconInfo(R.drawable.arrow_dro02_72, R.drawable.arrow_dro01_72,
                    R.drawable.arrow_dro02_60, R.drawable.arrow_dro01_60,
                    R.drawable.arrow_dro02_48, R.drawable.arrow_dro01_48,
                    R.drawable.arrow_dro02_44, R.drawable.arrow_dro01_44,
                    R.drawable.arrow_dro02_36, R.drawable.arrow_dro01_36,
                    R.drawable.arrow_dro02_32, R.drawable.arrow_dro01_32, SCALE_FOR_DROID, 0.0f,
                    1.0f - SCALE_FOR_DROID),
            new IconInfo(R.drawable.arrow_blue02_72, R.drawable.arrow_blue01_72,
                    R.drawable.arrow_blue02_60, R.drawable.arrow_blue01_60,
                    R.drawable.arrow_blue02_48, R.drawable.arrow_blue01_48,
                    R.drawable.arrow_blue02_44, R.drawable.arrow_blue01_44,
                    R.drawable.arrow_blue02_36, R.drawable.arrow_blue01_36,
                    R.drawable.arrow_blue02_32, R.drawable.arrow_blue01_32, SCALE_FOR_ARROW,
                    1.0f - SCALE_FOR_ARROW, 0.0f),
            new IconInfo(R.drawable.arrow_green02_72, R.drawable.arrow_green01_72,
                    R.drawable.arrow_green02_60, R.drawable.arrow_green01_60,
                    R.drawable.arrow_green02_48, R.drawable.arrow_green01_48,
                    R.drawable.arrow_green02_44, R.drawable.arrow_green01_44,
                    R.drawable.arrow_green02_36, R.drawable.arrow_green01_36,
                    R.drawable.arrow_green02_32, R.drawable.arrow_green01_32, SCALE_FOR_ARROW,
                    1.0f - SCALE_FOR_ARROW, 0.0f),
            new IconInfo(R.drawable.arrow_pink02_72, R.drawable.arrow_pink01_72,
                    R.drawable.arrow_pink02_60, R.drawable.arrow_pink01_60,
                    R.drawable.arrow_pink02_48, R.drawable.arrow_pink01_48,
                    R.drawable.arrow_pink02_44, R.drawable.arrow_pink01_44,
                    R.drawable.arrow_pink02_36, R.drawable.arrow_pink01_36,
                    R.drawable.arrow_pink02_32, R.drawable.arrow_pink01_32, SCALE_FOR_ARROW,
                    1.0f - SCALE_FOR_ARROW, 0.0f),
            new IconInfo(R.drawable.arrow_white02_72, R.drawable.arrow_white01_72,
                    R.drawable.arrow_white02_60, R.drawable.arrow_white01_60,
                    R.drawable.arrow_white02_48, R.drawable.arrow_white01_48,
                    R.drawable.arrow_white02_44, R.drawable.arrow_white01_44,
                    R.drawable.arrow_white02_36, R.drawable.arrow_white01_36,
                    R.drawable.arrow_white02_32, R.drawable.arrow_white01_32, SCALE_FOR_ARROW,
                    1.0f - SCALE_FOR_ARROW, 0.0f),
            new IconInfo(R.drawable.arrow_black02_72, R.drawable.arrow_black01_72,
                    R.drawable.arrow_black02_60, R.drawable.arrow_black01_60,
                    R.drawable.arrow_black02_48, R.drawable.arrow_black01_48,
                    R.drawable.arrow_black02_44, R.drawable.arrow_black01_44,
                    R.drawable.arrow_black02_36, R.drawable.arrow_black01_36,
                    R.drawable.arrow_black02_32, R.drawable.arrow_black01_32, SCALE_FOR_ARROW,
                    1.0f - SCALE_FOR_ARROW, 0.0f),
    };

    private static final int[] ICON_SIZE_CONFIG = {
            makeConfig(960, 72), makeConfig(800, 60), makeConfig(480, 44), makeConfig(0, 32),
    };

    private static int makeConfig(int border, int size) {
        if (border < 0 || size < 0) {
            throw new RuntimeException("unexpected icon size config. border=" + border + ", size="
                    + size);
        }
        final int config = (border << 16) | (size & 0xFFFF);
        return config;
    }

    private static int getBorderFromConfig(int config) {
        return (config >> 16) & 0xFFFF;
    }

    private static int getSizeFromConfig(int config) {
        return (config & 0xFFFF);
    }

    /**
     * アプリ一覧表示用グリッド。
     */
    private GridView appGrid_;

    /**
     * アプリ一覧グリッド構築時のプログレス
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
     * <p>
     * このメソッドが正常に完了した後は、 {@link #progressDialog_} が {@code null} に なります。
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
        final long start = System.nanoTime();

        ActionItem chart = buildIconCandidate(appInfo, new BadgeIconCreator(
                BadgeIconCreator.NO_BADGE));
        qa.addActionItem(chart);
        for (IconInfo iconInfo : ICON_INFO_LIST) {
            chart = buildIconCandidate(appInfo, new SandwichIconCreator(iconInfo));
            qa.addActionItem(chart);
        }
        final long end = System.nanoTime();

        Log.i("DEBUG", "t: " + TimeUnit.NANOSECONDS.toMillis(end - start) + "ms");
        qa.show();
    }

    /**
     * {@link QuickAction} に表示する、ショートカットアイコン候補を構築します。
     * 候補は、クリックされるとショートカット作成インテントをリザルトとしてセットして {@link CreateShortcutActivity}
     * を終了します。
     *
     * @param appInfo 対象アプリ情報。
     * @return {@link ActionItem}。
     */
    private ActionItem buildIconCandidate(final AppInfo appInfo, ShortcutIconCreator builder) {
        final BitmapDrawable bd = (BitmapDrawable) appInfo.getIcon();
        final Bitmap originalIcon = bd.getBitmap();

        final Bitmap shortcutIcon = builder.build(getApplicationContext(), originalIcon);

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

                final AppInfo appInfo = new AppInfo(label.toString(), icon, activityFqcn,
                        packageName);
                appList.add(appInfo);
            }
            Collections.sort(appList, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo app1, AppInfo app2) {
                    return app1.getLabel().compareTo(app2.getLabel());
                }
            });

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
        /** アプリケーションのラベル */
        private final String label_;

        /** アプリケーションのアイコン */
        private final Drawable icon_;

        /** アプリケーションの FQCN */
        private final String activityFqcn_;

        /** アプリケーションのパッケージ名 */
        private final String packageName_;

        public AppInfo(String label, Drawable icon, String activityFqcn, String packageName) {
            super();
            label_ = label;
            icon_ = icon;
            activityFqcn_ = activityFqcn;
            packageName_ = packageName;
        }

        /**
         * アプリケーションのラベルを返します。
         *
         * @return ラベル。
         */
        public String getLabel() {
            return label_;
        }

        /**
         * アプリケーションのアイコンを返します。
         *
         * @return アイコン。
         */
        public Drawable getIcon() {
            return icon_;
        }

        /**
         * アプリケーションの FQCN を返します。
         *
         * @return FQCN
         */
        public String getActivityFqcn() {
            return activityFqcn_;
        }

        /**
         * アプリケーションのパッケージ名を返します。
         *
         * @return パッケージ名。
         */
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

        /**
         * アプリ一覧
         */
        private final List<AppInfo> apps_;

        /**
         * グリッドの要素を生成するためのインフレータ。
         */
        private final LayoutInflater inflater_;

        /**
         * グリッドにアプリアイコンを表示する際のサイズを保持するパラメータ。
         */
        private final LinearLayout.LayoutParams params_;

        /**
         * 指定されたアプリ一覧を提供する {@link AppsAdapter} を構築します。
         *
         * @param appContext アプリケーションコンテキスト。 コンストラクタ内でのみ使用し、参照は保持しません。
         * @param apps 表示するアプリケーションのリスト。
         *            渡されたリストは、アダプター内で保持します。以降呼び出し側で変更しないことを前提にしています。
         */
        public AppsAdapter(Context appContext, List<AppInfo> apps) {
            apps_ = apps;
            inflater_ = (LayoutInflater) appContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 画面サイズを取得
            final int iconSize = getIconSize(appContext.getWallpaperDesiredMinimumWidth(),
                    appContext.getWallpaperDesiredMinimumHeight());
            params_ = new LinearLayout.LayoutParams(iconSize, iconSize);
        }

        /**
         * 画面の大きさから、適切なアイコンのピクセル数を決定します。
         *
         * @param wallpaperWidth 画面の幅。
         * @param wallpaperHeight 画面の高さ。
         * @return アイコン画像のいっぺんのピクセル数。
         */
        private static int getIconSize(int wallpaperWidth, int wallpaperHeight) {
            for (int config : ICON_SIZE_CONFIG) {
                final int border = getBorderFromConfig(config);
                if (wallpaperHeight < border) {
                    continue;
                }
                final int size = getSizeFromConfig(config);
                return size;
            }
            throw new RuntimeException("failed to determine icon size. wallpaperWidth="
                    + wallpaperWidth + ", wallpaperHeight=" + wallpaperHeight);
        }

        /**
         * アプリ1つ分を表現する {@link View} を返します。
         *
         * @param position アイテムのインデックス。 0 ベース。
         * @param convertView これまで使用されていた {@link View} オブジェクト。 {@code null}
         *            の可能性あり。 可能なかぎり再利用すること。
         * @param parent 対象とする {@link View} の親。
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

        /**
         * グリッド内の UI コンポーネントを取り出し、 {@link GridRowData} として返します。
         *
         * @param rowView アプリ1つ分のView。
         * @return {@link View} から取り出した UI コンポーネントを保持する {@link GridRowData}。
         */
        private GridRowData createRowData(View rowView) {
            final TextView text = (TextView) rowView.findViewById(R.id.grid_row_txt);
            final ImageView image = (ImageView) rowView.findViewById(R.id.grid_row_img);
            image.setLayoutParams(params_);

            final GridRowData rowData = new GridRowData(text, image);
            return rowData;
        }

        /**
         * アダプタが保持するアプリの数を返します。
         */
        public final int getCount() {
            return apps_.size();
        }

        /**
         * 指定されたインデックスの {@link AppInfo} を返します。
         *
         * @return 指定されたインデックスに対応する {@link AppInfo}。
         * @throws IndexOutOfBoundsException 指定されたインデックスが、 {@code 0} 以上
         *             {@link #getCount()} 未満の範囲かた外れている場合。
         */
        public final AppInfo getItem(int position) {
            if (position < 0 || getCount() <= position) {
                throw new IndexOutOfBoundsException();
            }
            return apps_.get(position);
        }

        /**
         * インデックスをアイテムIDに変換します。
         */
        public final long getItemId(int position) {
            return position;
        }

        /**
         * グリッドに属するUIコンポーネントを束ねるクラスです。
         * <p>
         * グリッドのセル１つを構成する {@link View} に簡単にアクセスできるように、 関係する {@link View}
         * をメンバ変数として保持するクラスです。
         * <p>
         *
         * @author zaki
         */
        @DefaultAnnotation(NonNull.class)
        private static final class GridRowData {
            /**
             * アプリのラベルを保持する {@link View}。
             */
            private final TextView text_;

            /**
             * アプリのアイコンを保持する {@link View}。
             */
            private final ImageView image_;

            public GridRowData(TextView text, ImageView image) {
                if (text == null) {
                    throw new IllegalArgumentException("'text' must not be null");
                }
                if (image == null) {
                    throw new IllegalArgumentException("'image' must not be null");
                }
                text_ = text;
                image_ = image;
            }

            /**
             * アプリラベルを保持する {@link View} を返します。
             *
             * @return {@link TextView}
             */
            public TextView getTextView() {
                return text_;
            }

            /**
             * アプリアイコンを保持する {@link View} を返します。
             *
             * @return {@link ImageView}
             */
            public ImageView getImageView() {
                return image_;
            }
        }
    }
}
