/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix.serializer;

import com.powsybl.math.matrix.SparseMatrix;
import com.powsybl.math.matrix.SparseMatrixFactory;
import org.ejml.data.DMatrixSparseCSC;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Sinks;
import us.hebi.matlab.mat.types.Sources;
import us.hebi.matlab.mat.util.Casts;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

/**
 * Utility class providing SparseMatrices save and load features, to and from MATLAB binary format.
 * This class uses <a href="https://github.com/HebiRobotics/MFL">MFL</a> and
 * <a href="https://github.com/lessthanoptimal/ejml">EJML</a> libraries.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public final class SparseMatrixMatUtils {

    private static final String ENTRY_NAME = "ejmlMatrix";

    private SparseMatrixMatUtils() {
    }

    public static void save(SparseMatrix m, OutputStream outputStream) {
        Objects.requireNonNull(m);
        Objects.requireNonNull(outputStream);
        writeMatlab(toEjmlSparseMatrix(m), outputStream, ENTRY_NAME);
    }

    public static SparseMatrix load(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        DMatrixSparseCSC matrix = readMatlab(inputStream, null, ENTRY_NAME);
        return fromEjmlSparseMatrix(matrix);
    }

    private static DMatrixSparseCSC toEjmlSparseMatrix(SparseMatrix m) {
        Objects.requireNonNull(m);
        DMatrixSparseCSC ejmlM = new DMatrixSparseCSC(m.getRowCount(), m.getColumnCount());
        m.iterateNonZeroValue(ejmlM::set);
        return ejmlM;
    }

    private static SparseMatrix fromEjmlSparseMatrix(DMatrixSparseCSC matrix) {
        Objects.requireNonNull(matrix);
        int numRows = matrix.getNumRows();
        int numCols = matrix.getNumCols();
        SparseMatrix m = new SparseMatrixFactory().create(numRows, numCols, matrix.getNonZeroLength());
        for (int col = 0; col < numCols; col++) {
            int idx0 = matrix.col_idx[col];
            int idx1 = matrix.col_idx[col + 1];

            for (int i = idx0; i < idx1; i++) {
                int row = matrix.nz_rows[i];
                double value = matrix.nz_values[i];
                m.set(row, col, value);
            }
        }
        return m;
    }

    private static ByteBuffer getBuffer(MatFile matFile) throws IOException {
        int bufferSize = Casts.sint32(matFile.getUncompressedSerializedSize());
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.nativeOrder());
        matFile.writeTo(Sinks.wrap(buffer));
        buffer.flip();
        return buffer;
    }

    private static void writeMatlab(org.ejml.data.Matrix m, OutputStream outputStream, String entryName) {
        try (WritableByteChannel channel = Channels.newChannel(outputStream)) {
            try (MatFile mat = Mat5.newMatFile().addArray(entryName, Mat5Ejml.asArray(m))) {
                channel.write(getBuffer(mat));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T extends org.ejml.data.Matrix> T readMatlab(InputStream inputStream, T output, String entryName) {
        try (MatFile mat = Mat5.newReader(Sources.wrapInputStream(inputStream)).setEntryFilter(entry -> entry.getName().equals(entryName)).readMat()) {
            if (mat.getNumEntries() == 0) {
                throw new IllegalArgumentException("expected matrix named '" + entryName + "' not found.");
            }
            return Mat5Ejml.convert(mat.getArray(entryName), output);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
