/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.trove.TDoubleArrayListHack;
import com.powsybl.commons.util.trove.TIntArrayListHack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Sparse matrix implementation in <a href="https://en.wikipedia.org/wiki/Sparse_matrix#Compressed_sparse_column_(CSC_or_CCS)">CSC</a></a> format.
 * This implementation rely on a native library which is a wrapper around KLU module of
 * <a href="http://faculty.cse.tamu.edu/davis/suitesparse.html">SuiteSparse</a> project.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SparseMatrix extends AbstractMatrix {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparseMatrix.class);

    /**
     * Flag that indicates if native library has been loaded.
     */
    static final boolean NATIVE_INIT;

    private static native void nativeInit();

    static {
        boolean pb = false;
        try {
            System.loadLibrary("mathjni");
            nativeInit();
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Cannot load native math library");
            pb = true;
        }
        NATIVE_INIT = !pb;
    }

    private static void checkNativeInit() {
        if (!NATIVE_INIT) {
            throw new PowsyblException("Native init has failed");
        }
    }

    /**
     * Row count.
     */
    private final int rowCount;

    /**
     * Column count.
     */
    private final int columnCount;

    /**
     * Column start index in {@link #values} array.
     * Length of this vector is the number of column.
     */
    private final int[] columnStart; // plus value count in the last element

    /**
     * Row index for each of the {@link #values}.
     * Length of this vector is the number of values.
     */
    private final TIntArrayListHack rowIndices;

    /**
     * Non zero values.
     */
    private final TDoubleArrayListHack values;

    private int currentColumn = -1; // just for matrix filling

    SparseMatrix(int rowCount, int columnCount, int[] columnStart, int[] rowIndices, double[] values) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.columnStart = Objects.requireNonNull(columnStart);
        this.rowIndices = new TIntArrayListHack(Objects.requireNonNull(rowIndices));
        this.values = new TDoubleArrayListHack(Objects.requireNonNull(values));
    }

    SparseMatrix(int rowCount, int columnCount, int estimatedNonZeroValueCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        columnStart = new int[columnCount + 1];
        Arrays.fill(columnStart, -1);
        this.columnStart[columnCount] = 0;
        rowIndices = new TIntArrayListHack(estimatedNonZeroValueCount);
        values = new TDoubleArrayListHack(estimatedNonZeroValueCount);
    }

    /**
     * Get columm start index vector.
     *
     * @return columm start index vector
     */
    int[] getColumnStart() {
        return columnStart;
    }

    /**
     * Get row index vector.
     *
     * @return row index vector
     */
    int[] getRowIndices() {
        return rowIndices.getData();
    }

    /**
     * Get non zero value vector.
     *
     * @return non zero value vector
     */
    double[] getValues() {
        return values.getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return rowCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * As sparse matrix is stored in CSC format. Columns must be filled in ascending order but values inside a column
     * may be filled in any order.
     * </p>
     * @throws PowsyblException if values are filled in wrong order.
     */
    @Override
    public void set(int i, int j, double value) {
        if (j == currentColumn) {
            // ok, continue to fill row
        } else if (j > currentColumn) {
            // start new column
            columnStart[j] = values.size();
            currentColumn = j;
        } else {
            throw new PowsyblException("Columns have to be filled in the right order");
        }
        values.add(value);
        rowIndices.add(i);
        columnStart[columnStart.length - 1] = values.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * As sparse matrix is stored in CSC format. Columns must be filled in ascending order but values inside a column
     * may be filled in any order.
     * </p>
     * @throws PowsyblException if values are filled in wrong order.
     */
    @Override
    public void add(int i, int j, double value) {
        boolean startNewColumn = false;
        if (j == currentColumn) {
            // ok, continue to fill row
        } else if (j > currentColumn) {
            // start new column
            columnStart[j] = values.size();
            currentColumn = j;
            startNewColumn = true;
        } else {
            throw new PowsyblException("Columns have to be filled in the right order");
        }
        if (!startNewColumn && i == rowIndices.get(rowIndices.size() - 1)) {
            int vi = values.size() - 1;
            values.set(vi, values.get(vi) + value);
        } else {
            values.add(value);
            rowIndices.add(i);
            columnStart[columnStart.length - 1] = values.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LUDecomposition decomposeLU() {
        checkNativeInit();
        return new SparseLUDecomposition(this);
    }

    private native SparseMatrix times(int m1, int n1, int[] ap1, int[] ai1, double[] ax1, int m2, int n2, int[] ap2, int[] ai2, double[] ax2);

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix times(Matrix other) {
        checkNativeInit();
        if (!(other instanceof SparseMatrix)) {
            throw new PowsyblException("Sparse and dense matrix multiplication is not supported");
        }
        SparseMatrix o = (SparseMatrix) other;
        return times(rowCount, columnCount, columnStart, rowIndices.getData(), values.getData(),
                     o.rowCount, o.columnCount, o.columnStart, o.rowIndices.getData(), o.values.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        for (int j = 0; j < columnCount; j++) {
            iterateNonZeroValueOfColumn(j, handler);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iterateNonZeroValueOfColumn(int j, ElementHandler handler) {
        int first = columnStart[j];
        if (first != -1) {
            int last = j < columnStart.length - 1 ? columnStart[j + 1] : values.size();
            for (int v = first; v < last; v++) {
                int i = rowIndices.getQuick(v);
                double value = values.getQuick(v);
                handler.onElement(i, j, value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DenseMatrix toDense() {
        return (DenseMatrix) to(new DenseMatrixFactory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparseMatrix toSparse() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix to(MatrixFactory factory) {
        Objects.requireNonNull(factory);
        if (factory instanceof SparseMatrixFactory) {
            return this;
        }
        return copy(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getEstimatedNonZeroValueCount() {
        return values.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void print(PrintStream out) {
        print(out, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void print(PrintStream out, List<String> rowNames, List<String> columnNames) {
        out.println("rowCount=" + rowCount);
        out.println("columnCount=" + columnCount);
        out.println("columnStart=" + Arrays.toString(columnStart));
        out.println("rowIndices=" + rowIndices);
        out.println("values=" + values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return rowCount + columnCount + Arrays.hashCode(columnStart) + rowIndices.hashCode() + values.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SparseMatrix) {
            SparseMatrix other = (SparseMatrix) obj;
            return rowCount == other.rowCount &&
                    columnCount == other.columnCount &&
                    Arrays.equals(columnStart, other.columnStart) &&
                    rowIndices.equals(other.rowIndices) &&
                    values.equals(other.values);
        }
        return false;
    }
}
