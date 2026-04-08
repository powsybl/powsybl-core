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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Sparse matrix serializer/deserializer using the MATLAB binary format.
 * This class uses <a href="https://github.com/HebiRobotics/MFL">MFL</a> and
 * <a href="https://github.com/lessthanoptimal/ejml">EJML</a> libraries.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public final class SparseMatrixMatSerializer {

    private static final String ENTRY_NAME = "ejmlMatrix";

    private SparseMatrixMatSerializer() {
    }

    /**
     * Write a SparseMatrix to a stream, in MATLAB format
     *
     * @param m The sparse matrix
     * @param oStream The output stream used by the serializer
     */
    public static void exportMat(SparseMatrix m, OutputStream oStream) {
        Objects.requireNonNull(m);
        Objects.requireNonNull(oStream);
        writeMatlab(toEjmlSparseMatrix(m), oStream, ENTRY_NAME);
    }

    /**
     * Read a SparseMatrix from a stream (MATLAB format).
     *
     * @param iStream The input stream used by the serializer
     * @return The sparse matrix read from the input stream
     */
    public static SparseMatrix importMat(InputStream iStream) {
        Objects.requireNonNull(iStream);
        DMatrixSparseCSC m = readMatlab(iStream, null, ENTRY_NAME);
        return fromEjmlSparseMatrix(m);
    }

    /**
     * Write a SparseMatrix to a MATLAB file.
     *
     * @param m The sparse matrix
     * @param file The output file used by the serializer
     */
    public static void exportMat(SparseMatrix m, Path file) {
        Objects.requireNonNull(m);
        Objects.requireNonNull(file);
        try (OutputStream oStream = Files.newOutputStream(file)) {
            exportMat(m, oStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read a SparseMatrix from a MATLAB file.
     *
     * @param file The input file used by the serializer
     * @return The sparse matrix read from the file
     */
    public static SparseMatrix importMat(Path file) {
        Objects.requireNonNull(file);
        try (InputStream iStream = Files.newInputStream(file)) {
            return importMat(iStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DMatrixSparseCSC toEjmlSparseMatrix(SparseMatrix matrix) {
        DMatrixSparseCSC ejmlMatrix = new DMatrixSparseCSC(matrix.getRowCount(), matrix.getColumnCount());
        matrix.iterateNonZeroValue(ejmlMatrix::set);
        return ejmlMatrix;
    }

    private static SparseMatrix fromEjmlSparseMatrix(DMatrixSparseCSC ejmlMatrix) {
        int numRows = ejmlMatrix.getNumRows();
        int numCols = ejmlMatrix.getNumCols();
        SparseMatrix matrix = new SparseMatrixFactory().create(numRows, numCols, ejmlMatrix.getNonZeroLength());
        for (int col = 0; col < numCols; col++) {
            int idx0 = ejmlMatrix.col_idx[col];
            int idx1 = ejmlMatrix.col_idx[col + 1];

            for (int i = idx0; i < idx1; i++) {
                int row = ejmlMatrix.nz_rows[i];
                double value = ejmlMatrix.nz_values[i];
                matrix.set(row, col, value);
            }
        }
        return matrix;
    }

    private static ByteBuffer getBuffer(MatFile matFile) throws IOException {
        int bufferSize = Casts.sint32(matFile.getUncompressedSerializedSize());
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.nativeOrder());
        matFile.writeTo(Sinks.wrap(buffer));
        buffer.flip();
        return buffer;
    }

    private static void writeMatlab(org.ejml.data.Matrix ejmlMatrix, OutputStream outputStream, String entryName) {
        try (WritableByteChannel channel = Channels.newChannel(outputStream)) {
            try (MatFile mat = Mat5.newMatFile().addArray(entryName, Mat5Ejml.asArray(ejmlMatrix))) {
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
