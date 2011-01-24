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

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * ショートカットアイコンを作成するユーティリティクラスです。
 *
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
final class ShortcutIconUtil {

    /**
     * バッジをオリジナルアイコンと同じ大きさにするための拡大率を返します。
     *
     * @param originalX オリジナルアイコンのX軸方向のピクセル数。
     * @param originalY オリジナルアイコンのY軸方向のピクセル数。
     * @param badgeX バッジアイコンのX軸方向のピクセル数。
     * @param badgeY バッジアイコンのY軸方向のピクセル数。
     * @return 拡大率。
     */
    static float calcRatio(int originalX, int originalY, int badgeX, int badgeY) {
        final float ratioX = (float) originalX / (float) badgeX;
        final float ratioY = (float) originalY / (float) badgeY;
        final float ratio = Math.min(ratioX, ratioY);

        return ratio;
    }

    /**
     * インスタンス作成禁止
     */
    private ShortcutIconUtil() {
        throw new AssertionError("instantiation prohibited");
    }
}
