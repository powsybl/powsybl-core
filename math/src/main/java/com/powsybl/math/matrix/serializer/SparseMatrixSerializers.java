/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix.serializer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.math.matrix.SparseMatrix;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Provides a single entry-point to SparseMatrixSerializer implementations.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public final class SparseMatrixSerializers {
    private SparseMatrixSerializers() {
    }

    /**
     * Get all supported formats.
     *
     * @return the supported formats
     */
    public static Collection<String> getFormats() {
        List<String> formats = new ArrayList<>();
        for (SparseMatrixSerializer s : ServiceLoader.load(SparseMatrixSerializer.class, SparseMatrixSerializer.class.getClassLoader())) {
            formats.add(s.getFormat());
        }
        return formats;
    }

    /**
     * Get the serializer for the specified format
     *
     * @param format The serializer format
     *
     * @return The serializer for the specified format or null if this format is not supported
     */
    public static SparseMatrixSerializer getSerializer(String format) {
        Objects.requireNonNull(format);
        for (SparseMatrixSerializer s : ServiceLoader.load(SparseMatrixSerializer.class, SparseMatrixSerializer.class.getClassLoader())) {
            if (format.equals(s.getFormat())) {
                return s;
            }
        }
        return null;
    }

    private static SparseMatrixSerializer getSerializerIfExistOrError(String format) {
        SparseMatrixSerializer s = getSerializer(format);
        if (s == null) {
            throw new PowsyblException("Unsupported format: " + format + " [" + getFormats() + "]");
        }
        return s;
    }

    public static void save(SparseMatrix matrix, OutputStream outputStream, String format) {
        Objects.requireNonNull(matrix);
        Objects.requireNonNull(outputStream);

        SparseMatrixSerializer s = getSerializerIfExistOrError(format);
        s.save(matrix, outputStream);
    }

    public static void save(SparseMatrix matrix, Path file, String format) {
        Objects.requireNonNull(matrix);
        Objects.requireNonNull(file);

        SparseMatrixSerializer s = getSerializerIfExistOrError(format);
        s.save(matrix, file);
    }

    public static SparseMatrix load(InputStream inputStream, String format) {
        Objects.requireNonNull(inputStream);

        SparseMatrixSerializer s = getSerializerIfExistOrError(format);
        return s.load(inputStream);
    }

    public static SparseMatrix load(Path file, String format) {
        Objects.requireNonNull(file);

        SparseMatrixSerializer s = getSerializerIfExistOrError(format);
        return s.load(file);
    }
}
