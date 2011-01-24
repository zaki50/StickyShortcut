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
public final class BadgeIconCreator implements ShortcutIconCreator {

    /**
     * バッジ無しを表すリソース識別子。
     */
    public static final int NO_BADGE = -1;

    private final int badgeResId_;

    /**
     * アイコンリソースのIDを指定して構築します。
     *
     * @param badgeResId バッジアイコンリソース識別子。バッジ無しの場合は、 {@link #NO_BADGE} を指定してください。
     */
    public BadgeIconCreator(int badgeResId) {
        super();
        badgeResId_ = badgeResId;
    }

    @Override
    public Bitmap build(Context appContext, Bitmap originalIcon) {
        final Bitmap shortcutIcon = Bitmap.createBitmap(originalIcon.getWidth(),
                originalIcon.getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(shortcutIcon);
        canvas.drawBitmap(originalIcon, 0, 0, null);

        if (badgeResId_ == NO_BADGE) {
            // バッジなしなので、そのまま帰す。
            return shortcutIcon;
        }

        // バッジを重ねる
        final Bitmap badge;
        badge = BitmapFactory.decodeResource(appContext.getResources(), badgeResId_);
        try {
            final Matrix m = new Matrix();

            // 縦横比を維持するため、比が小さい方を縦横両方に採用してスケーリングする
            final float ratio = ShortcutIconUtil.calcRatio(originalIcon.getWidth(),
                    originalIcon.getHeight(), badge.getWidth(), badge.getHeight());
            m.postScale(ratio, ratio);
            canvas.drawBitmap(badge, m, null);
        } finally {
            badge.recycle();
        }

        return shortcutIcon;
    }
}
