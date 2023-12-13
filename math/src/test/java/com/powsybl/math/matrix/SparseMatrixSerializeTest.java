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
class SparseMatrixSerializeTest {

    private final MatrixFactory matrixFactory = new SparseMatrixFactory();

    private SparseMatrix generateRandomSparseMatrix(int numRows, int numCols, double density) {
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

    private SparseMatrix createSparseMatrix() {
        SparseMatrix matrix = (SparseMatrix) matrixFactory.create(2, 5, 4);
        matrix.set(0, 0, 1d);
        matrix.set(1, 0, 2d);
        matrix.set(0, 2, 3d);
        matrix.set(1, 4, 4d);
        return matrix;
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
            assertEquals(matrix, matrix1);
        }
    }

    @Test
    void testSerializeToFile() throws IOException {
        SparseMatrix matrix = createSparseMatrix();
        File file = Paths.get("sparse-matrix-test.obj").toFile();
        SparseMatrixUtils.saveSparseMatrixToFile(matrix, file);
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromFile(file);
        assertEquals(matrix, m1);
        assertTrue(file.delete());
    }

    @Test
    void testSerializeSparseMatrixToMatlabFormat() throws IOException {
        SparseMatrix m = createSparseMatrix();
        File file = Paths.get("sparse-matrix-test.mat").toFile();
        SparseMatrixUtils.saveSparseMatrixToMatlabFile(m, file);
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromMatlabFile(file);
        assertEquals(m, m1);
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
        assertEquals(m, m1);
        assertTrue(file.delete());
    }

    @Test
    void testSerializeLargerSparseMatrixToMatlabFormat() throws IOException {
        //create a 100x100 sparse matrix with 30% (circa) non zero values
        SparseMatrix m = generateRandomSparseMatrix(100, 100, 0.3);
        File file = Paths.get("sparse-large-matrix-test.mat").toFile();
        SparseMatrixUtils.saveSparseMatrixToMatlabFile(m, file);
        SparseMatrix m1 = SparseMatrixUtils.loadSparseMatrixFromMatlabFile(file);
        assertEquals(m, m1);
        assertTrue(file.delete());
    }
}
