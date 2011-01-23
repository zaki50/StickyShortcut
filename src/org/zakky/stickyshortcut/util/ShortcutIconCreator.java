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

package org.zakky.stickyshortcut.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * ショートカットアイコンを作成するユーティリティクラスです。
 * 
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
public final class ShortcutIconCreator {

    /**
     * バッジ無しを表すリソース識別子。
     */
    public static final int NO_BADGE = -1;

    /**
     * 指定されたバッジ付きのショートカットアイコンを作成します。
     * 
     * @param appContext アプリケーションコンテキスト。
     * @param originalIcon 対象アプリのオリジナルアイコン。
     * @param badgeResId バッジに使用するリソースの識別子。 {@link #NO_BADGE} が
     *            渡された場合はバッジなしでアイコンを作成します。
     * @return ショートカットアイコンの {@link Bitmap} オブジェクト。 必ず新たに作成された {@link Bitmap}
     *         オブジェクトが返ります。
     */
    public static Bitmap create(Context appContext, Bitmap originalIcon, int badgeResId) {
        final Bitmap shortcutIcon = Bitmap.createBitmap(originalIcon.getWidth(),
                originalIcon.getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(shortcutIcon);
        canvas.drawBitmap(originalIcon, 0, 0, null);

        if (badgeResId == NO_BADGE) {
            // バッジなしなので、そのまま帰す。
            return shortcutIcon;
        }

        // バッジを重ねる
        final Bitmap badge;
        badge = BitmapFactory.decodeResource(appContext.getResources(), badgeResId);
        try {
            final Matrix m = new Matrix();

            // 縦横比を維持するため、比が小さい方を縦横両方に採用してスケーリングする
            final float ratio = calcRatio(originalIcon.getWidth(), originalIcon.getHeight(),
                    badge.getWidth(), badge.getHeight());
            m.postScale(ratio, ratio);
            canvas.drawBitmap(badge, m, null);
        } finally {
            badge.recycle();
        }

        return shortcutIcon;
    }

    /**
     * かめこスペシャルなショートカットアイコンを作成します。
     * 
     * @param appContext アプリケーションコンテキスト。
     * @param originalIcon 対象アプリのオリジナルアイコン。
     * @param info アイコン情報。
     * @return ショートカットアイコンの {@link Bitmap} オブジェクト。 必ず新たに作成された {@link Bitmap}
     *         オブジェクトが返ります。
     */
    public static Bitmap createKamekoSpecial(Context appContext, Bitmap originalIcon, IconInfo info) {
        final Bitmap shortcutIcon = Bitmap.createBitmap(originalIcon.getWidth(),
                originalIcon.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(shortcutIcon);

        /*
         * ベース -> アプリアイコン -> 矢印 の順に描画する
         */

        final Matrix badgeMatrix = new Matrix();

        final Bitmap base;
        base = BitmapFactory.decodeResource(appContext.getResources(), info.base_);
        try {
            final float ratio = calcRatio(originalIcon.getWidth(), originalIcon.getHeight(),
                    base.getWidth(), base.getHeight());
            badgeMatrix.postScale(ratio, ratio);
            canvas.drawBitmap(base, badgeMatrix, null);
        } finally {
            base.recycle();
        }

        final Matrix appIconMatrix = new Matrix();
        appIconMatrix.postScale(info.scale_, info.scale_);
        appIconMatrix.postTranslate(originalIcon.getWidth() * info.leftMergin_,
                originalIcon.getWidth() * info.topMergin_);
        canvas.drawBitmap(originalIcon, appIconMatrix, null);

        final Bitmap arrow;
        arrow = BitmapFactory.decodeResource(appContext.getResources(), info.arrow_);
        try {
            assert base.getWidth() == arrow.getWidth();
            assert base.getHeight() == arrow.getHeight();
            // サイズは同じという前提でマトリックスを再利用

            canvas.drawBitmap(arrow, badgeMatrix, null);
        } finally {
            arrow.recycle();
        }

        return shortcutIcon;
    }

    /**
     * バッジをオリジナルアイコンと同じ大きさにするための拡大率を返します。
     * 
     * @param originalX オリジナルアイコンのX軸方向のピクセル数。
     * @param originalY オリジナルアイコンのY軸方向のピクセル数。
     * @param badgeX バッジアイコンのX軸方向のピクセル数。
     * @param badgeY バッジアイコンのY軸方向のピクセル数。
     * @return 拡大率。
     */
    private static float calcRatio(int originalX, int originalY, int badgeX, int badgeY) {
        final float ratioX = (float) originalX / (float) badgeX;
        final float ratioY = (float) originalY / (float) badgeY;
        final float ratio = Math.min(ratioX, ratioY);

        return ratio;
    }

    /**
     * ショートカットアイコンを作成する際のリソース/レイアウトの情報を保持します。
     */
    @DefaultAnnotation(NonNull.class)
    public static final class IconInfo {

        /** アイコンの下に描画する画像のリソース識別子。 */
        public final int base_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        public final int arrow_;

        /** オリジナルアイコンの拡大率。 */
        public final float scale_;

        /** オリジナルアイコンの描画位置のトップマージン。 {@code 0.0f <= topMergin_ <= 1.0f} */
        public final float topMergin_;

        /** オリジナルアイコンの描画位置のレフトマージン。 {@code 0.0f <= topMergin_ <= 1.0f} */
        public final float leftMergin_;

        /**
         * 指定された情報を保持する {@link IconInfo} を構築します。
         * 
         * @param base アイコンの下に描画する画像のリソース識別子。
         * @param arrow アイコンの上に描画する画像のリソース識別子。
         * @param scale オリジナルアイコンの拡大率。
         * @param leftMergin オリジナルアイコンの描画位置のレフトマージン。
         *            {@code 0.0f <= topMergin_ <= 1.0f}
         * @param topMergin オリジナルアイコンの描画位置のトップマージン。
         *            {@code 0.0f <= topMergin_ <= 1.0f}
         */
        public IconInfo(int base, int arrow, float scale, float leftMergin, float topMergin) {
            super();
            base_ = base;
            arrow_ = arrow;
            scale_ = scale;
            topMergin_ = topMergin;
            leftMergin_ = leftMergin;
        }
    }

    /**
     * インスタンス作成禁止
     */
    private ShortcutIconCreator() {
        throw new AssertionError("instantiation prohibited");
    }
}
