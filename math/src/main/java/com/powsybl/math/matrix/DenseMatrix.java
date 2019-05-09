/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import com.google.common.base.Strings;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

/**
 * Dense matrix implementation based on an array of {@code rowCount} * {@code columnCount} double values.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DenseMatrix extends AbstractMatrix {

    private final int rowCount;

    private final int columnCount;

    private final ByteBuffer buffer;

    private static ByteBuffer createBuffer(int m, int n) {
        return ByteBuffer.allocateDirect(m * n * Double.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    public DenseMatrix(int rowCount, int columnCount, double[] values) {
        this(rowCount, columnCount);
        setValues(values);
    }

    public DenseMatrix(int rowCount, int columnCount) {
        this(rowCount, columnCount, createBuffer(rowCount, columnCount));
    }

    public DenseMatrix(int rowCount, int columnCount, ByteBuffer buffer) {
        if (rowCount < 0) {
            throw new IllegalArgumentException("row count has to be positive");
        }
        if (columnCount < 0) {
            throw new IllegalArgumentException("column count has to be positive");
        }
        if (buffer.capacity() != rowCount * columnCount * Double.BYTES) {
            throw new IllegalArgumentException("values size (" + buffer.capacity() +
                    ") is incorrect (should be " + rowCount * columnCount + ")");
        }
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.buffer = Objects.requireNonNull(buffer);
    }

    public DenseMatrix(Jama.Matrix matrix) {
        this(matrix.getRowDimension(), matrix.getColumnDimension(), matrix.getColumnPackedCopy());
    }

    private void checkBounds(int i, int j) {
        if (j < 0 || j >= columnCount) {
            throw new IllegalArgumentException("Bad column index: " + j);
        }
        if (i < 0 || i >= rowCount) {
            throw new IllegalArgumentException("Bad row index: " + i);
        }
    }

    /**
     * Get value at row {@code i} and  column {@code j}.
     *
     * @param i row index
     * @param j column index
     * @return value at row {@code i} and column {@code j}
     */
    public double get(int i, int j) {
        checkBounds(i, j);
        return buffer.getDouble(j * Double.BYTES * rowCount + i * Double.BYTES);
    }

    /**
     * @deprecated Use {@link #get(int, int)} instead.
     */
    @Deprecated
    public double getValue(int i, int j) {
        return get(i, j);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(int i, int j, double value) {
        checkBounds(i, j);
        buffer.putDouble(j * Double.BYTES * rowCount + i * Double.BYTES, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int i, int j, double value) {
        checkBounds(i, j);
        int index = j * Double.BYTES * rowCount + i * Double.BYTES;
        buffer.putDouble(index, buffer.getDouble(index) + value);
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

    ByteBuffer getBuffer() {
        return buffer;
    }

    void setValues(double[] values) {
        if (values.length != rowCount * columnCount) {
            throw new IllegalArgumentException("Incorrect values array size "
                    + values.length + ", expected " + rowCount * columnCount);
        }
        for (int i = 0; i < values.length; i++) {
            buffer.putDouble(i * Double.BYTES, values[i]);
        }
    }

    private double[] getValuesCopy() {
        double[] values = new double[rowCount * columnCount];
        buffer.asDoubleBuffer().get(values);
        return values;
    }

    /**
     * Convert to <a href="Jama https://math.nist.gov/javanumerics/jama/">Jama</a> matrix.
     *
     * @return a Jama matrix
     */
    Jama.Matrix toJamaMatrix() {
        return new Jama.Matrix(getValuesCopy(), rowCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LUDecomposition decomposeLU() {
        return new DenseLUDecomposition(toJamaMatrix().lu());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix times(Matrix other) {
        return new DenseMatrix(toJamaMatrix().times(other.toDense().toJamaMatrix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        Objects.requireNonNull(handler);
        for (int j = 0; j < getColumnCount(); j++) {
            iterateNonZeroValueOfColumn(j, handler);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iterateNonZeroValueOfColumn(int j, ElementHandler handler) {
        for (int i = 0; i < getRowCount(); i++) {
            double value = get(i, j);
            if (value != 0) {
                handler.onElement(i, j, value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DenseMatrix toDense() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparseMatrix toSparse() {
        return (SparseMatrix) to(new SparseMatrixFactory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix to(MatrixFactory factory) {
        Objects.requireNonNull(factory);
        if (factory instanceof DenseMatrixFactory) {
            return this;
        }
        return copy(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getEstimatedNonZeroValueCount() {
        return getRowCount() * getColumnCount();
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
        int rowNamesWidth = getMaxWidthAmongRowNames(rowNames);

        int[] width = getMaxWidthForEachColumn(columnNames);

        if (columnNames != null) {
            if (rowNames != null) {
                out.print(Strings.repeat(" ", rowNamesWidth + 1));
            }
            for (int j = 0; j < getColumnCount(); j++) {
                out.print(Strings.padStart(columnNames.get(j), width[j] + 1, ' '));
            }
            out.println();
        }
        for (int i = 0; i < getRowCount(); i++) {
            if (rowNames != null) {
                out.print(Strings.padStart(rowNames.get(i), rowNamesWidth + 1, ' '));
            }
            for (int j = 0; j < getColumnCount(); j++) {
                out.print(Strings.padStart(Double.toString(get(i, j)), width[j] + 1, ' '));
            }
            out.println();
        }
    }

    /**
     * {@inheritDoc}
     */
    private int getMaxWidthAmongRowNames(List<String> rowNames) {
        int rowNamesWidth = 0;
        if (rowNames != null) {
            for (String rowName : rowNames) {
                rowNamesWidth = Math.max(rowNamesWidth, rowName.length());
            }
        }
        return rowNamesWidth;
    }

    /**
     * {@inheritDoc}
     */
    private int[] getMaxWidthForEachColumn(List<String> columnNames) {
        int[] width = new int[getColumnCount()];
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                width[j] = Math.max(width[j], Double.toString(get(i, j)).length());
                if (columnNames != null) {
                    width[j] = Math.max(width[j], columnNames.get(j).length());
                }
            }
        }
        return width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return rowCount + columnCount + buffer.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DenseMatrix) {
            DenseMatrix other = (DenseMatrix) obj;
            return rowCount == other.rowCount && columnCount == other.columnCount && buffer.equals(other.buffer);
        }
        return false;
    }
}
