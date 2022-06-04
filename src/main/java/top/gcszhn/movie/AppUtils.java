/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package top.gcszhn.movie;

public class AppUtils {
    /**
     * 可读的文件大小
     * @param size 文件字节大小
     * @return 可读的文件大小
     */
    public static String readableFileSize(long size) {
        if (size < 0) return "0.00B";
        final String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        double rfs = size;
        int level = 0;
        while (level < units.length - 1 && rfs >= 1024) {
            rfs /= 1024;
            level ++;
        }
        return String.format("%.2f%s", rfs, units[level]);
    }
}
