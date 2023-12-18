/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.math.matrix.serializer;

import com.powsybl.math.matrix.SparseMatrix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Abstract class for sparse matrices serializers classes. It provides an implementation for common methods.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public abstract class AbstractSparseMatrixSerializer implements SparseMatrixSerializer {

    public void save(SparseMatrix matrix, Path file) {
        Objects.requireNonNull(matrix);
        Objects.requireNonNull(file);
        try (OutputStream outputStream = Files.newOutputStream(file)) {
            save(matrix, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public SparseMatrix load(Path file) {
        Objects.requireNonNull(file);
        try (InputStream inputStream = Files.newInputStream(file)) {
            return load(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
