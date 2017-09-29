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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Matrix {

    static Matrix createFromColumn(double[] c, MatrixFactory matrixFactory) {
        Objects.requireNonNull(c);
        Objects.requireNonNull(matrixFactory);
        Matrix m = matrixFactory.create(c.length, 1, c.length);
        for (int i = 0; i < c.length; i++) {
            m.setValue(i, 0, c[i]);
        }
        return m;
    }

    interface ElementHandler {

        void onValue(int i, int j, double value);
    }

    int getM();

    int getN();

    void setValue(int i, int j, double value);

    LUDecomposition decomposeLU();

    Matrix times(Matrix other);

    void iterateNonZeroValue(ElementHandler handler);

    void iterateNonZeroValueOfColumn(int j, ElementHandler handler);

    DenseMatrix toDense();

    SparseMatrix toSparse();

    Matrix to(MatrixFactory factory);

    Matrix copy(MatrixFactory factory);

    void print();

    void print(List<String> rowNames, List<String> columnNames);

    void print(PrintStream out, List<String> rowNames, List<String> columnNames);

    void print(PrintStream out);
}
