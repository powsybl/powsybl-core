/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class ComplexMatrix {

    private DenseMatrix realPartMatrix;
    private DenseMatrix imagPartMatrix;

    public ComplexMatrix(int nbRow, int nbCol) {
        this.realPartMatrix = new DenseMatrix(nbRow, nbCol);
        this.imagPartMatrix = new DenseMatrix(nbRow, nbCol);
    }

    // we suppose that the input indices start at 1
    public void set(int i, int j, Complex complex) {
        this.realPartMatrix.set(i - 1, j - 1, complex.getReal());
        this.imagPartMatrix.set(i - 1, j - 1, complex.getImaginary());
    }

    public int getNbRow() {
        return realPartMatrix.getRowCount();
    }

    public int getNbCol() {
        return realPartMatrix.getColumnCount();
    }

    public Complex getTerm(int i, int j) {
        return new Complex(realPartMatrix.get(i - 1, j - 1), imagPartMatrix.get(i - 1, j - 1));
    }

    public static ComplexMatrix complexMatrixIdentity(int nbRow) {
        ComplexMatrix complexMatrix = new ComplexMatrix(nbRow, nbRow);
        for (int i = 0; i < nbRow; i++) {
            complexMatrix.realPartMatrix.set(i, i, 1.);
        }
        return complexMatrix;
    }

    public static ComplexMatrix getTransposed(ComplexMatrix cm) {
        ComplexMatrix complexMatrixTransposed = new ComplexMatrix(cm.getNbCol(), cm.getNbRow());
        for (int i = 0; i < cm.getNbCol(); i++) {
            for (int j = 0; j < cm.getNbRow(); j++) {
                complexMatrixTransposed.realPartMatrix.set(i, j, cm.realPartMatrix.get(j, i));
                complexMatrixTransposed.imagPartMatrix.set(i, j, cm.imagPartMatrix.get(j, i));
            }
        }
        return complexMatrixTransposed;
    }

    public static ComplexMatrix getMatrixScaled(ComplexMatrix cm, Complex factor) {
        ComplexMatrix complexMatrixScaled = new ComplexMatrix(cm.getNbRow(), cm.getNbCol());
        for (int i = 0; i < cm.getNbRow(); i++) {
            for (int j = 0; j < cm.getNbCol(); j++) {
                complexMatrixScaled.realPartMatrix.set(i, j, cm.realPartMatrix.get(i, j) * factor.getReal() - cm.imagPartMatrix.get(i, j) * factor.getImaginary());
                complexMatrixScaled.imagPartMatrix.set(i, j, cm.imagPartMatrix.get(i, j) * factor.getReal() + cm.realPartMatrix.get(i, j) * factor.getImaginary());
            }
        }
        return complexMatrixScaled;
    }

    public static ComplexMatrix getMatrixScaled(ComplexMatrix cm, double factor) {
        return getMatrixScaled(cm, new Complex(factor, 0.));
    }

    // utils to switch between complex and real cartesian representation of a complex matrix
    public DenseMatrix getRealCartesianMatrix() {
        DenseMatrix realMatrix = new DenseMatrix(getNbRow() * 2, getNbCol() * 2);
        for (int i = 0; i < getNbRow(); i++) {
            for (int j = 0; j < getNbCol(); j++) {
                Complex complexTerm = new Complex(realPartMatrix.get(i, j), imagPartMatrix.get(i, j));
                realMatrix.add(2 * i, 2 * j, complexTerm.getReal());
                realMatrix.add(2 * i + 1, 2 * j + 1, complexTerm.getReal());
                realMatrix.add(2 * i, 2 * j + 1, -complexTerm.getImaginary());
                realMatrix.add(2 * i + 1, 2 * j, complexTerm.getImaginary());
            }
        }

        return realMatrix;
    }

    public static ComplexMatrix getComplexMatrixFromRealCartesian(DenseMatrix realMatrix) {

        int nbCol = realMatrix.getColumnCount();
        int nbRow = realMatrix.getRowCount();
        if (nbCol % 2 != 0 || nbRow % 2 != 0) { // dimensions have to be even
            throw new MatrixException("Incompatible matrices dimensions to build a complex matrix from a real cartesian");
        }

        ComplexMatrix complexMatrix = new ComplexMatrix(nbRow / 2, nbCol / 2);
        for (int i = 1; i <= nbRow / 2; i++) {
            for (int j = 1; j <= nbCol / 2; j++) {

                int rowIndexInCartesian = 2 * (i - 1);
                int colIndexInCartesian = 2 * (j - 1);

                // Before building the complex matrix term, check that the 4x4 cartesian bloc can be transformed into a Complex term
                double t11 = realMatrix.get(rowIndexInCartesian, colIndexInCartesian);
                double t12 = realMatrix.get(rowIndexInCartesian, colIndexInCartesian + 1);
                double t21 = realMatrix.get(rowIndexInCartesian + 1, colIndexInCartesian);
                double t22 = realMatrix.get(rowIndexInCartesian + 1, colIndexInCartesian + 1);

                double epsilon = 0.00000001;
                if (FastMath.abs(t11 - t22) > epsilon) {
                    throw new MatrixException("Incompatible bloc matrices terms to build a complex matrix from a real cartesian");
                }

                if (FastMath.abs(t12 + t21) > epsilon) {
                    throw new MatrixException("Incompatible bloc matrices terms to build a complex matrix from a real cartesian");
                }

                Complex complexTerm = new Complex(t11, t21);

                complexMatrix.set(i, j, complexTerm);
            }
        }

        return complexMatrix;
    }

}
