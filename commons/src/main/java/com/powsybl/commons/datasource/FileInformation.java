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

import java.util.List;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class FileInformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileInformation.class);
    private final List<String> usualSourceFormats = List.of(
        ".iidm", ".xiidm", ".jiidm", ".biidm", ".xml", ".txt", ".uct", ".raw", ".dgs", ".json"
    );
    private String baseName;
    private CompressionFormat compressionFormat;
    private ArchiveFormat archiveFormat;
    private String sourceFormatExtension;

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
        } else {
            archiveFormat = "tar".equals(fileNameWithoutCompressionExtension.substring(currentDotIndex + 1)) ? ArchiveFormat.TAR : null;
            fileNameWithoutCompressionNorArchive = archiveFormat == null ? fileNameWithoutCompressionExtension : fileNameWithoutCompressionExtension.substring(0, currentDotIndex);
        }

        // Last dot index
        currentDotIndex = fileNameWithoutCompressionNorArchive.lastIndexOf('.');

        /* Datasource format
         * Four cases are possible:
         *  - case 1 ("dummy"): currentDotIndex < 0 -> no source format is given
         *  - case 2 (".dummy"): currentDotIndex == 0 -> considered as a hidden file so no source format is given
         *  - case 3 ("dummy.foo"): ".foo" is the source format
         *  - case 4 ("dummy.iidm.xml"): ".iidm.xml" is the source format
         */
        sourceFormatExtension = findSourceFormatExtension(fileNameWithoutCompressionNorArchive, currentDotIndex);
        logSourceFormat(fileName, sourceFormatExtension, dataSourceInitialization);

        // Base name
        baseName = sourceFormatExtension.isEmpty() ?
            fileNameWithoutCompressionNorArchive :
            fileNameWithoutCompressionNorArchive.substring(0, fileNameWithoutCompressionNorArchive.lastIndexOf(sourceFormatExtension));
        if (baseName.isEmpty()) {
            LOGGER.warn("Base name is empty in file {}", fileName);
        }
    }

    /**
     * Get the source format extension from the file name. If the last extension is ".xml", it checks if there is a
     * previous ".iidm" extension
     */
    private String findSourceFormatExtension(String fileNameWithoutCompressionNorArchive, int currentDotIndex) {
        String extension = currentDotIndex < 1 ? "" : fileNameWithoutCompressionNorArchive.substring(currentDotIndex);
        if (".xml".equals(extension)
            && fileNameWithoutCompressionNorArchive.length() > 9
            && ".iidm".equals(fileNameWithoutCompressionNorArchive.substring(
                fileNameWithoutCompressionNorArchive.length() - 9,
                fileNameWithoutCompressionNorArchive.length() - 4))) {
            extension = ".iidm.xml";
        }
        return extension;
    }

    private void logSourceFormat(String fileName, String sourceFormat, boolean dataSourceInitialization) {
        if (dataSourceInitialization) {
            if (sourceFormat.isEmpty()) {
                LOGGER.warn("Source format is empty in file {}", fileName);
            } else if (!usualSourceFormats.contains(sourceFormat)) {
                LOGGER.warn("Source format {} is not a usual one!", sourceFormat);
            }
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

    public String getSourceFormatExtension() {
        return sourceFormatExtension;
    }

    public String toString() {
        return "FileInformation["
            + "baseName=" + baseName + ", "
            + "sourceFormatExtension=" + sourceFormatExtension + ", "
            + "archiveFormat=" + archiveFormat + ", "
            + "compressionFormat=" + compressionFormat
            + "]";
    }
}
