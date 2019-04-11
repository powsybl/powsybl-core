/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/**
 * Interface for all double typed matrix implementation. It has been designed to be generic enough to allow dense and
 * sparse matrix implementations. In the case of sparse matrix additional usage constraint could be added and specified
 * in the javadoc for instance in the order a matrix has to be filled.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Matrix {

    /**
     * Utility method for creating a single column matrix from a java array.
     *
     * @param c a column array
     * @param matrixFactory matrix factory to allow creating the matrix with different implementations.
     * @return the single column matrix
     */
    static Matrix createFromColumn(double[] c, MatrixFactory matrixFactory) {
        Objects.requireNonNull(c);
        Objects.requireNonNull(matrixFactory);
        Matrix m = matrixFactory.create(c.length, 1, c.length);
        for (int i = 0; i < c.length; i++) {
            m.setValue(i, 0, c[i]);
        }
        return m;
    }

    /**
     * Handler used to iterate a matrix and get values.
     */
    interface ElementHandler {

        /**
         * This method is called for each value of the matrix.
         *
         * @param i row index
         * @param j column index
         * @param value the value at position (i, j)
         */
        void onValue(int i, int j, double value);
    }

    /**
     * Get row count.
     *
     * @return row count
     */
    int getM();

    /**
     * Get column count.
     *
     * @return column count
     */
    int getN();

    /**
     * Set value at row i and column j.
     *
     * @param i row index
     * @param j column index
     * @param value the value to set
     */
    void setValue(int i, int j, double value);

    /**
     * Add value at row i and column j.
     *
     * @param i row index
     * @param j column index
     * @param value the value to add
     */
    void addValue(int i, int j, double value);

    /**
     * Get LU decomposition utility class for this matrix.
     *
     * @return LU decomposition utility class for this matrix
     */
    LUDecomposition decomposeLU();

    /**
     * Multiply the matrix by another one (this*other).
     *
     * @param other the other matrix
     * @return the result of the multiplication of this matrix by the other one
     */
    Matrix times(Matrix other);

    /**
     * Iterate over non zero values of the matrix. At each non zero value {@link ElementHandler#onValue(int, int, double)}
     * is called.
     *
     * @param handler the element handler
     */
    void iterateNonZeroValue(ElementHandler handler);

    /**
     * Iterate over non zero values of the j column of the matrix. At each non zero value {@link ElementHandler#onValue(int, int, double)}
     * is called.
     *
     * @param handler the element handler
     */
    void iterateNonZeroValueOfColumn(int j, ElementHandler handler);

    /**
     * Copy this matrix using a dense implementation. If already a dense matrix, this method is allowed to return this.
     *
     * @return a copy of the matrix with a dense implementation.
     */
    DenseMatrix toDense();

    /**
     * Copy this matrix using a sparse implementation. If already a sparse matrix, this method is allowed to return this.
     *
     * @return a copy of the matrix with a sparse implementation.
     */
    SparseMatrix toSparse();

    /**
     * Copy this matrix using another implementation. If already with the right implementation, this method is allowed to
     * return this.
     *
     * @param factory a matrix factory to create the copy.
     *
     * @return a copy of the matrix
     */
    Matrix to(MatrixFactory factory);

    /**
     * Copy this matrix using another implementation. This method is not allowed to return this.
     *
     * @param factory a matrix factory to create the copy.
     *
     * @return a copy of the matrix
     */
    Matrix copy(MatrixFactory factory);

    /**
     * Print the matrix to a stream. Row and column names are also printed to facilitate debugging.
     *
     * @param out the stream
     * @param rowNames row names
     * @param columnNames column names
     */
    void print(PrintStream out, List<String> rowNames, List<String> columnNames);

    /**
     * Print the matrix to a stream.
     *
     * @param out the stream
     */
    void print(PrintStream out);
}
