/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import com.powsybl.commons.util.trove.TDoubleArrayListHack;
import com.powsybl.commons.util.trove.TIntArrayListHack;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Sparse matrix implementation in <a href="https://en.wikipedia.org/wiki/Sparse_matrix#Compressed_sparse_column_(CSC_or_CCS)">CSC</a></a> format.
 * This implementation rely on a native library which is a wrapper around KLU module of
 * <a href="http://faculty.cse.tamu.edu/davis/suitesparse.html">SuiteSparse</a> project.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SparseMatrix extends AbstractMatrix implements Serializable {
    private static final long serialVersionUID = -7810324161942335828L;

    // Classes allowed during deserialization
    private static final Set<Class<?>> ALLOWED_CLASSES = Set.of(SparseMatrix.class,
            int[].class, double[].class,
            TIntArrayListHack.class, TDoubleArrayListHack.class,
            TIntArrayList.class, TDoubleArrayList.class);

    /**
     * Sparse Element implementation.
     * An element in a sparse matrix is defined by its index in the values vector.
     */
    class SparseElement implements Element {

        /**
         * Index of the element in the values vector.
         */
        private final int valueIndex;

        SparseElement(int valueIndex) {
            this.valueIndex = valueIndex;
        }

        @Override
        public void set(double value) {
            values.setQuick(valueIndex, value);
        }

        @Override
        public void add(double value) {
            values.setQuick(valueIndex, values.getQuick(valueIndex) + value);
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
     * Length of this vector is the number of column, plus one last element at the end for value count.
     */
    private final int[] columnStart;

    /**
     * Column value count.
     * Length of this vector is the number of column.
     */
    private final int[] columnValueCount;

    /**
     * Row index for each of the {@link #values}.
     * Length of this vector is the number of values.
     */
    private final TIntArrayListHack rowIndices;

    /**
     * Non zero values.
     */
    private final TDoubleArrayListHack values;

    private double rgrowthThreshold = SparseLUDecomposition.DEFAULT_RGROWTH_THRESHOLD;

    private int currentColumn = -1; // just for matrix filling

    /**
     * Create a sparse matrix from its internal structure vectors.
     * This constructor is only called on C++ side.
     *
     * @param rowCount row count
     * @param columnCount column count
     * @param columnStart column start vector
     * @param rowIndices row indices vector
     * @param values value vector
     */
    public SparseMatrix(int rowCount, int columnCount, int[] columnStart, int[] rowIndices, double[] values) {
        checkSize(rowCount, columnCount);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.columnStart = Objects.requireNonNull(columnStart);
        if (columnStart.length != columnCount + 1) {
            throw new MatrixException("columnStart array length has to be columnCount + 1");
        }
        columnValueCount = new int[columnCount];
        this.rowIndices = new TIntArrayListHack(Objects.requireNonNull(rowIndices));
        this.values = new TDoubleArrayListHack(Objects.requireNonNull(values));
        if (rowIndices.length != values.length) {
            throw new MatrixException("rowIndices and values arrays must have the same length");
        }
        fillColumnValueCount(this.columnCount, this.columnStart, columnValueCount, this.values);
        currentColumn = columnCount - 1;
    }

    private static void fillColumnValueCount(int columnCount, int[] columnStart, int[] columnValueCount, TDoubleArrayListHack values) {
        int lastNonEmptyColumn = -1;
        for (int column = 0; column < columnCount; column++) {
            if (columnStart[column] != -1) {
                if (lastNonEmptyColumn != -1) {
                    columnValueCount[lastNonEmptyColumn] = columnStart[column] - columnStart[lastNonEmptyColumn];
                }
                lastNonEmptyColumn = column;
            }
        }
        if (lastNonEmptyColumn != -1) {
            columnValueCount[lastNonEmptyColumn] = values.size() - columnStart[lastNonEmptyColumn];
        }
    }

    /**
     * Create an empty sparse matrix.
     *
     * @param rowCount row count
     * @param columnCount column count
     * @param estimatedValueCount estimated number of values (used for internal pre-allocation)
     */
    SparseMatrix(int rowCount, int columnCount, int estimatedValueCount) {
        checkSize(rowCount, columnCount);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        columnStart = new int[columnCount + 1];
        columnValueCount = new int[columnCount];
        Arrays.fill(columnStart, -1);
        this.columnStart[columnCount] = 0;
        rowIndices = new TIntArrayListHack(estimatedValueCount);
        values = new TDoubleArrayListHack(estimatedValueCount);
    }

    private static void checkSize(int rowCount, int columnCount) {
        if (rowCount < 1) {
            throw new MatrixException("row count has to be strictly positive");
        }
        if (columnCount < 1) {
            throw new MatrixException("column count has to be strictly positive");
        }
    }

    public double getRgrowthThreshold() {
        return rgrowthThreshold;
    }

    public void setRgrowthThreshold(double rgrowthThreshold) {
        this.rgrowthThreshold = rgrowthThreshold;
    }

    /**
     * Get columm start index vector.
     *
     * @return columm start index vector
     */
    public int[] getColumnStart() {
        return columnStart;
    }

    /**
     * Get column value count vector.
     *
     * @return column value count vector.
     */
    int[] getColumnValueCount() {
        return columnValueCount;
    }

    /**
     * Get row index vector.
     *
     * @return row index vector
     */
    public int[] getRowIndices() {
        return rowIndices.getData();
    }

    /**
     * Get non zero value vector.
     *
     * @return non zero value vector
     */
    public double[] getValues() {
        return values.getData();
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

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
     * @throws MatrixException if values are filled in wrong order.
     */
    @Override
    public void set(int i, int j, double value) {
        checkBounds(i, j);
        if (j == currentColumn) {
            // ok, continue to fill row
        } else if (j > currentColumn) {
            // start new column
            for (int k = currentColumn + 1; k <= j; k++) {
                columnStart[k] = values.size();
            }
            currentColumn = j;
        } else {
            throw new MatrixException("Columns have to be filled in the right order");
        }
        values.add(value);
        rowIndices.add(i);
        columnStart[columnStart.length - 1] = values.size();
        columnValueCount[j]++;
    }

    private void fillLastEmptyColumns() {
        for (int k = currentColumn + 1; k < columnCount; k++) {
            columnStart[k] = values.size();
        }
        currentColumn = columnCount - 1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * As sparse matrix is stored in CSC format. Columns must be filled in ascending order but values inside a column
     * may be filled in any order.
     * </p>
     * @throws MatrixException if values are filled in wrong order.
     */
    @Override
    public void add(int i, int j, double value) {
        checkBounds(i, j);
        boolean startNewColumn = false;
        if (j == currentColumn) {
            // ok, continue to fill row
        } else if (j > currentColumn) {
            // start new column
            columnStart[j] = values.size();
            currentColumn = j;
            startNewColumn = true;
        } else {
            throw new MatrixException("Columns have to be filled in the right order");
        }
        if (!startNewColumn && i == rowIndices.get(rowIndices.size() - 1)) {
            int vi = values.size() - 1;
            values.setQuick(vi, values.getQuick(vi) + value);
        } else {
            values.add(value);
            rowIndices.add(i);
            columnStart[columnStart.length - 1] = values.size();
            columnValueCount[j]++;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * As sparse matrix is stored in CSC format. Columns must be filled in ascending order but values inside a column
     * may be filled in any order.
     * </p>
     * @throws MatrixException if values are filled in wrong order.
     */
    @Override
    public Element addAndGetElement(int i, int j, double value) {
        add(i, j, value);
        return new SparseElement(values.size() - 1);
    }

    @Override
    public int addAndGetIndex(int i, int j, double value) {
        add(i, j, value);
        return values.size() - 1;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= values.size()) {
            throw new MatrixException("Element index out of bound [0, " + (values.size() - 1) + "]");
        }
    }

    @Override
    public void setAtIndex(int index, double value) {
        checkElementIndex(index);
        setQuickAtIndex(index, value);
    }

    @Override
    public void setQuickAtIndex(int index, double value) {
        values.set(index, value);
    }

    @Override
    public void addAtIndex(int index, double value) {
        checkElementIndex(index);
        addQuickAtIndex(index, value);
    }

    @Override
    public void addQuickAtIndex(int index, double value) {
        values.setQuick(index, values.getQuick(index) + value);
    }

    @Override
    public void reset() {
        values.fill(0d);
    }

    @Override
    public LUDecomposition decomposeLU() {
        fillLastEmptyColumns();
        return new SparseLUDecomposition(this);
    }

    private native SparseMatrix times(int m1, int n1, int[] ap1, int[] ai1, double[] ax1, int m2, int n2, int[] ap2, int[] ai2, double[] ax2);

    private native SparseMatrix add(int m1, int n1, int[] ap1, int[] ai1, double[] ax1, int m2, int n2, int[] ap2, int[] ai2, double[] ax2, double alpha, double beta);

    public SparseMatrix times(SparseMatrix other) {
        Objects.requireNonNull(other);
        fillLastEmptyColumns();
        other.fillLastEmptyColumns();
        SparseMatrix result = times(rowCount, columnCount, columnStart, rowIndices.getData(), values.getData(),
                other.rowCount, other.columnCount, other.columnStart, other.rowIndices.getData(), other.values.getData());
        result.setRgrowthThreshold(rgrowthThreshold);
        return result;
    }

    public SparseMatrix times(SparseMatrix other, double scalar) {
        SparseMatrix result = times(other);
        result.values.transformValues(v -> v * scalar);
        return result;
    }

    @Override
    public Matrix times(Matrix other, double scalar) {
        SparseMatrix o = Objects.requireNonNull(other).toSparse();
        return times(o, scalar);
    }

    public SparseMatrix add(SparseMatrix other, double alpha, double beta) {
        Objects.requireNonNull(other);
        fillLastEmptyColumns();
        other.fillLastEmptyColumns();
        SparseMatrix result = add(rowCount, columnCount, columnStart, rowIndices.getData(), values.getData(),
                other.rowCount, other.columnCount, other.columnStart, other.rowIndices.getData(), other.values.getData(), alpha, beta);
        result.setRgrowthThreshold(rgrowthThreshold);
        return result;
    }

    @Override
    public Matrix add(Matrix other, double alpha, double beta) {
        SparseMatrix o = Objects.requireNonNull(other).toSparse();
        return add(o, alpha, beta);
    }

    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        for (int j = 0; j < columnCount; j++) {
            iterateNonZeroValueOfColumn(j, handler);
        }
    }

    @Override
    public void iterateNonZeroValueOfColumn(int j, ElementHandler handler) {
        int first = columnStart[j];
        if (first != -1) {
            for (int v = first; v < first + columnValueCount[j]; v++) {
                int i = rowIndices.getQuick(v);
                double value = values.getQuick(v);
                handler.onElement(i, j, value);
            }
        }
    }

    @Override
    public DenseMatrix toDense() {
        return (DenseMatrix) to(new DenseMatrixFactory());
    }

    @Override
    public SparseMatrix toSparse() {
        return this;
    }

    @Override
    public Matrix to(MatrixFactory factory) {
        Objects.requireNonNull(factory);
        if (factory instanceof SparseMatrixFactory) {
            return this;
        }
        return copy(factory);
    }

    @Override
    public int getValueCount() {
        return values.size();
    }

    private native SparseMatrix transpose(int m, int n, int[] ap, int[] ai, double[] ax);

    @Override
    public SparseMatrix transpose() {
        fillLastEmptyColumns();
        SparseMatrix transposed = transpose(rowCount, columnCount, columnStart, rowIndices.getData(), values.getData());
        transposed.setRgrowthThreshold(rgrowthThreshold);
        return transposed;
    }

    @Override
    public void print(PrintStream out) {
        print(out, null, null);
    }

    @Override
    public void print(PrintStream out, List<String> rowNames, List<String> columnNames) {
        out.println("rowCount=" + rowCount);
        out.println("columnCount=" + columnCount);
        out.println("columnStart=" + Arrays.toString(columnStart));
        out.println("columnValueCount=" + Arrays.toString(columnValueCount));
        out.println("rowIndices=" + rowIndices);
        out.println("values=" + values);
    }

    @Override
    public int hashCode() {
        return rowCount + columnCount + Arrays.hashCode(columnStart) + Arrays.hashCode(columnValueCount) + rowIndices.hashCode() + values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SparseMatrix other) {
            return rowCount == other.rowCount &&
                    columnCount == other.columnCount &&
                    Arrays.equals(columnStart, other.columnStart) &&
                    Arrays.equals(columnValueCount, other.columnValueCount) &&
                    rowIndices.equals(other.rowIndices) &&
                    values.equals(other.values);
        }
        return false;
    }

    public void write(OutputStream outputStream) {
        Objects.requireNonNull(outputStream);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SparseMatrix read(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            // Check that the object to deserialize is really a SparseMatrix.
            // This check is done prior to its complete deserialization to prevent security problems (RCE).
            // - Check that all non-null encountered classes are among the accepted ones (the one composing a SparseMatrix).
            ObjectInputFilter allowedClassesFilter = ObjectInputFilter.allowFilter(ALLOWED_CLASSES::contains, ObjectInputFilter.Status.REJECTED);
            objectInputStream.setObjectInputFilter(allowedClassesFilter);
            return (SparseMatrix) objectInputStream.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
    }

    public void write(Path file) {
        Objects.requireNonNull(file);
        try (OutputStream outputStream = Files.newOutputStream(file)) {
            write(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SparseMatrix read(Path file) {
        Objects.requireNonNull(file);
        try (InputStream inputStream = Files.newInputStream(file)) {
            return read(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
