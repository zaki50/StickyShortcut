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
        final Bitmap base;
        base = BitmapFactory.decodeResource(appContext.getResources(),
                info_.getBaseIconResId(shortcutIcon.getWidth()));
        try {
            if (base.getWidth() == shortcutIcon.getWidth()
                    && base.getHeight() == shortcutIcon.getHeight()) {
                // アプリアイコンとバッジの大きさが異なる場合(通常はこっち)
                canvas.drawBitmap(base, 0, 0, null);
            } else {
                // アプリアイコンとバッジの大きさが異なる場合(レアケース)
                final float scale = ShortcutIconUtil.calcRatio(shortcutIcon.getWidth(),
                        shortcutIcon.getHeight(), base.getWidth(), base.getHeight());
                final Matrix m = new Matrix();
                m.postScale(scale, scale);
                canvas.drawBitmap(base, m, null);
            }
        } finally {
            base.recycle();
        }

        final Matrix appIconMatrix = new Matrix();
        appIconMatrix.postScale(info_.scale_, info_.scale_);
        appIconMatrix.postTranslate(originalIcon.getWidth() * info_.leftMergin_,
                originalIcon.getWidth() * info_.topMergin_);
        canvas.drawBitmap(originalIcon, appIconMatrix, null);

        final Bitmap arrow;
        arrow = BitmapFactory.decodeResource(appContext.getResources(),
                info_.getArrowIconResId(shortcutIcon.getWidth()));
        try {
            if (arrow.getWidth() == shortcutIcon.getWidth()
                    && arrow.getHeight() == shortcutIcon.getHeight()) {
                // アプリアイコンとバッジの大きさが異なる場合(通常はこっち)
                canvas.drawBitmap(arrow, 0, 0, null);
            } else {
                // アプリアイコンとバッジの大きさが異なる場合(レアケース)
                final float scale = ShortcutIconUtil.calcRatio(shortcutIcon.getWidth(),
                        shortcutIcon.getHeight(), arrow.getWidth(), arrow.getHeight());
                final Matrix m = new Matrix();
                m.postScale(scale, scale);
                canvas.drawBitmap(arrow, m, null);
            }
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
        private final int base72_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        private final int arrow72_;

        /** アイコンの下に描画する画像のリソース識別子。 */
        private final int base60_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        private final int arrow60_;

        /** アイコンの下に描画する画像のリソース識別子。 */
        private final int base48_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        private final int arrow48_;

        /** アイコンの下に描画する画像のリソース識別子。 */
        private final int base44_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        private final int arrow44_;

        /** アイコンの下に描画する画像のリソース識別子。 */
        private final int base36_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        private final int arrow36_;

        /** アイコンの下に描画する画像のリソース識別子。 */
        private final int base32_;

        /** アイコンの上に描画する画像のリソース識別子。 */
        private final int arrow32_;

        /** オリジナルアイコンの拡大率。 */
        public final float scale_;

        /** オリジナルアイコンの描画位置のトップマージン。 {@code 0.0f <= topMergin_ <= 1.0f} */
        public final float topMergin_;

        /** オリジナルアイコンの描画位置のレフトマージン。 {@code 0.0f <= topMergin_ <= 1.0f} */
        public final float leftMergin_;

        /**
         * 指定された情報を保持する {@link IconInfo} を構築します。
         *
         * @param base72 アイコンの下に描画する画像のリソース識別子(72x72)。
         * @param arrow72 アイコンの上に描画する画像のリソース識別子(72x72)。
         * @param base60 アイコンの下に描画する画像のリソース識別子(60x60)。
         * @param arrow60 アイコンの上に描画する画像のリソース識別子(60x60)。
         * @param base48 アイコンの下に描画する画像のリソース識別子(48x48)。
         * @param arrow48 アイコンの上に描画する画像のリソース識別子(48x48)。
         * @param base44 アイコンの下に描画する画像のリソース識別子(44x44)。
         * @param arrow44 アイコンの上に描画する画像のリソース識別子(44x44)。
         * @param base36 アイコンの下に描画する画像のリソース識別子(36x36)。
         * @param arrow36 アイコンの上に描画する画像のリソース識別子(36x36)。
         * @param base32 アイコンの下に描画する画像のリソース識別子(32x32)。
         * @param arrow32 アイコンの上に描画する画像のリソース識別子(32x32)。
         * @param scale オリジナルアイコンの拡大率。
         * @param leftMergin オリジナルアイコンの描画位置のレフトマージン。
         *            {@code 0.0f <= topMergin_ <= 1.0f}
         * @param topMergin オリジナルアイコンの描画位置のトップマージン。
         *            {@code 0.0f <= topMergin_ <= 1.0f}
         */
        public IconInfo(int base72, int arrow72, int base60, int arrow60, int base48, int arrow48,
                int base44, int arrow44, int base36, int arrow36, int base32, int arrow32,
                float scale, float leftMergin, float topMergin) {
            super();
            base72_ = base72;
            arrow72_ = arrow72;
            base60_ = base60;
            arrow60_ = arrow60;
            base48_ = base48;
            arrow48_ = arrow48;
            base44_ = base44;
            arrow44_ = arrow44;
            base36_ = base36;
            arrow36_ = arrow36;
            base32_ = base32;
            arrow32_ = arrow32;
            scale_ = scale;
            topMergin_ = topMergin;
            leftMergin_ = leftMergin;
        }

        public int getBaseIconResId(int iconSize) {
            switch (iconSize) {
                case 72:
                    return base72_;
                case 60:
                    return base60_;
                case 48:
                    return base48_;
                case 44:
                    return base44_;
                case 36:
                    return base36_;
                case 32:
                    return base32_;
                default:
                    return base72_;
            }
        }

        public int getArrowIconResId(int iconSize) {
            switch (iconSize) {
                case 72:
                    return arrow72_;
                case 60:
                    return arrow60_;
                case 48:
                    return arrow48_;
                case 44:
                    return arrow44_;
                case 36:
                    return arrow36_;
                case 32:
                    return arrow32_;
                default:
                    return arrow72_;
            }
        }
    }
}
