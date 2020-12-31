/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.PerUnitContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseTransformer;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TransformerConverter extends AbstractConverter {

    private static final String V_PROPERTY = "v";
    private static final String ANGLE_PROPERTY = "angle";

    public TransformerConverter(PsseTransformer psseTransformer, ContainersMapping containersMapping,
        PerUnitContext perUnitContext, Network network, Map<Integer, PsseBus> busNumToPsseBus, double sbase) {
        super(containersMapping, network);
        this.psseTransformer = psseTransformer;
        this.busNumToPsseBus = busNumToPsseBus;
        this.sbase = sbase;
        this.perUnitContext = perUnitContext;
    }

    public void create() {
        if (psseTransformer.getK() == 0) {
            createTwoWindingsTransformer();
        } else {
            createThreeWindingsTransformer();
        }
    }

    private void createTwoWindingsTransformer() {

        String id = "T-" + psseTransformer.getI() + "-" + psseTransformer.getJ() + "-" + psseTransformer.getCkt();

        String bus1Id = getBusId(psseTransformer.getI());
        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseTransformer.getI());
        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevel1Id);
        double baskv1 = busNumToPsseBus.get(psseTransformer.getI()).getBaskv();

        String bus2Id = getBusId(psseTransformer.getJ());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseTransformer.getJ());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);
        double baskv2 = busNumToPsseBus.get(psseTransformer.getJ()).getBaskv();

        double zb2 = voltageLevel2.getNominalV() * voltageLevel2.getNominalV() / perUnitContext.getSb();
        double sbase12 = psseTransformer.getSbase12();
        double nomV1 = psseTransformer.getWinding1().getNomv();
        double nomV2 = psseTransformer.getWinding2().getNomv();

        Complex z = defineImpedanceBetweenWindings(psseTransformer.getR12(), psseTransformer.getX12(), sbase, sbase12, psseTransformer.getCz());

        // Handling terminal ratios
        double w1 = defineRatio(psseTransformer.getWinding1().getWindv(), baskv1, nomV1, psseTransformer.getCw());
        double w2 = defineRatio(psseTransformer.getWinding2().getWindv(), baskv2, nomV2, psseTransformer.getCw());

        // Handling magnetizing admittance Gm and Bm
        Complex ysh = defineShuntAdmittance(id, psseTransformer.getMag1(), psseTransformer.getMag2(), sbase, sbase12, baskv1, nomV1, psseTransformer.getCm());

        TwoWindingsTransformer tfo2W = voltageLevel2.getSubstation().newTwoWindingsTransformer()
            .setId(id)
            .setEnsureIdUnicity(true)
            .setConnectableBus1(bus1Id)
            .setVoltageLevel1(voltageLevel1Id)
            .setConnectableBus2(bus2Id)
            .setVoltageLevel2(voltageLevel2Id)
            .setRatedU1(voltageLevel1.getNominalV() * w1)
            .setRatedU2(voltageLevel2.getNominalV() * w2)
            .setR(z.getReal() * zb2 * w2 * w2) // R12 and X12 shifted on the other side of the 2 wire (PSSE model to iidm model)
            .setX(z.getImaginary() * zb2 * w2 * w2)
            .setG(ysh.getReal() / (zb2 * (w2 / w1) * (w2 / w1))) // magnetizing susceptance and conductance shifted from left of the first wire (PSSE model) to the right of the second wire (iidm model)
            .setB(ysh.getImaginary() / (zb2 * (w2 / w1) * (w2 / w1)))
            .add();

        // Phase Shift Transformer
        if (psseTransformer.getWinding1().getAng() != 0) {
            PhaseTapChangerAdder phaseTapChangerAdder = tfo2W.newPhaseTapChanger()
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulating(false)
                .setTapPosition(0);
            List<Double> alphas = new ArrayList<>();
            alphas.add(-psseTransformer.getWinding1().getAng()); // TODO : check angle and angle units (supposed
                                                                       // in degrees)
            // TODO create full table
            for (double alpha : alphas) {
                phaseTapChangerAdder.beginStep()
                    .setAlpha(alpha)
                    .setRho(1)
                    .setR(0)
                    .setX(0)
                    .setG(0)
                    .setB(0)
                    .endStep();
            }
            phaseTapChangerAdder.add();
        }

        //TODO support phase shift on all ends of the Tfo
        if (psseTransformer.getWinding2().getAng() != 0) {
            LOGGER.warn("Phase shift of Transformer ({}) located on end 2 not yet supported  ", id);
        }

        if (psseTransformer.getStat() == 1) {
            tfo2W.getTerminal1().connect();
            tfo2W.getTerminal2().connect();
        }
    }

    private void createThreeWindingsTransformer() {
        String id = "T-" + psseTransformer.getI() + "-" + psseTransformer.getJ() + "-" + psseTransformer.getK() + "-" + psseTransformer.getCkt();

        String bus1Id = getBusId(psseTransformer.getI());
        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseTransformer.getI());
        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevel1Id);
        double baskv1 = busNumToPsseBus.get(psseTransformer.getI()).getBaskv();

        String bus2Id = getBusId(psseTransformer.getJ());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseTransformer.getJ());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);
        double baskv2 = busNumToPsseBus.get(psseTransformer.getJ()).getBaskv();

        String bus3Id = getBusId(psseTransformer.getK());
        String voltageLevel3Id = getContainersMapping().getVoltageLevelId(psseTransformer.getK());
        VoltageLevel voltageLevel3 = getNetwork().getVoltageLevel(voltageLevel3Id);
        double baskv3 = busNumToPsseBus.get(psseTransformer.getK()).getBaskv();

        double sbase12 = psseTransformer.getSbase12();
        double sbase23 = psseTransformer.getSbase23();
        double sbase31 = psseTransformer.getSbase31();

        double nomV1 = psseTransformer.getWinding1().getNomv();
        double nomV2 = psseTransformer.getWinding2().getNomv();
        double nomV3 = psseTransformer.getWinding3().getNomv();

        Complex z12 = defineImpedanceBetweenWindings(psseTransformer.getR12(), psseTransformer.getX12(), sbase, sbase12, psseTransformer.getCz());
        Complex z23 = defineImpedanceBetweenWindings(psseTransformer.getR23(), psseTransformer.getX23(), sbase, sbase23, psseTransformer.getCz());
        Complex z31 = defineImpedanceBetweenWindings(psseTransformer.getR31(), psseTransformer.getX31(), sbase, sbase31, psseTransformer.getCz());

        // Transform impedances between windings to star
        Complex z1 = z12.add(z31).subtract(z23).multiply(0.5);
        Complex z2 = z12.add(z23).subtract(z31).multiply(0.5);
        Complex z3 = z23.add(z31).subtract(z12).multiply(0.5);

        // Handling terminal ratios
        double w1 = defineRatio(psseTransformer.getWinding1().getWindv(), baskv1, nomV1, psseTransformer.getCw());
        double w2 = defineRatio(psseTransformer.getWinding2().getWindv(), baskv2, nomV2, psseTransformer.getCw());
        double w3 = defineRatio(psseTransformer.getWinding3().getWindv(), baskv3, nomV3, psseTransformer.getCw());

        // Handling magnetizing admittance Gm and Bm
        Complex ysh = defineShuntAdmittance(id, psseTransformer.getMag1(), psseTransformer.getMag2(), sbase, sbase12, baskv1, nomV1, psseTransformer.getCm());

        //set a voltage base at star node with the associated Zbase
        double v0 = 1.0;
        double zbV0 = v0 * v0 / perUnitContext.getSb();

        ThreeWindingsTransformer tfo3W = voltageLevel1.getSubstation().newThreeWindingsTransformer()
            .setRatedU0(v0)
            .setEnsureIdUnicity(true)
            .setId(id)
            .newLeg1()
            .setR(z1.getReal() * zbV0)
            .setX(z1.getImaginary() * zbV0)
            .setG(ysh.getReal() * w1 * w1 / zbV0)
            .setB(ysh.getImaginary() * w1 * w1 / zbV0)
            .setRatedU(voltageLevel1.getNominalV() * w1)
            .setConnectableBus(bus1Id)
            .setVoltageLevel(voltageLevel1Id)
            .add()
            .newLeg2()
            .setR(z2.getReal() * zbV0)
            .setX(z2.getImaginary() * zbV0)
            .setG(0)
            .setB(0)
            .setRatedU(voltageLevel2.getNominalV() * w2)
            .setConnectableBus(bus2Id)
            .setVoltageLevel(voltageLevel2Id)
            .add()
            .newLeg3()
            .setR(z3.getReal() * zbV0)
            .setX(z3.getImaginary() * zbV0)
            .setG(0)
            .setB(0)
            .setRatedU(voltageLevel3.getNominalV() * w3)
            .setConnectableBus(bus3Id)
            .setVoltageLevel(voltageLevel3Id)
            .add()
            .add();

        if (psseTransformer.getStat() == 1) {
            tfo3W.getLeg1().getTerminal().connect();
            tfo3W.getLeg2().getTerminal().connect();
            tfo3W.getLeg3().getTerminal().connect();
        }

        // set the init value at the star point
        // TODO: check the right base to put the voltage module
        tfo3W.setProperty(V_PROPERTY, Float.toString((float) psseTransformer.getVmstar()));
        tfo3W.setProperty(ANGLE_PROPERTY, Float.toString((float) psseTransformer.getAnstar()));

        if (psseTransformer.getK() != 0 && psseTransformer.getWinding3().getAng() != 0) {
            LOGGER.warn("Phase shift of Transformer ({}) located on end 3 not yet supported  ", id);
        }
    }

    private static Complex defineImpedanceBetweenWindings(double r, double x, double sbase, double windingSbase, int cz) {
        double rw;
        double xw;
        switch (cz) {
            case 1:
                rw = r;
                xw = x;
                break;
            case 2:
                // change to right Sbase pu
                rw = r * sbase / windingSbase;
                xw = x * sbase / windingSbase;
                break;
            case 3:
                // convert load loss power and current into pu impedances
                rw = r / windingSbase / 1000000;
                xw = Math.sqrt(x * x - rw * rw);

                rw = rw * sbase / windingSbase;
                xw = xw * sbase / windingSbase;
                break;
            default:
                throw new PsseException("PSSE: Unexpected CZ = " + cz);
        }
        return new Complex(rw, xw);
    }

    private static Complex defineShuntAdmittance(String id, double magG, double magB, double sbase, double windingSbase,
        double baskv, double nomV, int cm) {
        double g;
        double b;
        switch (cm) {
            case 1:
                // g and b represent the values of the magnetizing admittance at the i end in pu at 1/Zb1 base where Zb1 = Vb1*Vb1/Sb1
                // Vb1 is the bus i voltage base (BASKV) and Sb1 is the system MVA base which is SBASE
                g = magG;
                b = magB;
                break;
            case 2:
                g = magG / (1000000 * sbase) * (baskv / nomV) * (baskv / nomV);
                double y = magB * (windingSbase / sbase) * (baskv / nomV) * (baskv / nomV);
                double b2 = y * y - g * g;
                if (b2 >= 0) {
                    b = -Math.sqrt(b2);
                } else {
                    b = 0.0;
                    LOGGER.warn("Magnetizing susceptance of Transformer ({}) set to 0 because admittance module is ({}) and conductance is ({})  ", id, y, g);
                }
                break;
            default:
                throw new PsseException("PSSE: Unexpected CM = " + cm);
        }
        return new Complex(g, b);
    }

    private static double defineRatio(double windV, double baskv, double nomV, int cw) {
        double ratio;
        switch (cw) {
            case 1:
                ratio = windV;
                break;
            case 2:
                ratio = windV / baskv;
                break;
            case 3:
                ratio = windV * nomV / baskv;
                break;
            default:
                throw new PsseException("PSSE: Unexpected CW = " + cw);
        }
        return ratio;
    }

    private final PsseTransformer psseTransformer;
    private final Map<Integer, PsseBus> busNumToPsseBus;
    private final double sbase;
    private final PerUnitContext perUnitContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerConverter.class);
}
