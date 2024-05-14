/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
            m.set(i, 0, c[i]);
        }
        return m;
    }

    /**
     * Utility method for creating a single row matrix from a java array.
     *
     * @param r a row array
     * @param matrixFactory matrix factory to allow creating the matrix with different implementations.
     * @return the single row matrix
     */
    static Matrix createFromRow(double[] r, MatrixFactory matrixFactory) {
        Objects.requireNonNull(r);
        Objects.requireNonNull(matrixFactory);
        Matrix m = matrixFactory.create(1, r.length, r.length);
        for (int j = 0; j < r.length; j++) {
            m.set(0, j, r[j]);
        }
        return m;
    }

    /**
     * An element of the matrix.
     * Used to later update a value.
     */
    interface Element {

        /**
         * Set element value.
         *
         * @param value to value to set
         */
        void set(double value);

        /**
         * Add value to the element.
         *
         * @param value value to add
         */
        void add(double value);
    }

    /**
     * Handler used to iterate a matrix and get values.
     */
    interface ElementHandler {

        /**
         * This method is called for each element of the matrix.
         *
         * @param i row index
         * @param j column index
         * @param value the value at position ({@code i}, {@code j})
         */
        void onElement(int i, int j, double value);

        /**
         * @deprecated Use {@link #onElement(int, int, double)} instead.
         */
        @Deprecated(since = "2.5.0")
        default void onValue(int i, int j, double value) {
            onElement(i, j, value);
        }
    }

    /**
     * Get row count.
     *
     * @return row count
     */
    int getRowCount();

    /**
     * @deprecated Use {@link #getRowCount()} instead.
     */
    @Deprecated(since = "2.5.0")
    default int getM() {
        return getRowCount();
    }

    /**
     * Get column count.
     *
     * @return column count
     */
    int getColumnCount();

    /**
     * @deprecated Use {@link #getColumnCount()} instead.
     */
    @Deprecated(since = "2.5.0")
    default int getN() {
        return getColumnCount();
    }

    /**
     * Set value at row {@code i} and column {@code j}.
     *
     * @param i row index
     * @param j column index
     * @param value the value to set at row {@code i} and column {@code j}
     */
    void set(int i, int j, double value);

    /**
     * @deprecated Use {@link #set(int, int, double)} instead.
     */
    @Deprecated(since = "2.5.0")
    default void setValue(int i, int j, double value) {
        set(i, j, value);
    }

    /**
     * Add value at row {@code i} and column {@code j}.
     *
     * @param i row index
     * @param j column index
     * @param value the value to add at row {@code i} and column {@code j}
     */
    void add(int i, int j, double value);

    /**
     * Add value at row {@code i} and column {@code j} and get an {@code #Element} to later update the element.
     *
     * @param i row index
     * @param j column index
     * @param value the value to add at row {@code i} and column {@code j}
     * @return an element at row {@code i} and column {@code j}
     */
    Element addAndGetElement(int i, int j, double value);

    /**
     * Add value at row {@code i} and column {@code j} and get an element index to later update the element.
     *
     * @param i row index
     * @param j column index
     * @param value the value to add at row {@code i} and column {@code j}
     * @return an element index corresponding to row {@code i} and column {@code j}
     */
    int addAndGetIndex(int i, int j, double value);

    /**
     * Set value at element index {@code index}.
     *
     * @param index element index
     * @param value the value to set at element index {@code index}
     */
    void setAtIndex(int index, double value);

    /**
     * Set value at element index {@code index} without doing any bound checking.
     *
     * @param index element index
     * @param value the value to set at element index {@code index}
     */
    void setQuickAtIndex(int index, double value);

    /**
     * Add value at element index {@code index}.
     *
     * @param index element index
     * @param value the value to add at element index {@code index}
     */
    void addAtIndex(int index, double value);

    /**
     * Add value at element index {@code index} without doing any bound checking.
     *
     * @param index element index
     * @param value the value to add at element index {@code index}
     */
    void addQuickAtIndex(int index, double value);

    /**
     * @deprecated Use {@link #add(int, int, double)} instead.
     */
    @Deprecated(since = "2.5.0")
    default void addValue(int i, int j, double value) {
        add(i, j, value);
    }

    /**
     * Fill matrix with zeros.
     */
    void reset();

    /**
     * Get LU decomposition utility class for this matrix.
     *
     * @return LU decomposition utility class for this matrix
     */
    LUDecomposition decomposeLU();

    /**
     * Multiply the matrix by another one and by a scalar (this*other*scalar). The resulting matrix has the same implementation as
     * this matrix.
     *
     * @param other the other matrix
     * @param scalar a scalar to multiply the result matrix
     * @return the result of the multiplication of this matrix by the other one
     */
    Matrix times(Matrix other, double scalar);

    /**
     * Multiply the matrix by another one. The resulting matrix has the same implementation as
     * this matrix.
     *
     * @param other the other matrix
     * @return the result of the multiplication of this matrix by the other one
     */
    Matrix times(Matrix other);

    /**
     * Addition the matrix with another one (alpha * this + beta * other). The resulting matrix has the same
     * implementation as this matrix.
     *
     * @param other the other matrix
     * @param alpha a scalar to multiply this matrix
     * @param beta a scalar to multiply other matrix
     * @return the result of the addition of this matrix and the other one
     */
    Matrix add(Matrix other, double alpha, double beta);

    /**
     * Addition the matrix with another one (this + other). The resulting matrix has the same implementation as
     * this matrix.
     *
     * @param other the other matrix
     * @return the result of the addition of this matrix and the other one
     */
    Matrix add(Matrix other);

    /**
     * Iterate over non zero values of the matrix. At each non zero value {@link ElementHandler#onElement(int, int, double)}
     * is called.
     *
     * @param handler the element handler
     */
    void iterateNonZeroValue(ElementHandler handler);

    /**
     * Iterate over non zero values of the {@code j} column of the matrix. At each non zero value {@link ElementHandler#onElement(int, int, double)}
     * is called.
     *
     * @param j column index
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
     * Calculate the transposed matrix.
     *
     * @return the transposed matrix
     */
    Matrix transpose();

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
