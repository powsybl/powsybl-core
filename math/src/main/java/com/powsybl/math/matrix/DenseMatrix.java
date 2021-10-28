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
import java.util.function.Supplier;

/**
 * Dense matrix implementation based on an array of {@code rowCount} * {@code columnCount} double values.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DenseMatrix extends AbstractMatrix {

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
        return ByteBuffer.allocateDirect(rowCount * columnCount * Double.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
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
            throw new IllegalArgumentException("row count has to be positive");
        }
        if (columnCount < 0) {
            throw new IllegalArgumentException("column count has to be positive");
        }
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        Objects.requireNonNull(bufferSupplier);
        buffer = bufferSupplier.get();
        if (buffer.capacity() != rowCount * columnCount * Double.BYTES) {
            throw new IllegalArgumentException("values size (" + buffer.capacity() +
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
        return buffer.getDouble(j * Double.BYTES * rowCount + i * Double.BYTES);
    }

    /**
     * @deprecated Use {@link #get(int, int)} instead.
     */
    @Deprecated
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
            throw new IllegalArgumentException("Element index out of bound [0, " + (rowCount * columnCount - 1) + "]");
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

    @Override
    public LUDecomposition decomposeLU() {
        return new DenseLUDecomposition(this);
    }

    @Override
    public Matrix times(Matrix other) {
        return new DenseMatrix(toJamaMatrix().times(other.toDense().toJamaMatrix()));
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
    protected int getEstimatedNonZeroValueCount() {
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
        if (obj instanceof DenseMatrix) {
            DenseMatrix other = (DenseMatrix) obj;
            return rowCount == other.rowCount && columnCount == other.columnCount && buffer.equals(other.buffer);
        }
        return false;
    }
}
