/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Dense matrix implementation based on an array of {@code rowCount} * {@code columnCount} double values.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DenseMatrix extends AbstractMatrix {

    private static final Logger LOGGER = LoggerFactory.getLogger(DenseMatrix.class);

    public static final int MAX_ELEMENT_COUNT = Integer.MAX_VALUE / Double.BYTES;

    public static final DenseMatrix EMPTY = new DenseMatrix(0, 0);

    /**
     * Dense element implementation.
     * An element in a dense matrix is defined by its row index and column index.
     */
    class DenseElement implements Element {

        /**
         * Row index.
         */
        private final int i;

        /**
         * Column index.
         */
        private final int j;

        DenseElement(int i, int j) {
            this.i = i;
            this.j = j;
        }

        @Override
        public void set(double value) {
            DenseMatrix.this.set(i, j, value);
        }

        @Override
        public void add(double value) {
            DenseMatrix.this.add(i, j, value);
        }
    }

    private final int rowCount;

    private final int columnCount;

    private final ByteBuffer buffer;

    private static ByteBuffer createBuffer(int rowCount, int columnCount) {
        try {
            int capacity = Math.multiplyExact(Math.multiplyExact(rowCount, columnCount), Double.BYTES);
            return ByteBuffer.allocateDirect(capacity)
                    .order(ByteOrder.LITTLE_ENDIAN);
        } catch (ArithmeticException e) {
            LOGGER.error(e.toString(), e);
            throw new MatrixException("Too many elements for a dense matrix, maximum allowed is "
                    + MAX_ELEMENT_COUNT);
        }
    }

    public DenseMatrix(int rowCount, int columnCount, double[] values) {
        this(rowCount, columnCount);
        setValues(values);
    }

    public DenseMatrix(int rowCount, int columnCount) {
        this(rowCount, columnCount, () -> createBuffer(rowCount, columnCount));
    }

    public DenseMatrix(int rowCount, int columnCount, Supplier<ByteBuffer> bufferSupplier) {
        if (rowCount < 0) {
            throw new MatrixException("row count has to be positive");
        }
        if (columnCount < 0) {
            throw new MatrixException("column count has to be positive");
        }
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        Objects.requireNonNull(bufferSupplier);
        buffer = bufferSupplier.get();
        if (buffer.capacity() != rowCount * columnCount * Double.BYTES) {
            throw new MatrixException("values size (" + buffer.capacity() +
                    ") is incorrect (should be " + rowCount * columnCount + ")");
        }
    }

    public DenseMatrix(Jama.Matrix matrix) {
        this(matrix.getRowDimension(), matrix.getColumnDimension(), matrix.getColumnPackedCopy());
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
        return getUnsafe(i, j);
    }

    private double getUnsafe(int i, int j) {
        return buffer.getDouble(j * Double.BYTES * rowCount + i * Double.BYTES);
    }

    /**
     * @deprecated Use {@link #get(int, int)} instead.
     */
    @Deprecated(since = "2.5.0")
    public double getValue(int i, int j) {
        return get(i, j);
    }

    private void setUnsafe(int i, int j, double value) {
        buffer.putDouble(j * Double.BYTES * rowCount + i * Double.BYTES, value);
    }

    @Override
    public void set(int i, int j, double value) {
        checkBounds(i, j);
        setUnsafe(i, j, value);
    }

    private void addUnsafe(int i, int j, double value) {
        int index = j * Double.BYTES * rowCount + i * Double.BYTES;
        buffer.putDouble(index, buffer.getDouble(index) + value);
    }

    @Override
    public void add(int i, int j, double value) {
        checkBounds(i, j);
        addUnsafe(i, j, value);
    }

    @Override
    public Element addAndGetElement(int i, int j, double value) {
        add(i, j, value);
        return new DenseElement(i, j);
    }

    @Override
    public int addAndGetIndex(int i, int j, double value) {
        add(i, j, value);
        return j * rowCount + i;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= rowCount * columnCount) {
            throw new MatrixException("Element index out of bound [0, " + (rowCount * columnCount - 1) + "]");
        }
    }

    @Override
    public void setAtIndex(int index, double value) {
        checkElementIndex(index);
        setQuickAtIndex(index, value);
    }

    @Override
    public void setQuickAtIndex(int index, double value) {
        int i = index % rowCount;
        int j = index / rowCount;
        setUnsafe(i, j, value);
    }

    @Override
    public void addAtIndex(int index, double value) {
        checkElementIndex(index);
        addQuickAtIndex(index, value);
    }

    @Override
    public void addQuickAtIndex(int index, double value) {
        int i = index % rowCount;
        int j = index / rowCount;
        addUnsafe(i, j, value);
    }

    @Override
    public void reset() {
        for (int k = 0; k < rowCount * columnCount; k++) {
            buffer.putDouble(k * Double.BYTES, 0);
        }
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    ByteBuffer getBuffer() {
        return buffer;
    }

    void setValues(double[] values) {
        if (values.length != rowCount * columnCount) {
            throw new MatrixException("Incorrect values array size "
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

    @Override
    public LUDecomposition decomposeLU() {
        return new DenseLUDecomposition(this);
    }

    @Override
    public Matrix times(Matrix other, double scalar) {
        return times(other.toDense(), scalar);
    }

    public DenseMatrix times(DenseMatrix other, double scalar) {
        Objects.requireNonNull(other);
        if (other.rowCount != columnCount) {
            throw new MatrixException("Invalid matrices inner dimension");
        }

        DenseMatrix result = new DenseMatrix(rowCount, other.columnCount);

        double[] otherColumnJ = new double[columnCount];
        for (int j = 0; j < other.columnCount; j++) {
            for (int k = 0; k < columnCount; k++) {
                otherColumnJ[k] = other.getUnsafe(k, j);
            }
            for (int i = 0; i < rowCount; i++) {
                double s = 0;
                for (int k = 0; k < columnCount; k++) {
                    s += getUnsafe(i, k) * otherColumnJ[k];
                }
                result.setUnsafe(i, j, s * scalar);
            }
        }

        return result;
    }

    public DenseMatrix times(DenseMatrix other) {
        return times(other, 1d);
    }

    public DenseMatrix add(DenseMatrix other, double alpha, double beta) {
        Objects.requireNonNull(other);
        if (other.rowCount != rowCount || other.columnCount != columnCount) {
            throw new MatrixException("Incompatible matrices dimensions");
        }

        DenseMatrix result = new DenseMatrix(rowCount, columnCount);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                result.setUnsafe(i, j, alpha * getUnsafe(i, j) + beta * other.getUnsafe(i, j));
            }
        }

        return result;
    }

    @Override
    public Matrix add(Matrix other, double alpha, double beta) {
        return add(other.toDense(), alpha, beta);
    }

    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        Objects.requireNonNull(handler);
        for (int j = 0; j < getColumnCount(); j++) {
            iterateNonZeroValueOfColumn(j, handler);
        }
    }

    @Override
    public void iterateNonZeroValueOfColumn(int j, ElementHandler handler) {
        for (int i = 0; i < getRowCount(); i++) {
            double value = get(i, j);
            if (value != 0) {
                handler.onElement(i, j, value);
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
    public int getValueCount() {
        return getRowCount() * getColumnCount();
    }

    @Override
    public DenseMatrix transpose() {
        int transposedRowCount = columnCount;
        int transposedColumnCount = rowCount;
        ByteBuffer transposedBuffer = createBuffer(transposedRowCount, transposedColumnCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                double value = this.buffer.getDouble(j * Double.BYTES * rowCount + i * Double.BYTES);
                transposedBuffer.putDouble(i * Double.BYTES * transposedRowCount + j * Double.BYTES, value);
            }
        }
        return new DenseMatrix(transposedRowCount, transposedColumnCount, () -> transposedBuffer);
    }

    /**
     * Copy all the values that are in an originalMatrix and paste it in the current DenseMatrix (without allocating new memory spaces)
     * The dimensions of both matrices must be the same
     */
    public void copyValuesFrom(DenseMatrix originalMatrix) {
        if (originalMatrix.getRowCount() == getRowCount() && originalMatrix.getColumnCount() == getColumnCount()) {
            for (int columnIndex = 0; columnIndex < originalMatrix.getColumnCount(); columnIndex++) {
                for (int rowIndex = 0; rowIndex < originalMatrix.getRowCount(); rowIndex++) {
                    set(rowIndex, columnIndex, originalMatrix.get(rowIndex, columnIndex));
                }
            }
        } else {
            throw new MatrixException("Incompatible matrix dimensions when copying values. Received (" + originalMatrix.getRowCount() + ", " + originalMatrix.getColumnCount() + ") but expected (" + getRowCount() + ", " + getColumnCount() + ")");
        }
    }

    public void resetRow(int row) {
        if (row >= 0 && row < getRowCount()) {
            for (int j = 0; j < getColumnCount(); j++) {
                set(row, j, 0);
            }
        } else {
            throw new IllegalArgumentException("Row value out of bounds. Expected in range [0," + (getRowCount() - 1) + "] but received " + row);
        }
    }

    public void resetColumn(int column) {
        if (column >= 0 && column < getColumnCount()) {
            for (int i = 0; i < getRowCount(); i++) {
                set(i, column, 0);
            }
        } else {
            throw new IllegalArgumentException("Column value out of bounds. Expected in range [0," + (getColumnCount() - 1) + "] but received " + column);
        }
    }

    public void removeSmallValues(double epsilonValue) {
        if (epsilonValue >= 0) {
            for (int i = 0; i < getRowCount(); i++) {
                for (int j = 0; j < getColumnCount(); j++) {
                    if (Math.abs(get(i, j)) < epsilonValue) {
                        set(i, j, 0.);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Argument epsilonValue should be positive but received " + epsilonValue);
        }
    }

    @Override
    public void print(PrintStream out) {
        print(out, null, null);
    }

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

    @Override
    public int hashCode() {
        return rowCount + columnCount + buffer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DenseMatrix other) {
            return rowCount == other.rowCount && columnCount == other.columnCount && buffer.equals(other.buffer);
        }
        return false;
    }
}
