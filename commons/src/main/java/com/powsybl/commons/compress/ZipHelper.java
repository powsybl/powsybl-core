/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @deprecated Use {@link ZipPackager} instead.
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
@Deprecated(since = "2.6.0")
public final class ZipHelper {

    /**
     * If the file is in .gz(detected by last 3 characters) format, the method decompresses .gz file first.
     *
     * @param baseDir   the base directory contaions files to zip
     * @param fileNames the files to be added in zip
     * @return bytes in zip format
     * @deprecated Use {@link ZipPackager#archiveFilesToZipBytes(Path, List)} instead.
     */
    @Deprecated
    public static byte[] archiveFilesToZipBytes(Path baseDir, List<String> fileNames) {
        return ZipPackager.archiveFilesToZipBytes(baseDir, fileNames);
    }

    public static byte[] archiveFilesToZipBytes(Path workingDir, String... fileNames) {
        return archiveFilesToZipBytes(workingDir, Arrays.asList(fileNames));
    }

    private ZipHelper() {
    }
}
