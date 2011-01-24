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

package org.zakky.stickyshortcut.icon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * 指定されたリソースの画像をバッジとしてオリジナルアイコンに重ねるアイコンクリエータです。
 *
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
public final class SandwichIconCreator implements ShortcutIconCreator {

    private final IconInfo info_;

    /**
     * 指定された {@link IconInfo} を元にショートカットアイコンを作成するクリエータを構築します。
     *
     * @param info アイコン情報。
     */
    public SandwichIconCreator(IconInfo info) {
        super();
        info_ = info;
    }

    @Override
    public Bitmap build(Context appContext, Bitmap originalIcon) {
        final Bitmap shortcutIcon = Bitmap.createBitmap(originalIcon.getWidth(),
                originalIcon.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(shortcutIcon);

        /*
         * ベース -> アプリアイコン -> 矢印 の順に描画する
         */

        final Matrix badgeMatrix = new Matrix();

        final Bitmap base;
        base = BitmapFactory.decodeResource(appContext.getResources(), info_.base_);
        try {
            final float ratio = ShortcutIconUtil.calcRatio(originalIcon.getWidth(),
                    originalIcon.getHeight(), base.getWidth(), base.getHeight());
            badgeMatrix.postScale(ratio, ratio);
            canvas.drawBitmap(base, badgeMatrix, null);
        } finally {
            base.recycle();
        }

        final Matrix appIconMatrix = new Matrix();
        appIconMatrix.postScale(info_.scale_, info_.scale_);
        appIconMatrix.postTranslate(originalIcon.getWidth() * info_.leftMergin_,
                originalIcon.getWidth() * info_.topMergin_);
        canvas.drawBitmap(originalIcon, appIconMatrix, null);

        final Bitmap arrow;
        arrow = BitmapFactory.decodeResource(appContext.getResources(), info_.arrow_);
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
}
