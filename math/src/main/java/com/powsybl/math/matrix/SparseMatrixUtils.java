/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import org.ejml.data.DMatrixSparseCSC;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;

import java.io.*;
import java.util.Objects;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public final class SparseMatrixUtils {

    private static final String ENTRY_NAME = "ejmlMatrix";

    private SparseMatrixUtils() {
    }

    public static void saveSparseMatrixToFile(SparseMatrix m, File file) throws IOException {
        Objects.requireNonNull(m);
        Objects.requireNonNull(file);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(m);
        }
    }

    public static SparseMatrix loadSparseMatrixFromFile(File file) throws IOException {
        Objects.requireNonNull(file);
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return (SparseMatrix) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
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

    public static void saveSparseMatrixToMatlabFile(SparseMatrix m, File file, String entryName) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(entryName);
        saveMatlab(toEjmlSparseMatrix(m), file, entryName);
    }

    public static void saveSparseMatrixToMatlabFile(SparseMatrix m, File file) throws IOException {
        saveSparseMatrixToMatlabFile(m, file, ENTRY_NAME);
    }

    public static SparseMatrix loadSparseMatrixFromMatlabFile(File file, String entryName) throws IOException {
        Objects.requireNonNull(file);
        DMatrixSparseCSC matrix = loadMatlab(file, null, entryName);
        return fromEjmlSparseMatrix(matrix);
    }

    public static SparseMatrix loadSparseMatrixFromMatlabFile(File file) throws IOException {
        return loadSparseMatrixFromMatlabFile(file, ENTRY_NAME);
    }

    // the code below was extracted from Efficient Java Matrix Library (EJML)'s class
    // <a href="https://github.com/lessthanoptimal/ejml/blob/53c2a8fb9fce3331870667dd9fabc752b8b57aa0/main/ejml-core/src/org/ejml/ops/MatrixIO.java">org.ejml.ops.MatrixIO</a>
    // (original code Licensed under the Apache License, Version 2.0)
    // changed to allow specifying a different name for the matlab target variable name
    // and using a File, instead of a String path for the matlab file.

    private static void saveMatlab(org.ejml.data.Matrix m, File file, String entryName) throws IOException {
        MatFile mat = Mat5.newMatFile().addArray(entryName, Mat5Ejml.asArray(m));
        Mat5.writeToFile(mat, file);
    }

    private static <T extends org.ejml.data.Matrix> T loadMatlab(File file, T output, String entryName) throws IOException {
        MatFile mat = Mat5.readFromFile(file);
        for (MatFile.Entry entry : mat.getEntries()) {
            if (entryName.matches(entry.getName())) {
                return Mat5Ejml.convert(entry.getValue(), output);
            }
        }
        throw new IllegalArgumentException("File does not have expected entry: '" + entryName + "'");
    }
}
