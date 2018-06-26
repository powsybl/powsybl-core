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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DenseMatrix extends AbstractMatrix {

    private final int m;

    private final int n;

    private final ByteBuffer buffer;

    private static ByteBuffer createBuffer(int m, int n) {
        return ByteBuffer.allocateDirect(m * n * Double.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    public DenseMatrix(int m, int n, double[] values) {
        this(m, n);
        setValues(values);
    }

    public DenseMatrix(int m, int n) {
        this(m, n, createBuffer(m, n));
    }

    public DenseMatrix(int m, int n, ByteBuffer buffer) {
        if (m < 0) {
            throw new IllegalArgumentException("row count has to be positive");
        }
        if (n < 0) {
            throw new IllegalArgumentException("column count has to be positive");
        }
        if (buffer.capacity() != m * n * Double.BYTES) {
            throw new IllegalArgumentException("values size (" + buffer.capacity() +
                    ") is incorrect (should be " + m * n + ")");
        }
        this.m = m;
        this.n = n;
        this.buffer = Objects.requireNonNull(buffer);
    }

    public DenseMatrix(Jama.Matrix matrix) {
        this(matrix.getRowDimension(), matrix.getColumnDimension(), matrix.getColumnPackedCopy());
    }

    private void checkBounds(int i, int j) {
        if (j < 0 || j >= n) {
            throw new IllegalArgumentException("Bad column index: " + j);
        }
        if (i < 0 || i >= m) {
            throw new IllegalArgumentException("Bad row index: " + i);
        }
    }

    public double getValue(int i, int j) {
        checkBounds(i, j);
        return buffer.getDouble(j * Double.BYTES * m + i * Double.BYTES);
    }

    @Override
    public void setValue(int i, int j, double value) {
        checkBounds(i, j);
        buffer.putDouble(j * Double.BYTES * m + i * Double.BYTES, value);
    }

    public void addValue(int i, int j, double value) {
        checkBounds(i, j);
        int index = j * Double.BYTES * m + i * Double.BYTES;
        buffer.putDouble(index, buffer.getDouble(index) + value);
    }

    @Override
    public int getM() {
        return m;
    }

    @Override
    public int getN() {
        return n;
    }

    ByteBuffer getBuffer() {
        return buffer;
    }

    void setValues(double[] values) {
        if (values.length != m * n) {
            throw new IllegalArgumentException("Incorrect values array size "
                    + values.length + ", expected " + m * n);
        }
        for (int i = 0; i < values.length; i++) {
            buffer.putDouble(i * Double.BYTES, values[i]);
        }
    }

    double[] getValuesCopy() {
        double[] values = new double[m * n];
        buffer.asDoubleBuffer().get(values);
        return values;
    }

    Jama.Matrix toJamaMatrix() {
        return new Jama.Matrix(getValuesCopy(), m);
    }

    @Override
    public LUDecomposition decomposeLU() {
        return new DenseLUDecomposition(toJamaMatrix().lu());
    }

    @Override
    public Matrix times(Matrix other) {
        return new DenseMatrix(toJamaMatrix().times(other.toDense().toJamaMatrix()));
    }

    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        Objects.requireNonNull(handler);
        for (int j = 0; j < getN(); j++) {
            iterateNonZeroValueOfColumn(j, handler);
        }
    }

    @Override
    public void iterateNonZeroValueOfColumn(int j, ElementHandler handler) {
        for (int i = 0; i < getM(); i++) {
            double value = getValue(i, j);
            if (value != 0) {
                handler.onValue(i, j, value);
            }
        }
    }

    @Override
    public DenseMatrix toDense() {
        return this;
    }

    @Override
    public SparseMatrix toSparse() {
        return (SparseMatrix) to(new SparseMatrixFactory());
    }

    @Override
    public Matrix to(MatrixFactory factory) {
        Objects.requireNonNull(factory);
        if (factory instanceof DenseMatrixFactory) {
            return this;
        }
        return copy(factory);
    }

    @Override
    protected int getEstimatedNonZeroValueCount() {
        return getM() * getN();
    }

    @Override
    public void print() {
        print(System.out);
    }

    @Override
    public void print(PrintStream out) {
        print(out, null, null);
    }

    @Override
    public void print(List<String> rowNames, List<String> columnNames) {
        print(System.out, rowNames, columnNames);
    }

    @Override
    public void print(PrintStream out, List<String> rowNames, List<String> columnNames) {
        int rowNamesWidth = getMaxWidthAmongRowNames(rowNames);

        int[] width = getMaxWidthForEachColumn(columnNames);

        if (columnNames != null) {
            if (rowNames != null) {
                out.print(Strings.repeat(" ", rowNamesWidth + 1));
            }
            for (int j = 0; j < getN(); j++) {
                out.print(Strings.padStart(columnNames.get(j), width[j] + 1, ' '));
            }
            out.println();
        }
        for (int i = 0; i < getM(); i++) {
            if (rowNames != null) {
                out.print(Strings.padStart(rowNames.get(i), rowNamesWidth + 1, ' '));
            }
            for (int j = 0; j < getN(); j++) {
                out.print(Strings.padStart(Double.toString(getValue(i, j)), width[j] + 1, ' '));
            }
            out.println();
        }
    }

    private int getMaxWidthAmongRowNames(List<String> rowNames) {
        int rowNamesWidth = 0;
        if (rowNames != null) {
            for (String rowName : rowNames) {
                rowNamesWidth = Math.max(rowNamesWidth, rowName.length());
            }
        }
        return rowNamesWidth;
    }

    private int[] getMaxWidthForEachColumn(List<String> columnNames) {
        int[] width = new int[getN()];
        for (int i = 0; i < getM(); i++) {
            for (int j = 0; j < getN(); j++) {
                width[j] = Math.max(width[j], Double.toString(getValue(i, j)).length());
                if (columnNames != null) {
                    width[j] = Math.max(width[j], columnNames.get(j).length());
                }
            }
        }
        return width;
    }

    @Override
    public int hashCode() {
        return m + n + buffer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DenseMatrix) {
            DenseMatrix other = (DenseMatrix) obj;
            return m == other.m && n == other.n && buffer.equals(other.buffer);
        }
        return false;
    }
}
