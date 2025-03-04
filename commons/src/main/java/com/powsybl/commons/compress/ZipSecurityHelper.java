/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.powsybl.commons.datasource.ReadOnlyDataSource;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
public final class ZipSecurityHelper {

    public static final int THRESHOLD_ENTRIES = 10000;
    public static final int THRESHOLD_SIZE = 1_610_612_736; // 1.5 GB
    public static final double THRESHOLD_RATIO = 20;

    private ZipSecurityHelper() {
    }

    public static void checkIfZipExtractionIsSafe(ReadOnlyDataSource dataSource, String name) {
        checkIfZipExtractionIsSafe(dataSource, name, THRESHOLD_ENTRIES, THRESHOLD_SIZE, THRESHOLD_RATIO);
    }

    public static void checkIfZipExtractionIsSafe(ReadOnlyDataSource dataSource, String name, int thresholdEntries, int thresholdSize, double thresholdCompressionRatio) {
        try (ZipInputStream is = new ZipInputStream(dataSource.newInputStream(name))) {
            if (!ZipSecurityHelper.isZipFileSafe(is, thresholdEntries, thresholdSize, thresholdCompressionRatio)) {
                throw new UncheckedIOException("Zip file extraction is not safe", new IOException());
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static boolean isZipFileSafe(ZipInputStream zipInputStream) throws IOException {
        return isZipFileSafe(zipInputStream, THRESHOLD_ENTRIES, THRESHOLD_SIZE, THRESHOLD_RATIO);
    }

    public static boolean isZipFileSafe(ZipInputStream zipInputStream, int thresholdEntries, int thresholdSize, double thresholdCompressionRatio) throws IOException {
        int totalSizeArchive = 0;
        int totalEntryArchive = 0;
        ZipEntry ze = zipInputStream.getNextEntry();
        while (ze != null) {
            totalEntryArchive++;

            ZipEntry currentEntry = ze;
            ze = zipInputStream.getNextEntry();
            long entrySize = currentEntry.getCompressedSize();
            long uncompressedSize = currentEntry.getSize();
            totalSizeArchive += uncompressedSize;

            if (totalSizeArchive > thresholdSize) {
                // the uncompressed data size is too much for the application resource capacity
                return false;
            }

            if (totalEntryArchive > thresholdEntries) {
                // too many entries in this archive, can lead to inodes exhaustion of the system
                return false;
            }

            double compressionRatio = (double) uncompressedSize / (double) entrySize;
            if (compressionRatio > thresholdCompressionRatio) {
                // ratio between compressed and uncompressed data is highly suspicious, looks
                // like a Zip Bomb Attack
                return false;
            }
        }
        return true;
    }

}
