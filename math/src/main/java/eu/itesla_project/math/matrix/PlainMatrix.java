/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.math.matrix;

import com.google.common.base.Strings;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlainMatrix implements Matrix {

    private final Jama.Matrix matrix;

    public PlainMatrix(int m, int n) {
        this(new double[m][n]);
    }

    public PlainMatrix(double[][] values) {
        this(new Jama.Matrix(values));
    }

    private PlainMatrix(Jama.Matrix matrix) {
        this.matrix = Objects.requireNonNull(matrix);
    }

    public double getValue(int i, int j) {
        return matrix.get(i, j);
    }

    @Override
    public void setValue(int i, int j, double value) {
        matrix.set(i, j, value);
    }

    @Override
    public int getM() {
        return matrix.getRowDimension();
    }

    @Override
    public int getN() {
        return matrix.getColumnDimension();
    }

    @Override
    public LUDecomposition decomposeLU() {
        return new PlainLUDecomposition(matrix.lu());
    }

    @Override
    public Matrix times(Matrix other) {
        return new PlainMatrix(matrix.times(other.toPlain().matrix));
    }

    @Override
    public void iterateNonZeroValue(ElementHandler handler) {
        Objects.requireNonNull(handler);
        for (int i = 0; i < getM(); i++) {
            for (int j = 0; j < getN(); j++) {
                double value = matrix.get(i, j);
                if (value != 0) {
                    handler.onValue(i, j, value);
                }
            }
        }
    }

    @Override
    public PlainMatrix toPlain() {
        return this;
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
        int rowNamesWidth = 0;
        if (rowNames != null) {
            for (String rowName : rowNames) {
                rowNamesWidth = Math.max(rowNamesWidth, rowName.length());
            }
        }
        int[] width = new int[getN()];
        for (int i = 0; i < getM(); i++) {
            for (int j = 0; j < getN(); j++) {
                width[j] = Math.max(width[j], Double.toString(matrix.get(i, j)).length());
                if (columnNames != null) {
                    width[j] = Math.max(width[j], columnNames.get(j).length());
                }
            }
        }

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
                out.print(Strings.padStart(Double.toString(matrix.get(i, j)), width[j] + 1, ' '));
            }
            out.println();
        }
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(matrix.getArray());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlainMatrix) {
            return Arrays.deepEquals(matrix.getArray(), ((PlainMatrix) obj).matrix.getArray());
        }
        return false;
    }
}
