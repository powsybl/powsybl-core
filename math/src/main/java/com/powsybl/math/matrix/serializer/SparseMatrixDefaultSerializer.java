/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix.serializer;

import com.google.auto.service.AutoService;
import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import com.powsybl.math.matrix.SparseMatrix;

import java.io.*;
import java.util.Objects;

/**
 * Sparse matrix serializer implementation that uses the default java serialization.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
@AutoService(SparseMatrixSerializer.class)
public class SparseMatrixDefaultSerializer extends AbstractSparseMatrixSerializer {

    private static final String FORMAT_ID = "DEFAULT";

    @Override
    public String getFormat() {
        return FORMAT_ID;
    }

    @Override
    public String getComment() {
        return "Java serializer format";
    }

    @Override
    public void save(SparseMatrix matrix, OutputStream outputStream) {
        Objects.requireNonNull(matrix);
        Objects.requireNonNull(outputStream);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(matrix);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public SparseMatrix load(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (SparseMatrix) objectInputStream.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
    }
}
