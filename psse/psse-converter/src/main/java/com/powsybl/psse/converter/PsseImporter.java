/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.psse.model.*;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.*;
import com.powsybl.psse.model.pf.io.PowerFlowDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
@AutoService(Importer.class)
public class PsseImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseImporter.class);

    private static final String FORMAT = "PSS/E";

    private static final String[] EXTENSIONS = {"raw", "RAW", "rawx", "RAWX"};

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("psse.import.ignore-base-voltage",
            ParameterType.BOOLEAN,
            "Ignore base voltage specified in the file",
            Boolean.FALSE);

    private static final String V_PROPERTY = "v";

    private static final String ANGLE_PROPERTY = "angle";

    private static String getBusId(int busNum) {
        return "B" + busNum;
    }

    private static Bus createBus(PsseBus psseBus, VoltageLevel voltageLevel) {
        String busId = getBusId(psseBus.getI());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(psseBus.getName())
                .add();
        bus.setV(psseBus.getVm() * voltageLevel.getNominalV())
                .setAngle(psseBus.getVa());

        return bus;
    }

    private static Substation createSubstation(Network network, String substationId) {
        Substation substation = network.getSubstation(substationId);
        if (substation == null) {
            substation = network.newSubstation()
                    .setId(substationId)
                    .add();
        }
        return substation;
    }

    private static VoltageLevel createVoltageLevel(PsseBus psseBus, PerUnitContext perUnitContext,
                                                   String voltageLevelId, Substation substation, Network network) {
        double nominalV = perUnitContext.isIgnoreBaseVoltage() || psseBus.getBaskv() == 0 ? 1 : psseBus.getBaskv();
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            voltageLevel = substation.newVoltageLevel()
                    .setId(voltageLevelId)
                    .setNominalV(nominalV)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .add();
        }
        return voltageLevel;
    }

    private static void createLoad(PsseLoad psseLoad, ContainersMapping containerMapping, Network network) {
        String busId = getBusId(psseLoad.getI());
        VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseLoad.getI()));
        Load load = voltageLevel.newLoad()
                .setId(busId + "-L" + psseLoad.getId())
                .setConnectableBus(busId)
                .setP0(psseLoad.getPl()) //TODO: take into account Ip, Yp when iidm static load will have exponential modelling
                .setQ0(psseLoad.getQl()) //TODO: take into account Iq, Yq when iidm static load will have exponential modelling
                .add();

        if (psseLoad.getStatus() == 1) {
            load.getTerminal().connect();
        }

    }

    private static void createShuntCompensator(PsseFixedShunt psseShunt, ContainersMapping containerMapping, Network network) {
        if (psseShunt.getBl() != 0) {
            String busId = getBusId(psseShunt.getI());
            VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseShunt.getI()));
            ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
                    .setId(busId + "-SH" + psseShunt.getId())
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setSectionCount(1);
            adder.newLinearModel()
                    .setBPerSection(psseShunt.getBl())//TODO: take into account gl
                    .setMaximumSectionCount(1)
                    .add();
            ShuntCompensator shunt = adder.add();

            if (psseShunt.getStatus() == 1) {
                shunt.getTerminal().connect();
            }

            if (psseShunt.getGl() != 0) {
                LOGGER.warn("Shunt Gl not supported ({})", psseShunt.getI());
            }
        } else {
            LOGGER.warn("Shunt ({}) has Bl = 0, not imported ", psseShunt.getI()); //TODO : allow import of shunts with Bl= 0 in iidm?
        }
    }

    private static void createSwitchedShunt(PsseSwitchedShunt psseSwShunt, PerUnitContext perUnitContext, ContainersMapping containerMapping, Network network, Map<PsseSwitchedShunt, ShuntBlockTab> stoBlockiTab) {
        String busId = getBusId(psseSwShunt.getI());
        VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseSwShunt.getI()));
        ShuntBlockTab sbl = stoBlockiTab.get(psseSwShunt);

        for (int i = 1; i <= sbl.getSize(); i++) {
            if (psseSwShunt.getBinit() != 0) { //TODO : improve it to make it robust to all configurations
                ShuntCompensator shunt = voltageLevel.newShuntCompensator()
                        .setId(busId + "-SwSH-B" + i)
                        .setConnectableBus(busId)
                        .setSectionCount(1)
                        .newLinearModel() //TODO: use Binit and sbl.getNi(i) to initiate Bi, for now we use Binit to obtain de same load-flow results
                        .setBPerSection(psseSwShunt.getBinit())//TODO: take into account BINIT to define the number of switched steps in the case BINIT is different from the max switched steps
                        .setMaximumSectionCount(1)
                        .add()
                        .add();

                if (psseSwShunt.getStat() == 1) {
                    shunt.getTerminal().connect();
                }
            }
        }
    }

    private static void createGenerator(PsseGenerator psseGen, PsseBus psseBus, ContainersMapping containerMapping, Network network) {
        String busId = getBusId(psseGen.getI());
        VoltageLevel voltageLevel = network.getVoltageLevel(containerMapping.getVoltageLevelId(psseGen.getI()));
        Generator generator = voltageLevel.newGenerator()
                .setId(busId + "-G" + psseGen.getId())
                .setConnectableBus(busId)
                .setTargetP(psseGen.getPg())
                .setMaxP(psseGen.getPt())
                .setMinP(psseGen.getPb())
                .setVoltageRegulatorOn(false)
                .setTargetQ(psseGen.getQt())
                .add();

        generator.newMinMaxReactiveLimits()
                .setMinQ(psseGen.getQb())
                .setMaxQ(psseGen.getQt())
                .add();

        if (psseBus.getIde() != 3) {
            // The "if" added to be compliant with the IEEE 24 case where type 3 bus is regulating out of its Qmin Qmax
            // Assuming this is true in general for all PSSE cases for type 3 buses
            generator.newMinMaxReactiveLimits()
                    .setMinQ(psseGen.getQb())
                    .setMaxQ(psseGen.getQt())
                    .add();
        }

        if (psseGen.getStat() == 1) {
            generator.getTerminal().connect();
        }

        if (psseGen.getVs() > 0 && ((psseGen.getQt() - psseGen.getQb()) > 0.002 || psseBus.getIde() == 3)) {
            if (psseGen.getIreg() == 0) {
                //PV group
                generator.setTargetV(psseGen.getVs() * voltageLevel.getNominalV());
                generator.setVoltageRegulatorOn(true);
                generator.setTargetQ(psseGen.getQg());
            } else {
                //TODO : implement remote voltage control regulation
                LOGGER.warn("Remote Voltage control not supported ({})", generator.getId());
            }
        }
        //TODO: take into account zr zx Mbase...
    }

    private static void createBuses(PssePowerFlowModel psseModel, ContainersMapping containerMapping, PerUnitContext perUnitContext,
                                    Network network, Map<Integer, PsseBus> busNumToPsseBus) {
        for (PsseBus psseBus : psseModel.getBuses()) {
            String voltageLevelId = containerMapping.getVoltageLevelId(psseBus.getI());
            String substationId = containerMapping.getSubstationId(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(psseBus, perUnitContext, voltageLevelId, substation, network);

            // create bus
            createBus(psseBus, voltageLevel);

            busNumToPsseBus.put(psseBus.getI(), psseBus);
        }
    }

    private static void createLine(PsseNonTransformerBranch psseLine, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        String id = "L-" + psseLine.getI() + "-" + psseLine.getJ() + "-" + psseLine.getCkt();

        String bus1Id = getBusId(psseLine.getI());
        String bus2Id = getBusId(psseLine.getJ());
        String voltageLevel1Id = containerMapping.getVoltageLevelId(psseLine.getI());
        String voltageLevel2Id = containerMapping.getVoltageLevelId(psseLine.getJ());
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
        double zb = voltageLevel2.getNominalV() * voltageLevel2.getNominalV() / perUnitContext.getSb();

        Line line = network.newLine()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevel1Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevel2Id)
                .setR(psseLine.getR() * zb)
                .setX(psseLine.getX() * zb)
                .setG1(psseLine.getGi() / zb)
                .setB1(psseLine.getB() / zb / 2 + psseLine.getBi() / zb)
                .setG2(psseLine.getGj() / zb)
                .setB2(psseLine.getB() / zb / 2 + psseLine.getBj() / zb)
                .add();

        if (psseLine.getSt() == 1) {
            line.getTerminal1().connect();
            line.getTerminal2().connect();
        }

        if (psseLine.getGi() != 0 || psseLine.getGj() != 0) {
            LOGGER.warn("Branch G not supported ({})", psseLine.getI());
        }
    }

    private static void createTransformer(PsseTransformer psseTfo, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network, Map<Integer, PsseBus> busNumToPsseBus, double sbase) {

        String id = "T-" + psseTfo.getI() + "-" + psseTfo.getJ();
        if (psseTfo.getK() == 0) {
            id = id + "-" + psseTfo.getCkt();
        } else {
            id = id + "-" + psseTfo.getK() + "-" + psseTfo.getCkt();
        }

        String bus1Id = getBusId(psseTfo.getI());
        String bus2Id = getBusId(psseTfo.getJ());
        String voltageLevel1Id = containerMapping.getVoltageLevelId(psseTfo.getI());
        String voltageLevel2Id = containerMapping.getVoltageLevelId(psseTfo.getJ());
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevel1Id);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevel2Id);
        double baskv1 = busNumToPsseBus.get(psseTfo.getI()).getBaskv();
        double baskv2 = busNumToPsseBus.get(psseTfo.getJ()).getBaskv();
        double zb2 = voltageLevel2.getNominalV() * voltageLevel2.getNominalV() / perUnitContext.getSb();
        double sbase12 = psseTfo.getSbase12();
        double nomV1 = psseTfo.getWinding1().getNomv();

        //handling impedance and admittance
        // CZ = 1 the triangle values are already in right pu
        double r12 = psseTfo.getR12();
        double x12 = psseTfo.getX12();

        if (psseTfo.getCz() == 2) {
            //CZ = 2 change to right Sbase pu
            r12 = r12 * sbase / sbase12;
            x12 = x12 * sbase / sbase12;
        } else if (psseTfo.getCz() == 3) {
            //CZ = 3 convert load loss power and current into pu impedances
            r12 = r12 * sbase / (sbase12 * sbase12 * 1000000);
            double absZ12 = x12 * sbase / sbase12;
            x12 = Math.sqrt(absZ12 * absZ12 - r12 * r12);
        }

        // Handling terminal ratios
        //default value when Cw = 1
        double w1 = psseTfo.getWinding1().getWindv();
        double w2 = psseTfo.getWinding2().getWindv();
        if (psseTfo.getCw() == 2) {
            // case where Cw = 2
            w1 = w1 / baskv1;
            w2 = w2 / baskv2;
        }

        // Handling magnetizing admittance Gm and Bm
        // Case where Cm = 1
        double mag1 = psseTfo.getMag1(); // admittance value when Cm = 1
        double mag2 = psseTfo.getMag2(); // admittance value when Cm = 1
        double bmPu = mag2; //bmPu and gmPu represent the values of the magnetizing admittance at the i end in pu at 1/Zb1 base where Zb1 = Vb1*Vb1/Sb1
        double gmPu = mag1; //Vb1 is the bus i voltage base  (BASKV) and Sb1 is the system MVA base which is SBASE
        double ymPu = 0;
        if (psseTfo.getCm() == 2) {
            // modification of value if Cm = 2
            gmPu = mag1 / (nomV1 * nomV1 * 1000000) * (baskv1 * baskv1 / sbase); // we need to convert mag1 and mag2 from a (NOMV1, Sbase12) to a (baskv1, Sbase) base so that it is expressed in pu admittance at i end.
            ymPu = mag2 / (nomV1 * nomV1) * sbase12 * (baskv1 * baskv1 / sbase);
            double bm2 = ymPu * ymPu - gmPu * gmPu;
            if (bm2 >= 0) {
                bmPu = -Math.sqrt(bm2);
            } else {
                bmPu = 0;
                LOGGER.warn("Magnetizing susceptance of Transformer ({}) set to 0 because admittance module is ({}) and conductance is ({})  ", id, ymPu, gmPu);
            }
        }

        if (psseTfo.getK() == 0) {
            // Case of a 2 windings Transformer
            TwoWindingsTransformer tfo2W = voltageLevel2.getSubstation().newTwoWindingsTransformer()
                    .setId(id)
                    .setEnsureIdUnicity(true)
                    .setConnectableBus1(bus1Id)
                    .setVoltageLevel1(voltageLevel1Id)
                    .setConnectableBus2(bus2Id)
                    .setVoltageLevel2(voltageLevel2Id)
                    .setRatedU1(voltageLevel1.getNominalV() * w1)
                    .setRatedU2(voltageLevel2.getNominalV() * w2)
                    .setR(r12 * zb2 * w2 * w2) // R12 and X12 shifted on the other side of the 2 wire (PSSE model to iidm model)
                    .setX(x12 * zb2 * w2 * w2)
                    .setG(gmPu / (zb2 * (w2 / w1) * (w2 / w1))) // magnetizing susceptance and conductance shifted from left of the first wire (PSSE model) to the right of the second wire (iidm model)
                    .setB(bmPu / (zb2 * (w2 / w1) * (w2 / w1)))
                    .add();

            //Phase Shift Transformer
            if (psseTfo.getWinding1().getAng() != 0) {
                PhaseTapChangerAdder phaseTapChangerAdder = tfo2W.newPhaseTapChanger()
                        .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                        .setRegulating(false)
                        .setTapPosition(0);
                List<Double> alphas = new ArrayList<>();
                alphas.add(-psseTfo.getWinding1().getAng());  //TODO : check angle and angle units (supposed in degrees)
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
            if (psseTfo.getWinding2().getAng() != 0) {
                LOGGER.warn("Phase shift of Transformer ({}) located on end 2 not yet supported  ", id);
            }
            if (psseTfo.getK() != 0 && psseTfo.getWinding3().getAng() != 0) {
                LOGGER.warn("Phase shift of Transformer ({}) located on end 3 not yet supported  ", id);
            }

            if (psseTfo.getStat() == 1) {
                tfo2W.getTerminal1().connect();
                tfo2W.getTerminal2().connect();
            }

        } else {
            // case of a three windings transformer
            String bus3Id = getBusId(psseTfo.getK());
            String voltageLevel3Id = containerMapping.getVoltageLevelId(psseTfo.getK());
            VoltageLevel voltageLevel3 = network.getVoltageLevel(voltageLevel3Id);
            double baskv3 = busNumToPsseBus.get(psseTfo.getK()).getBaskv();

            // Cw = 1
            double w3 = psseTfo.getWinding3().getWindv();
            if (psseTfo.getCw() == 2) {
                // Cw = 2 : conversion of kV into ratio
                w3 = w3 / baskv3;
            }

            double sbase31 = psseTfo.getSbase31();
            double sbase23 = psseTfo.getSbase23();

            //Get the triangle impedances (rij,xij) values in all Cz configurations
            // CZ = 1 the triangle values are already in right pu
            double r23 = psseTfo.getR23();
            double x23 = psseTfo.getX23();
            double r31 = psseTfo.getR31();
            double x31 = psseTfo.getX31();
            if (psseTfo.getCz() == 2) {
                //CZ = 2 change to right Sbase pu
                r12 = r12 * sbase / sbase12;
                x12 = x12 * sbase / sbase12;
                r23 = r23 * sbase / sbase23;
                x23 = x23 * sbase / sbase23;
                r31 = r31 * sbase / sbase31;
                x31 = x31 * sbase / sbase31;
            } else if (psseTfo.getCz() == 3) {
                //CZ = 3 convert load loss power and current into pu impedances
                r23 = r23 * sbase / (sbase23 * sbase23 * 1000000);
                r31 = r31 * sbase / (sbase23 * sbase23 * 1000000);

                double absZ23 = x23 * sbase / sbase23;
                if (absZ23 * absZ23 - r23 * r23 <= 0) {
                    x23 = 0;
                    LOGGER.warn("inductance x23 of Transformer ({}) set to 0 because impedance module is ({}) and resistance is ({})  ", id, absZ23, r23);
                } else {
                    x23 = Math.sqrt(absZ23 * absZ23 - r23 * r23);
                }

                double absZ31 = x31 * sbase / sbase31;
                if (absZ23 * absZ23 - r23 * r23 <= 0) {
                    x31 = 0;
                    LOGGER.warn("inductance x31 of Transformer ({}) set to 0 because impedance module is ({}) and resistance is ({})  ", id, absZ31, r31);
                } else {
                    x31 = Math.sqrt(absZ31 * absZ31 - r31 * r31);
                }
            }

            //transform triangle (rij,xij) impedances into star (ri,xj) impedances
            double sumR = r12 + r23 + r31;
            double sumX = x12 + x23 + x31;
            double squareMod = sumR * sumR + sumX * sumX;

            double r1 = ((r31 * r12 - x31 * x12) * sumR + (r31 * x12 + r12 * x31) * sumX) / squareMod;
            double x1 = ((r31 * x12 + r12 * x31) * sumR - (r31 * r12 - x31 * x12) * sumX) / squareMod;

            double r2 = ((r12 * r23 - x12 * x23) * sumR + (r12 * x23 + r23 * x12) * sumX) / squareMod;
            double x2 = ((r12 * x23 + r23 * x12) * sumR - (r12 * r23 - x12 * x23) * sumX) / squareMod;

            double r3 = ((r23 * r31 - x23 * x31) * sumR + (r23 * x31 + r31 * x23) * sumX) / squareMod;
            double x3 = ((r23 * x31 + r31 * x23) * sumR - (r23 * r31 - x23 * x31) * sumX) / squareMod;

            //set a voltage base at star node with the associated Zbase
            double v0 = 1.0;
            double zbV0 = v0 * v0 / perUnitContext.getSb();

            ThreeWindingsTransformer tfo3W = voltageLevel1.getSubstation().newThreeWindingsTransformer()
                    .setRatedU0(v0)
                    .setEnsureIdUnicity(true)
                    .setId(id)
                    .newLeg1()
                        .setR(r1 * zbV0)
                        .setX(x1 * zbV0)
                        .setG(gmPu * w1 * w1 / zbV0)
                        .setB(bmPu * w1 * w1 / zbV0)
                        .setRatedU(voltageLevel1.getNominalV() * w1)
                        .setConnectableBus(bus1Id)
                        .setVoltageLevel(voltageLevel1Id)
                    .add()
                    .newLeg2()
                        .setR(r2 * zbV0)
                        .setX(x2 * zbV0)
                        .setG(0)
                        .setB(0)
                        .setRatedU(voltageLevel2.getNominalV() * w2)
                        .setConnectableBus(bus2Id)
                        .setVoltageLevel(voltageLevel2Id)
                    .add()
                    .newLeg3()
                        .setR(r3 * zbV0)
                        .setX(x3 * zbV0)
                        .setG(0)
                        .setB(0)
                        .setRatedU(voltageLevel3.getNominalV() * w3)
                        .setConnectableBus(bus3Id)
                        .setVoltageLevel(voltageLevel3Id)
                    .add()
                    .add();

            if (psseTfo.getStat() == 1) {
                tfo3W.getLeg1().getTerminal().connect();
                tfo3W.getLeg2().getTerminal().connect();
                tfo3W.getLeg3().getTerminal().connect();
            }

            //set the init value at the star point
            tfo3W.setProperty(V_PROPERTY, Float.toString((float) psseTfo.getVmstar())); //TODO: check the right base to put the voltage module
            tfo3W.setProperty(ANGLE_PROPERTY, Float.toString((float) psseTfo.getAnstar()));

        }
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(IGNORE_BASE_VOLTAGE_PARAMETER);
    }

    @Override
    public String getComment() {
        return "PSS/E Format to IIDM converter";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource);
            return exists(dataSource, ext);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new PsseException(String.format("No Power Flow Data file found. Basename: %s, supported extensions: %s",
                        dataSource.getBaseName(),
                        String.join("|", EXTENSIONS)));
            }
            PsseVersion version = PowerFlowDataFactory.create(ext).readVersion(dataSource, ext);
            Context context = new Context();
            PssePowerFlowModel pssePowerFlowModel = PowerFlowDataFactory.create(ext, version).read(dataSource, ext, context);
            pssePowerFlowModel.getCaseIdentification().validate();

            Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);
            // TODO store the PsseContext with the Network to be able to export back using its information
            convert(pssePowerFlowModel, network, parameters);
            return network;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String findExtension(ReadOnlyDataSource dataSource) throws IOException {
        for (String ext : EXTENSIONS) {
            if (dataSource.exists(null, ext)) {
                return ext;
            }
        }
        return null;
    }

    private boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        if (ext != null) {
            try {
                return PowerFlowDataFactory.create(ext).isValidFile(dataSource, ext);
            } catch (PsseException | IOException e) {
                LOGGER.error(String.format("Invalid content in filename %s.%s: %s",
                        dataSource.getBaseName(),
                        ext,
                        e.getMessage()));
            }
        }
        return false;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            String ext = findExtension(fromDataSource);
            if (!exists(fromDataSource, ext)) {
                throw new PowsyblException("From data source is not importable");
            }
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null, ext, false)) {
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createSwitchedShuntBlockMap(PssePowerFlowModel psseModel, Map<PsseSwitchedShunt, ShuntBlockTab> stoBlockiTab) {

        /* Creates a map between the PSSE switched shunt and the blocks info of this shunt
        A switched shunt may contain up to 8 blocks and each block may contain up to 9 steps of the same value (in MVAR)
        A block may be capacitive or inductive */
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            ShuntBlockTab sbt = new ShuntBlockTab();

            int[] ni = {
                    psseSwShunt.getN1(), psseSwShunt.getN2(), psseSwShunt.getN3(), psseSwShunt.getN4(),
                    psseSwShunt.getN5(), psseSwShunt.getN6(), psseSwShunt.getN7(), psseSwShunt.getN8()
            };

            double[] bi = {
                    psseSwShunt.getB1(), psseSwShunt.getB2(), psseSwShunt.getB3(), psseSwShunt.getB4(),
                    psseSwShunt.getB5(), psseSwShunt.getB6(), psseSwShunt.getB7(), psseSwShunt.getB8()
            };

            int i = 0;
            while (i <= 7 && ni[i] > 0) {
                sbt.add(i + 1, ni[i], bi[i]);
                i++;
            }

            stoBlockiTab.put(psseSwShunt, sbt);
        }
    }

    private Network convert(PssePowerFlowModel psseModel, Network network, Properties parameters) {
        // set date and time
        // TODO

        // build container to fit IIDM requirements
        List<Object> branches = ImmutableList.builder()
                .addAll(psseModel.getNonTransformerBranches())
                .addAll(psseModel.getTransformers())
                .build();
        ToIntFunction<Object> branchToNum1 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getI() : ((PsseTransformer) branch).getI();
        ToIntFunction<Object> branchToNum2 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getJ() : ((PsseTransformer) branch).getJ();
        ToIntFunction<Object> branchToNum3 = branch -> branch instanceof PsseNonTransformerBranch ? 0 : ((PsseTransformer) branch).getK();
        ToDoubleFunction<Object> branchToResistance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getR() : ((PsseTransformer) branch).getR12();
        ToDoubleFunction<Object> branchToReactance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getX() : ((PsseTransformer) branch).getX12();
        Predicate<Object> branchToIsTransformer = branch -> branch instanceof PsseTransformer;
        ContainersMapping containerMapping = ContainersMapping.create(psseModel.getBuses(), branches, PsseBus::getI, branchToNum1,
            branchToNum2, branchToNum3, branchToResistance, branchToReactance, branchToIsTransformer,
            busNums -> "VL" + busNums.iterator().next(), substationNum -> "S" + substationNum++);

        boolean ignoreBaseVoltage = ConversionParameters.readBooleanParameter(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);
        PerUnitContext perUnitContext = new PerUnitContext(psseModel.getCaseIdentification().getSbase(), ignoreBaseVoltage);

        // The map gives access to PsseBus object with the int bus Number
        Map<Integer, PsseBus> busNumToPsseBus = new HashMap<>();

        // create buses
        createBuses(psseModel, containerMapping, perUnitContext, network, busNumToPsseBus);

        // Create loads
        for (PsseLoad psseLoad : psseModel.getLoads()) {
            createLoad(psseLoad, containerMapping, network);
        }

        // Create fixed shunts
        for (PsseFixedShunt psseShunt : psseModel.getFixedShunts()) {
            createShuntCompensator(psseShunt, containerMapping, network);
        }

        // Create switched shunts
        Map<PsseSwitchedShunt, ShuntBlockTab> stoBlockiTab = new HashMap<>();
        createSwitchedShuntBlockMap(psseModel, stoBlockiTab);
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            createSwitchedShunt(psseSwShunt, perUnitContext, containerMapping, network, stoBlockiTab);
        }

        for (PsseGenerator psseGen : psseModel.getGenerators()) {
            createGenerator(psseGen, busNumToPsseBus.get(psseGen.getI()), containerMapping, network);
        }

        for (PsseNonTransformerBranch psseLine : psseModel.getNonTransformerBranches()) {
            createLine(psseLine, containerMapping, perUnitContext, network);
        }

        for (PsseTransformer psseTfo : psseModel.getTransformers()) {
            createTransformer(psseTfo, containerMapping, perUnitContext, network, busNumToPsseBus, psseModel.getCaseIdentification().getSbase());
        }

        // Attach a slack bus
        for (PsseArea psseArea : psseModel.getAreas()) {
            if (psseArea.getIsw() != 0) {
                String busId = getBusId(psseArea.getIsw());
                Bus bus = network.getBusBreakerView().getBus(busId);
                SlackTerminal.attach(bus);
            }
        }

        return network;
    }

    private static final class PerUnitContext {

        private final double sb; // base apparent power

        private final boolean ignoreBaseVoltage;

        private PerUnitContext(double sb, boolean ignoreBaseVoltage) {
            this.sb = sb;
            this.ignoreBaseVoltage = ignoreBaseVoltage;
        }

        private double getSb() {
            return sb;
        }

        private boolean isIgnoreBaseVoltage() {
            return ignoreBaseVoltage;
        }
    }

    private static final class ShuntBlockTab {

        private final Map<Integer, Integer> ni = new HashMap<>();
        private final Map<Integer, Double> bi = new HashMap<>();

        private void add(int i, int nni, double bni) {
            ni.put(i, nni);
            bi.put(i, bni);
        }

        private int getNi(int i) {
            return ni.get(i);
        }

        private double getBi(int i) {
            return bi.get(i);
        }

        private int getSize() {
            return ni.size();
        }
    }
}
