/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix.serializer;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.math.matrix.MatrixFactory;
import com.powsybl.math.matrix.SparseMatrix;
import com.powsybl.math.matrix.SparseMatrixFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class SparseMatrixSerializeTest {

    private final MatrixFactory matrixFactory = new SparseMatrixFactory();

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    private SparseMatrix createRandomSparseMatrix(int numRows, int numCols, double density) {
        Random random = new Random();
        SparseMatrix matrix = (SparseMatrix) matrixFactory.create(numRows, numCols, 0);
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                //Add a random value to the matrix with probability based on density
                if (random.nextDouble() < density) {
                    matrix.set(row, col, random.nextDouble());
                }
            }
        }
        return matrix;
    }

    private SparseMatrix createSimpleSparseMatrix() {
        SparseMatrix matrix = (SparseMatrix) matrixFactory.create(2, 5, 4);
        matrix.set(0, 0, 1d);
        matrix.set(1, 0, 2d);
        matrix.set(0, 2, 3d);
        matrix.set(1, 4, 4d);
        return matrix;
    }

    @Test
    void testGetFormats() {
        assertEquals("[DEFAULT, MAT]", SparseMatrixSerializers.getFormats().toString());
    }

    @Test
    void testGetExporter() {
        assertEquals("DEFAULT", SparseMatrixSerializers.getSerializer("DEFAULT").getFormat());
        assertEquals("MAT", SparseMatrixSerializers.getSerializer("MAT").getFormat());
        assertNull(SparseMatrixSerializers.getSerializer("DOES_NOT_EXIST"));
    }

    @Test
    void testSerializeToFileUnrecognizedFormat() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("/work/sparse-matrix-test.obj");
        assertThrows(PowsyblException.class, () -> SparseMatrixSerializers.save(matrix, file, "DOES_NOT_EXIST"));
    }

    @Test
    void testSerializeToFile() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("/work/sparse-matrix-test.obj");
        SparseMatrixSerializers.save(matrix, file, "DEFAULT");
        SparseMatrix m1 = SparseMatrixSerializers.load(file, "DEFAULT");
        assertEquals(matrix, m1);
    }

    @Test
    void testSerializeSparseMatrixToMatlabFormat() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        SparseMatrixSerializer serializer = SparseMatrixSerializers.getSerializer("MAT");
        Path file = fileSystem.getPath("/work/sparse-matrix-test.mat");
        serializer.save(matrix, file);
        SparseMatrix m1 = serializer.load(file);
        assertEquals(matrix, m1);
    }

    @Test
    void testSerializeSparseMatrixToMatlabFormat2() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("/work/sparse-matrix-test.mat");
        SparseMatrixSerializers.save(matrix, file, "MAT");
        SparseMatrix m1 = SparseMatrixSerializers.load(file, "MAT");
        assertEquals(matrix, m1);
    }

    @Test
    void testSerializeSparseMatrixMismatchingFormats() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("/work/sparse-matrix-test.mat");
        SparseMatrixSerializers.save(matrix, file, "DEFAULT");
        assertThrows(UncheckedIOException.class, () -> SparseMatrixSerializers.load(file, "MAT"));
    }

    @Test
    void testSerializeLargerSparseMatrixToMatlabFormat() {
        //create a 100x100 sparse matrix with 30% (circa) non zero values
        SparseMatrix matrix = createRandomSparseMatrix(100, 100, 0.3);
        Path file = fileSystem.getPath("/work/sparse-large-matrix-test.mat");
        SparseMatrixSerializers.save(matrix, file, "MAT");
        SparseMatrix m1 = SparseMatrixSerializers.load(file, "MAT");
        assertEquals(matrix, m1);
    }
}
