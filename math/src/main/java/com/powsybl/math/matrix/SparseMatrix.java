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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SparseMatrix extends AbstractMatrix {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparseMatrix.class);

    public static final boolean NATIVE_INIT;

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

    private final int m;
    private final int n;
    private final int[] columnStart; // plus value count in the last element
    private final TIntArrayListHack rowIndices;
    private final TDoubleArrayListHack values;

    private int currentColumn = -1; // just for matrix filling

    public SparseMatrix(int m, int n, int[] columnStart, int[] rowIndices, double[] values) {
        this.m = m;
        this.n = n;
        this.columnStart = Objects.requireNonNull(columnStart);
        this.rowIndices = new TIntArrayListHack(Objects.requireNonNull(rowIndices));
        this.values = new TDoubleArrayListHack(Objects.requireNonNull(values));
    }

    public SparseMatrix(int m, int n, int estimatedNonZeroValueCount) {
        this.m = m;
        this.n = n;
        columnStart = new int[n + 1];
        Arrays.fill(columnStart, -1);
        this.columnStart[n] = 0;
        rowIndices = new TIntArrayListHack(estimatedNonZeroValueCount);
        values = new TDoubleArrayListHack(estimatedNonZeroValueCount);
    }

    int[] getColumnStart() {
        return columnStart;
    }

    int[] getRowIndices() {
        return rowIndices.getData();
    }

    double[] getValues() {
        return values.getData();
    }

    @Override
    public int getM() {
        return m;
    }

    @Override
    public int getN() {
        return n;
    }

    @Override
    public void setValue(int m, int n, double value) {
        if (n == currentColumn) {
            // ok, continue to fill row
        } else if (n > currentColumn) {
            // start new column
            columnStart[n] = values.size();
            currentColumn = n;
        } else {
            throw new PowsyblException("Columns have to be filled in the right order");
        }
        values.add(value);
        rowIndices.add(m);
        columnStart[columnStart.length - 1] = values.size();
    }

    @Override
    public LUDecomposition decomposeLU() {
        checkNativeInit();
        return new SparseLUDecomposition(this);
    }

    private native SparseMatrix times(int m1, int n1, int[] ap1, int[] ai1, double[] ax1, int m2, int n2, int[] ap2, int[] ai2, double[] ax2);

    @Override
    public Matrix times(Matrix other) {
        checkNativeInit();
        if (!(other instanceof SparseMatrix)) {
            throw new PowsyblException("Sparse and dense matrix multiplication is not supported");
        }
        SparseMatrix o = (SparseMatrix) other;
        return times(m, n, columnStart, rowIndices.getData(), values.getData(),
                     o.m, o.n, o.columnStart, o.rowIndices.getData(), o.values.getData());
    }

    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        for (int j = 0; j < n; j++) {
            iterateNonZeroValueOfColumn(j, handler);
        }
    }

    @Override
    public void iterateNonZeroValueOfColumn(int j, ElementHandler handler) {
        int first = columnStart[j];
        if (first != -1) {
            int last = j < columnStart.length - 1 ? columnStart[j + 1] : values.size();
            for (int v = first; v < last; v++) {
                int i = rowIndices.getQuick(v);
                double value = values.getQuick(v);
                handler.onValue(i, j, value);
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
    protected int getEstimatedNonZeroValueCount() {
        return values.size();
    }

    @Override
    public void print() {
        print(System.out);
    }

    @Override
    public void print(List<String> rowNames, List<String> columnNames) {
        print(System.out, rowNames, columnNames);
    }

    @Override
    public void print(PrintStream out) {
        print(out, null, null);
    }

    @Override
    public void print(PrintStream out, List<String> rowNames, List<String> columnNames) {
        out.println("m=" + m);
        out.println("n=" + n);
        out.println("columnStart=" + Arrays.toString(columnStart));
        out.println("rowIndices=" + rowIndices);
        out.println("values=" + values);
    }

    @Override
    public int hashCode() {
        return m + n + Arrays.hashCode(columnStart) + rowIndices.hashCode() + values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SparseMatrix) {
            SparseMatrix other = (SparseMatrix) obj;
            return m == other.m &&
                    n == other.n &&
                    Arrays.equals(columnStart, other.columnStart) &&
                    rowIndices.equals(other.rowIndices) &&
                    values.equals(other.values);
        }
        return false;
    }
}
