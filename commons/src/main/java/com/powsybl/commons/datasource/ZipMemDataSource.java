/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Giovanni Ferrari <giovanni.ferrari@techrain.it>
 */
public class ZipMemDataSource extends ReadOnlyMemDataSource {

    ZipMemDataSource(String fileName, InputStream content) {
        super(DataSourceUtil.getBaseName(fileName));

        Objects.requireNonNull(content);
        try (ZipInputStream zipStream = new ZipInputStream(content)) {
            ZipEntry entry = zipStream.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();
                try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
                    ByteStreams.copy(zipStream, bao);
                    putData(entryName, bao.toByteArray());
                }
                entry = zipStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return super.newInputStream(DataSourceUtil.getFileName(getBaseName(), suffix, ext));
    }
}
