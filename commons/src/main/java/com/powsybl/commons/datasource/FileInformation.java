/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

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
    private String sourceFormat;

    FileInformation(String fileName) {
        Objects.requireNonNull(fileName);
        computeInformation(fileName, true);
    }

    FileInformation(String fileName, boolean dataSourceInitialization) {
        Objects.requireNonNull(fileName);
        computeInformation(fileName, dataSourceInitialization);
    }

    private void computeInformation(String fileName, boolean dataSourceInitialization) {
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
            archiveFormat = !fileNameWithoutCompressionExtension.substring(currentDotIndex + 1).isEmpty() && "tar".contains(fileNameWithoutCompressionExtension.substring(currentDotIndex + 1)) ? ArchiveFormat.TAR : null;
            fileNameWithoutCompressionNorArchive = archiveFormat == null ? fileNameWithoutCompressionExtension : fileNameWithoutCompressionExtension.substring(0, currentDotIndex);
        }

        // Last dot index
        currentDotIndex = fileNameWithoutCompressionNorArchive.lastIndexOf('.');

        // Datasource format
        sourceFormat = currentDotIndex < 0 ? "" : fileNameWithoutCompressionNorArchive.substring(currentDotIndex);
        if (dataSourceInitialization) {
            if (sourceFormat.isEmpty()) {
                LOGGER.warn("Source format is empty in file {}", fileName);
            } else if (!usualSourceFormats.contains(sourceFormat)) {
                LOGGER.warn("Source format {} is not a usual one!", sourceFormat);
            }
        }

        // Base name
        baseName = sourceFormat.isEmpty() ? fileNameWithoutCompressionNorArchive : fileNameWithoutCompressionNorArchive.substring(0, currentDotIndex);
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

    public String getSourceFormat() {
        return sourceFormat;
    }
}
