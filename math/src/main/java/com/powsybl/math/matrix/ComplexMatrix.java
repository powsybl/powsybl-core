/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import java.util.Objects;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class ComplexMatrix {

    private static final double EPSILON = 0.00000001;

    private final DenseMatrix realPartMatrix;
    private final DenseMatrix imagPartMatrix;

    public ComplexMatrix(int rowCount, int columnCount) {
        realPartMatrix = new DenseMatrix(rowCount, columnCount);
        imagPartMatrix = new DenseMatrix(rowCount, columnCount);
    }

    public void set(int i, int j, Complex complex) {
        Objects.requireNonNull(complex);
        realPartMatrix.set(i, j, complex.getReal());
        imagPartMatrix.set(i, j, complex.getImaginary());
    }

    public int getRowCount() {
        return realPartMatrix.getRowCount();
    }

    public int getColumnCount() {
        return realPartMatrix.getColumnCount();
    }

    public Complex get(int i, int j) {
        return new Complex(realPartMatrix.get(i, j), imagPartMatrix.get(i, j));
    }

    public static ComplexMatrix createIdentity(int nbRow) {
        ComplexMatrix complexMatrix = new ComplexMatrix(nbRow, nbRow);
        for (int i = 0; i < nbRow; i++) {
            complexMatrix.realPartMatrix.set(i, i, 1.);
        }
        return complexMatrix;
    }

    public ComplexMatrix transpose() {
        ComplexMatrix transposed = new ComplexMatrix(getColumnCount(), getRowCount());
        for (int i = 0; i < getColumnCount(); i++) {
            for (int j = 0; j < getRowCount(); j++) {
                transposed.realPartMatrix.set(i, j, realPartMatrix.get(j, i));
                transposed.imagPartMatrix.set(i, j, imagPartMatrix.get(j, i));
            }
        }
        return transposed;
    }

    public ComplexMatrix scale(Complex factor) {
        Objects.requireNonNull(factor);
        ComplexMatrix scaled = new ComplexMatrix(getRowCount(), getColumnCount());
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                scaled.realPartMatrix.set(i, j, realPartMatrix.get(i, j) * factor.getReal() - imagPartMatrix.get(i, j) * factor.getImaginary());
                scaled.imagPartMatrix.set(i, j, imagPartMatrix.get(i, j) * factor.getReal() + realPartMatrix.get(i, j) * factor.getImaginary());
            }
        }
        return scaled;
    }

    public ComplexMatrix scale(double factor) {
        return scale(new Complex(factor, 0.));
    }

    // utils to switch between complex and real cartesian representation of a complex matrix
    public DenseMatrix toRealCartesianMatrix() {
        DenseMatrix realMatrix = new DenseMatrix(getRowCount() * 2, getColumnCount() * 2);
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                Complex complexTerm = new Complex(realPartMatrix.get(i, j), imagPartMatrix.get(i, j));
                realMatrix.add(2 * i, 2 * j, complexTerm.getReal());
                realMatrix.add(2 * i + 1, 2 * j + 1, complexTerm.getReal());
                realMatrix.add(2 * i, 2 * j + 1, -complexTerm.getImaginary());
                realMatrix.add(2 * i + 1, 2 * j, complexTerm.getImaginary());
            }
        }

        return realMatrix;
    }

    public static ComplexMatrix fromRealCartesian(DenseMatrix realMatrix) {
        Objects.requireNonNull(realMatrix);
        int columnCount = realMatrix.getColumnCount();
        int rowCount = realMatrix.getRowCount();
        if (columnCount % 2 != 0 || rowCount % 2 != 0) { // dimensions have to be even
            throw new MatrixException("Incompatible matrices dimensions to build a complex matrix from a real cartesian");
        }

        ComplexMatrix complexMatrix = new ComplexMatrix(rowCount / 2, columnCount / 2);
        for (int i = 0; i < rowCount / 2; i++) {
            for (int j = 0; j < columnCount / 2; j++) {

                int rowIndexInCartesian = 2 * i;
                int colIndexInCartesian = 2 * j;

                // Before building the complex matrix term, check that the 4x4 cartesian bloc can be transformed into a Complex term
                double t11 = realMatrix.get(rowIndexInCartesian, colIndexInCartesian);
                double t12 = realMatrix.get(rowIndexInCartesian, colIndexInCartesian + 1);
                double t21 = realMatrix.get(rowIndexInCartesian + 1, colIndexInCartesian);
                double t22 = realMatrix.get(rowIndexInCartesian + 1, colIndexInCartesian + 1);

                if (FastMath.abs(t11 - t22) > EPSILON || FastMath.abs(t12 + t21) > EPSILON) {
                    throw new MatrixException("Incompatible bloc matrices terms to build a complex matrix from a real cartesian");
                }

                complexMatrix.set(i, j, new Complex(t11, t21));
            }
        }
        return complexMatrix;
    }
}
