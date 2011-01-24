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
import android.content.Context;
import android.graphics.Bitmap;

/**
 * ショートカットアイコンの {@link Bitmap} を作成するクラスの共通インタフェースです。
 *
 * @author zaki
 */
@DefaultAnnotation(NonNull.class)
public interface ShortcutIconCreator {

    /**
     * ショートカットアイコン作成時に呼び出されます。
     *
     * @param appContext アプリケーションコンテキスト。 {@code null} 禁止。
     * @param originalIcon ショートカットアプリアイコン。 {@code null} 禁止。
     * @return 作成されたショートカットアイコン。 必ず新たに作成された {@link Bitmap} オブジェクトが返ります。
     */
    public Bitmap build(Context appContext, Bitmap originalIcon);
}
