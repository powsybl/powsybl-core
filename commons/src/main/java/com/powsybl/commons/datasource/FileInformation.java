/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class FileInformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileInformation.class);
    private String baseName;
    private CompressionFormat compressionFormat;
    private ArchiveFormat archiveFormat;
    private String dataExtension;

    FileInformation(String fileName) {
        Objects.requireNonNull(fileName);
        computeInformation(fileName, true);
    }

    FileInformation(String fileName, boolean dataSourceInitialization) {
        Objects.requireNonNull(fileName);
        computeInformation(fileName, dataSourceInitialization);
    }

    private void computeInformation(String fileName, boolean dataSourceInitialization) {
        // Check if filename is empty or only a dot
        if (fileName.isEmpty() || fileName.equals(".")) {
            throw new PowsyblException("File name cannot be empty nor just a dot");
        }

        // Last dot index
        int currentDotIndex = fileName.lastIndexOf('.');

        // Compression extension
        compressionFormat = switch (fileName.substring(currentDotIndex + 1)) {
            case "bz2" -> CompressionFormat.BZIP2;
            case "gz" -> CompressionFormat.GZIP;
            case "xz" -> CompressionFormat.XZ;
            case "zip" -> CompressionFormat.ZIP;
            case "zst" -> CompressionFormat.ZSTD;
            default -> null;
        };

        // File name without the compression extension
        String fileNameWithoutCompressionExtension = compressionFormat == null ? fileName : fileName.substring(0, currentDotIndex);

        // Last dot index
        currentDotIndex = fileNameWithoutCompressionExtension.lastIndexOf('.');

        // Archive extension
        String fileNameWithoutCompressionNorArchive;
        if (compressionFormat == CompressionFormat.ZIP) {
            archiveFormat = ArchiveFormat.ZIP;
            fileNameWithoutCompressionNorArchive = fileNameWithoutCompressionExtension;
        } else if (ArchiveFormat.TAR.getExtension().equals(fileNameWithoutCompressionExtension.substring(currentDotIndex + 1))) {
            archiveFormat = ArchiveFormat.TAR;
            fileNameWithoutCompressionNorArchive = fileNameWithoutCompressionExtension.substring(0, currentDotIndex);
        } else {
            archiveFormat = null;
            fileNameWithoutCompressionNorArchive = fileNameWithoutCompressionExtension;
        }

        // Last dot index
        currentDotIndex = fileNameWithoutCompressionNorArchive.lastIndexOf('.');

        /* Data datasource extension
         * Four cases are possible:
         *  - case 1 ("dummy"): currentDotIndex < 0 -> no data extension is given
         *  - case 2 (".dummy"): currentDotIndex == 0 -> considered as a hidden file so no data extension is given
         *  - case 3 ("dummy.foo"): "foo" is the data extension
         */
        dataExtension = currentDotIndex < 1 ? "" : fileNameWithoutCompressionNorArchive.substring(currentDotIndex + 1);
        logDataExtension(fileName, dataExtension, dataSourceInitialization);

        // Base name
        baseName = dataExtension.isEmpty() ?
            fileNameWithoutCompressionNorArchive :
            fileNameWithoutCompressionNorArchive.substring(0, currentDotIndex);
        if (baseName.isEmpty()) {
            LOGGER.warn("Base name is empty in file {}", fileName);
        }
    }

    private void logDataExtension(String fileName, String dataExtension, boolean dataSourceInitialization) {
        if (dataSourceInitialization && dataExtension.isEmpty()) {
            LOGGER.warn("Data extension is empty in file {}", fileName);
        }
    }

    public String getBaseName() {
        return baseName;
    }

    public CompressionFormat getCompressionFormat() {
        return compressionFormat;
    }

    public ArchiveFormat getArchiveFormat() {
        return archiveFormat;
    }

    public String getDataExtension() {
        return dataExtension;
    }

    public String toString() {
        return "FileInformation["
            + "baseName=" + baseName + ", "
            + "dataExtension=" + dataExtension + ", "
            + "archiveFormat=" + archiveFormat + ", "
            + "compressionFormat=" + compressionFormat
            + "]";
    }
}
