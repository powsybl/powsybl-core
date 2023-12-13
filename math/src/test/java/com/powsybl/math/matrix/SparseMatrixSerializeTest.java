/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class SparseMatrixSerializeTest extends AbstractMatrixTest {

    private final MatrixFactory matrixFactory = new SparseMatrixFactory();

    private final MatrixFactory otherMatrixFactory = new DenseMatrixFactory();

    private static SparseMatrix generateRandomSparseMatrix(int numRows, int numCols, double density) {
        Random random = new Random();
        SparseMatrix matrix = new SparseMatrix(numRows, numCols, 0);
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                //Generate a random value and add it to the matrix with probability based on density
                if (random.nextDouble() < density) {
                    matrix.set(row, col, random.nextDouble());
                }
            }
        }
        return matrix;
    }

    @Override
    protected MatrixFactory getMatrixFactory() {
        return matrixFactory;
    }

    @Override
    protected MatrixFactory getOtherMatrixFactory() {
        return otherMatrixFactory;
    }

    private SparseMatrix createSparseMatrix() {
        Matrix matrix = getMatrixFactory().create(2, 5, 4);
        matrix.set(0, 0, 1d);
        matrix.set(1, 0, 2d);
        matrix.set(0, 2, 3d);
        matrix.set(1, 4, 4d);
        return (SparseMatrix) matrix;
    }

    public static void compareSparseMatrices(SparseMatrix m1, SparseMatrix m2) {
        assertArrayEquals(m1.getColumnValueCount(), m2.getColumnValueCount());
        assertArrayEquals(m1.getColumnStart(), m2.getColumnStart());
        assertArrayEquals(m1.getValues(), m2.getValues());
        assertArrayEquals(m1.getRowIndices(), m2.getRowIndices());
        assertEquals(m1.getColumnCount(), m2.getColumnCount());
        assertEquals(m1.getRowCount(), m2.getRowCount());
    }

    @Test
    void testSerialize() throws IOException, ClassNotFoundException {
        SparseMatrix matrix = createSparseMatrix();
        byte[] matrixObj;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(matrix);
            matrixObj = byteArrayOutputStream.toByteArray();
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(matrixObj);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            SparseMatrix matrix1 = (SparseMatrix) objectInputStream.readObject();
            compareSparseMatrices(matrix, matrix1);
        }
    }

    @Test
    void testSerializeToFile() throws IOException, ClassNotFoundException {
        SparseMatrix matrix = createSparseMatrix();
        File file = Paths.get("sparse-matrix-test.obj").toFile();
        SparseMatrixUtils.saveSparseMatrixToFile(matrix, file);
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromFile(file);
        compareSparseMatrices(matrix, m1);
        assertTrue(file.delete());
    }

    @Test
    void testSerializeSparseMatrixToMatlabFormat() throws IOException {
        SparseMatrix m = createSparseMatrix();
        File file = Paths.get("sparse-matrix-test.mat").toFile();
        SparseMatrixUtils.saveSparseMatrixToMatlabFile(m, file);
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromMatlabFile(file);
        compareSparseMatrices(m, m1);
        assertTrue(file.delete());
    }

    @Test
    void testSerializeSparseMatrixToMatlabFormat2() throws IOException {
        SparseMatrix m = createSparseMatrix();
        File file = Paths.get("sparse-matrix-test.mat").toFile();
        String matlabVariableName = "sparse_m";
        SparseMatrixUtils.saveSparseMatrixToMatlabFile(m, file, matlabVariableName);
        assertThrows(IllegalArgumentException.class, () -> SparseMatrixUtils.loadSparseMatrixFromMatlabFile(file, "DOES_NOT_EXIST"));
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromMatlabFile(file, matlabVariableName);
        compareSparseMatrices(m, m1);
        assertTrue(file.delete());
    }

    @Test
    void testSerializeLargerSparseMatrixToMatlabFormat() throws IOException {
        //create a 100x100 sparse matrix with 30% (circa) non zero values
        SparseMatrix m = generateRandomSparseMatrix(100, 100, 0.3);
        File file = Paths.get("sparse-large-matrix-test.mat").toFile();
        SparseMatrixUtils.saveSparseMatrixToMatlabFile(m, file);
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromMatlabFile(file);
        compareSparseMatrices(m, m1);
        assertTrue(file.delete());
    }
}

