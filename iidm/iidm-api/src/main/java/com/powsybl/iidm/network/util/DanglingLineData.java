/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.util.LinkData.Flow;

import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class DanglingLineData {

    private static final String PROPERTY_V = "v";
    private static final String PROPERTY_ANGLE = "angle";

    private final DanglingLine danglingLine;

    private double networkFlowP;
    private double networkFlowQ;
    private double boundaryBusU;
    private double boundaryBusTheta;
    private double boundaryFlowP;
    private double boundaryFlowQ;

    public DanglingLineData(DanglingLine danglingLine) {
        this(danglingLine, true);
    }

    public DanglingLineData(DanglingLine danglingLine, boolean splitShuntAdmittance) {
        this.danglingLine = Objects.requireNonNull(danglingLine);

        double networkU = getV(danglingLine);
        double networkTheta = getTheta(danglingLine);

        if (isDcApproximation(networkU, networkTheta)) {
            danglingLineDataDcApproximation(networkTheta, splitShuntAdmittance);
        } else {
            danglingLineData(networkU, networkTheta, splitShuntAdmittance);
        }
    }

    private boolean isDcApproximation(double networkU, double networkTheta) {
        return Double.isNaN(networkU) && !Double.isNaN(networkTheta);
    }

    private void danglingLineData(double networkU, double networkTheta, boolean splitShuntAdmittance) {
        Complex networkFlow = new Complex(danglingLine.getTerminal().getP(), danglingLine.getTerminal().getQ());

        if (!isVoltageValid(networkU, networkTheta)) {
            dlDataWhenThereIsNotVoltageAtNetworkSide(networkFlow);
            return;
        }

        if (isZ0(danglingLine)) {
            dlDataWhenThereIsVoltageAtNetworkSideAndIsZ0(networkU, networkTheta, networkFlow);
            return;
        }

        // boundary voltage from properties, paired and not paired danglingLines are considered
        double boundaryV = getDoubleProperty(danglingLine, PROPERTY_V);
        double boundaryTheta = Math.toRadians(getDoubleProperty(danglingLine, PROPERTY_ANGLE));

        if (isVoltageValid(boundaryV, boundaryTheta)) {
            dlDataWhenThereAreVoltagesAtBothSides(networkU, networkTheta, boundaryV, boundaryTheta, networkFlow, splitShuntAdmittance);
            return;
        }

        // Voltage and flow at the network side are available
        if (isFlowValid(networkFlow)) {
            dlDataWhenThereAreVoltageAndFlowAtNetworkSide(networkU, networkTheta, networkFlow, splitShuntAdmittance);
            return;
        }

        dlDataNotSupportedCases(networkFlow);
    }

    private void danglingLineDataDcApproximation(double networkTheta, boolean splitShuntAdmittance) {

        double networkFlow = danglingLine.getTerminal().getP();

        if (isZ0(danglingLine)) {
            dcApproximationDlDataWhenThereIsVoltageAtNetworkSideAndIsZ0(networkTheta, networkFlow);
            return;
        }

        // boundary voltage angle from properties, paired and not paired danglingLines are considered
        double boundaryTheta = Math.toRadians(getDoubleProperty(danglingLine, PROPERTY_ANGLE));

        if (isDcVoltageValid(boundaryTheta)) {
            dcApproximationDlDataWhenThereAreVoltagesAtBothSides(networkTheta, boundaryTheta, networkFlow);
            return;
        }

        // Voltage and flow at the network side are available
        if (isDcFlowValid(networkFlow)) {
            dcApproximationDlDataWhenThereAreVoltageAndFlowAtNetworkSide(networkTheta, networkFlow, splitShuntAdmittance);
            return;
        }

        dcApproximationDlDataNotSupportedCases(networkFlow);
    }

    private void dlDataWhenThereIsNotVoltageAtNetworkSide(Complex networkFlow) {

        boundaryBusU = Double.NaN;
        boundaryBusTheta = Double.NaN;
        boundaryFlowP = isZ0(danglingLine) ? -networkFlow.getReal() : Double.NaN;
        boundaryFlowQ = isZ0(danglingLine) ? -networkFlow.getImaginary() : Double.NaN;
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private void dlDataWhenThereIsVoltageAtNetworkSideAndIsZ0(double networkU, double networkTheta, Complex networkFlow) {

        boundaryBusU = networkU;
        boundaryBusTheta = networkTheta;
        boundaryFlowP = -networkFlow.getReal();
        boundaryFlowQ = -networkFlow.getImaginary();
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private void dlDataWhenThereAreVoltagesAtBothSides(double networkU, double networkTheta, double vBoundary,
        double tethaBoundary, Complex networkFlow, boolean splitShuntAdmittance) {

        double g1 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : danglingLine.getG();
        double b1 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : danglingLine.getB();
        double g2 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : 0.0;
        double b2 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : 0.0;

        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(danglingLine.getR(),
            danglingLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(g1, b1), new Complex(g2, b2));
        Flow flow = LinkData.flowBothEnds(adm.y11(), adm.y12(), adm.y21(), adm.y22(), networkU, networkTheta, vBoundary,
            tethaBoundary);

        boundaryBusU = vBoundary;
        boundaryBusTheta = tethaBoundary;
        boundaryFlowP = flow.getToFrom().getReal();
        boundaryFlowQ = flow.getToFrom().getImaginary();
        networkFlowP = Double.isNaN(networkFlow.getReal()) ? flow.getFromTo().getReal() : networkFlow.getReal();
        networkFlowQ = Double.isNaN(networkFlow.getImaginary()) ? flow.getFromTo().getImaginary() : networkFlow.getImaginary();
    }

    private void dlDataWhenThereAreVoltageAndFlowAtNetworkSide(double networkU, double networkTheta,
        Complex networkFlow, boolean splitShuntAdmittance) {

        SV sv = new SV(networkFlow.getReal(), networkFlow.getImaginary(), networkU, Math.toDegrees(networkTheta), Branch.Side.ONE)
            .otherSide(danglingLine, splitShuntAdmittance);

        boundaryBusU = sv.getU();
        boundaryBusTheta = Math.toRadians(sv.getA());
        boundaryFlowP = sv.getP();
        boundaryFlowQ = sv.getQ();
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    // it is possible to calculate the boundary voltage by knowing the network side voltage
    // and the boundary injection (with and without voltage control), nevertheless
    // these configurations are not considered in the current version
    private void dlDataNotSupportedCases(Complex networkFlow) {
        boundaryBusU = Double.NaN;
        boundaryBusTheta = Double.NaN;
        boundaryFlowP = Double.NaN;
        boundaryFlowQ = Double.NaN;
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private void dcApproximationDlDataWhenThereIsVoltageAtNetworkSideAndIsZ0(double networkTheta, double networkFlow) {

        boundaryBusU = Double.NaN;
        boundaryBusTheta = networkTheta;
        boundaryFlowP = -networkFlow;
        boundaryFlowQ = Double.NaN;
        networkFlowP = networkFlow;
        networkFlowQ = Double.NaN;
    }

    private void dcApproximationDlDataWhenThereAreVoltagesAtBothSides(double networkTheta, double boundaryTheta,
        double networkFlow) {

        double xpu = danglingLine.getX() / (danglingLine.getTerminal().getVoltageLevel().getNominalV()
            * danglingLine.getTerminal().getVoltageLevel().getNominalV());
        double b = xpu / danglingLine.getX();

        double boundaryflow = b * (boundaryTheta - networkTheta);
        boundaryBusU = Double.NaN;
        boundaryBusTheta = boundaryTheta;
        boundaryFlowP = boundaryflow;
        boundaryFlowQ = Double.NaN;
        networkFlowP = Double.isNaN(networkFlow) ? -boundaryflow : networkFlow;
        networkFlowQ = Double.NaN;
    }

    private void dcApproximationDlDataWhenThereAreVoltageAndFlowAtNetworkSide(double networkTheta, double networkFlow,
        boolean splitShuntAdmittance) {

        SV sv = new SV(networkFlow, Double.NaN, Double.NaN, Math.toDegrees(networkTheta), Branch.Side.ONE)
            .otherSide(danglingLine, splitShuntAdmittance);

        boundaryBusU = sv.getU();
        boundaryBusTheta = Math.toRadians(sv.getA());
        boundaryFlowP = sv.getP();
        boundaryFlowQ = sv.getQ();
        networkFlowP = networkFlow;
        networkFlowQ = Double.NaN;
    }

    private void dcApproximationDlDataNotSupportedCases(double networkFlow) {
        boundaryBusU = Double.NaN;
        boundaryBusTheta = Double.NaN;
        boundaryFlowP = Double.NaN;
        boundaryFlowQ = Double.NaN;
        networkFlowP = networkFlow;
        networkFlowQ = Double.NaN;
    }

    private static double getDoubleProperty(DanglingLine danglingLine, String name) {
        Objects.requireNonNull(danglingLine);
        String value = danglingLine.getProperty(name);
        return value != null ? Double.parseDouble(value) : Double.NaN;
    }

    private static double getV(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected() ? danglingLine.getTerminal().getBusView().getBus().getV()
            : Double.NaN;
    }

    private static double getTheta(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected()
            ? Math.toRadians(danglingLine.getTerminal().getBusView().getBus().getAngle())
            : Double.NaN;
    }

    private static boolean isVoltageValid(double v, double theta) {
        return !Double.isNaN(v) && v > 0.0 && !Double.isNaN(theta);
    }

    private static boolean isFlowValid(Complex flow) {
        return !Double.isNaN(flow.getReal()) && !Double.isNaN(flow.getImaginary());
    }

    private static boolean isDcVoltageValid(double theta) {
        return !Double.isNaN(theta);
    }

    private static boolean isDcFlowValid(double flow) {
        return !Double.isNaN(flow);
    }

    public static boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    public String getId() {
        return danglingLine.getId();
    }

    public double getBoundaryBusU() {
        return boundaryBusU;
    }

    public double getBoundaryBusTheta() {
        return boundaryBusTheta;
    }

    public double getBoundaryFlowP() {
        return boundaryFlowP;
    }

    public double getBoundaryFlowQ() {
        return boundaryFlowQ;
    }

    public double getNetworkFlowP() {
        return networkFlowP;
    }

    public double getNetworkFlowQ() {
        return networkFlowQ;
    }
}

