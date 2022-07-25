/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class HvdcConverter extends AbstractConverter {

    private final List<HvdcConfiguration> hvdcConfigurations = new ArrayList<>();

    HvdcConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    List<HvdcConfiguration> get() {
        return hvdcConfigurations;
    }

    boolean isDcLink(DataObject elmLne) {
        return hvdcConfigurations.stream().anyMatch(hvdcConfiguration -> isDcLink(hvdcConfiguration, elmLne));
    }

    private static boolean isDcLink(HvdcConfiguration hvdcConfiguration, DataObject elmLne) {
        return hvdcConfiguration.dcLnes.stream().anyMatch(hvdc -> hvdc.elmLne.equals(elmLne));
    }

    boolean isDcNode(DataObject elmTerm) {
        return hvdcConfigurations.stream().anyMatch(hvdcConfiguration -> isDcNode(hvdcConfiguration.dcLnes, elmTerm));
    }

    void createConfigurations(List<DataObject> elmTerms, List<DataObject> elmVscs) {
        if (elmVscs.isEmpty()) {
            return;
        }
        List<DataObject> elmTermsConnectedToAnyVsc = findElmTermsConnectedToAnyVsc(elmTerms, elmVscs);
        for (DataObject elmVsc : elmVscs) {
            if (isUsedInPreviousConfiguration(elmVsc, hvdcConfigurations)) {
                continue;
            }
            Optional<HvdcConfiguration> hvdcConfiguration = createHvdcConfiguration(elmVsc, elmTermsConnectedToAnyVsc);
            if (hvdcConfiguration.isPresent()) {
                hvdcConfigurations.add(hvdcConfiguration.get());
            }
        }
    }

    private static List<DataObject> findElmTermsConnectedToAnyVsc(List<DataObject> elmTerms, List<DataObject> elmVscs) {
        return elmTerms.stream().filter(elmTerm -> isElmTermConnectedToAnyVsc(elmTerm, elmVscs)).collect(Collectors.toList());
    }

    private static List<DataObject> findElmTermsConnectedToVsc(List<DataObject> elmTerms, DataObject elmVsc) {
        return elmTerms.stream().filter(elmTerm -> isElmTermConnectedToVsc(elmTerm, elmVsc)).collect(Collectors.toList());
    }

    private static boolean isElmTermConnectedToVsc(DataObject elmTerm, DataObject elmVsc) {
        return isElmTermConnectedToAnyVsc(elmTerm, Collections.singletonList(elmVsc));
    }

    private static boolean isElmTermConnectedToAnyVsc(DataObject elmTerm, List<DataObject> elmVscs) {
        return getDataObjectsConnectedToElmTerm(elmTerm).stream().anyMatch(elmVscs::contains);
    }

    private static List<DataObject> getDataObjectsConnectedToElmTerm(DataObject elmTerm) {
        return elmTerm.getChildren().stream()
            .map(staCubic -> staCubic.findObjectAttributeValue(DataAttributeNames.OBJ_ID).flatMap(DataObjectRef::resolve))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private static boolean isUsedInPreviousConfiguration(DataObject elmVsc, List<HvdcConfiguration> hvdcConfigurations) {
        return hvdcConfigurations.stream().anyMatch(hvdcConfiguration -> hvdcConfiguration.vsc0.equals(elmVsc)
            || hvdcConfiguration.vsc1.equals(elmVsc));
    }

    private static Optional<HvdcConfiguration> createHvdcConfiguration(DataObject elmVsc, List<DataObject> elmTermsConnectedToAnyVsc) {
        List<HvdcLne> hvdcLnes = hvdcLinksConnectedToVsc(elmTermsConnectedToAnyVsc, elmVsc);
        Optional<DataObject> otherVsc = otherVsc(elmTermsConnectedToAnyVsc, hvdcLnes);

        if (otherVsc.isEmpty()) {
            return Optional.empty();
        }

        Optional<DataObject> elemTermAc0 = findElemTermAc(elmVsc, elmTermsConnectedToAnyVsc, hvdcLnes);
        Optional<DataObject> elemTermAc1 = findElemTermAc(otherVsc.get(), elmTermsConnectedToAnyVsc, hvdcLnes);

        if (elemTermAc0.isEmpty() || elemTermAc1.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new HvdcConfiguration(elemTermAc0.get(), elmVsc, hvdcLnes, elemTermAc1.get(), otherVsc.get()));
    }

    private static List<HvdcLne> hvdcLinksConnectedToVsc(List<DataObject> elmTermsConnectedToAnyVsc, DataObject elmVsc) {

        List<DataObject> elmTermsConnectedToVsc = findElmTermsConnectedToVsc(elmTermsConnectedToAnyVsc, elmVsc);

        List<HvdcLne> hvdcLnes = new ArrayList<>();
        for (DataObject elmTerm : elmTermsConnectedToVsc) {

            List<DataObject> elmLnes = getDataObjectsConnectedToElmTerm(elmTerm).stream()
                .filter(dataObject -> dataObject.getDataClassName().equals("ElmLne"))
                .collect(Collectors.toList());

            for (DataObject elmLne : elmLnes) {
                Optional<DataObject> ohterElmTerm = otherElmTerm(elmTermsConnectedToAnyVsc, elmTerm, elmLne);
                if (ohterElmTerm.isPresent()) {
                    hvdcLnes.add(new HvdcLne(elmTerm, elmLne, ohterElmTerm.get()));
                }
            }
        }
        return hvdcLnes;
    }

    private static Optional<DataObject> otherElmTerm(List<DataObject> elmTermsConnectedToAnyVsc, DataObject elmTerm, DataObject elmLne) {
        return elmTermsConnectedToAnyVsc.stream()
            .filter(otherElmTerm -> !elmTerm.equals(otherElmTerm) && isElmTermConnectedToLne(otherElmTerm, elmLne))
            .findFirst();
    }

    private static boolean isElmTermConnectedToLne(DataObject elmTerm, DataObject elmLne) {
        return getDataObjectsConnectedToElmTerm(elmTerm).stream().anyMatch(elmLne::equals);
    }

    private static Optional<DataObject> otherVsc(List<DataObject> elmTermsConnectedToAnyVsc, List<HvdcLne> hvdcLnes) {
        if (hvdcLnes.isEmpty()) {
            return Optional.empty();
        }
        DataObject elmTerm1 = hvdcLnes.get(0).elmTerm1;
        List<DataObject> vscConnecteds = vscConnectedToElmTerm(elmTerm1);

        Optional<DataObject> otherVsc = findTheOnlyOneDataObject(vscConnecteds);
        if (otherVsc.isPresent()) {
            List<DataObject> elmTermsConnectedToVsc = findElmTermsConnectedToVsc(elmTermsConnectedToAnyVsc, otherVsc.get());

            return isEnd1ConnectedToVscForAllHvdcLnes(hvdcLnes, elmTermsConnectedToVsc) ? otherVsc : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static List<DataObject> vscConnectedToElmTerm(DataObject elmTerm) {
        return getDataObjectsConnectedToElmTerm(elmTerm).stream()
            .filter(dataObject -> dataObject.getDataClassName().equals("ElmVsc"))
            .collect(Collectors.toList());
    }

    private static boolean isEnd1ConnectedToVscForAllHvdcLnes(List<HvdcLne> hvdcLnes, List<DataObject> elmTermsConnectedToVsc) {
        return hvdcLnes.stream().allMatch(hvdcLne -> elmTermsConnectedToVsc.contains(hvdcLne.elmTerm1));
    }

    private static Optional<DataObject> findElemTermAc(DataObject elmVsc, List<DataObject> elmTermsConnectedToAnyVsc, List<HvdcLne> hvdcLnes) {
        List<DataObject> elmTermsAc = elmTermsConnectedToAnyVsc.stream()
            .filter(elmTerm -> isElmTermConnectedToVsc(elmTerm, elmVsc))
            .filter(elemTerm -> !isDcNode(hvdcLnes, elemTerm))
            .collect(Collectors.toList());

        return findTheOnlyOneDataObject(elmTermsAc);
    }

    private static boolean isDcNode(List<HvdcLne> hvdcLnes, DataObject elmTerm) {
        return hvdcLnes.stream().anyMatch(hvdcLne -> hvdcLne.elmTerm0.equals(elmTerm) || hvdcLne.elmTerm1.equals(elmTerm));
    }

    private static Optional<DataObject> findTheOnlyOneDataObject(List<DataObject> dataObjects) {
        if (dataObjects.isEmpty()) {
            return Optional.empty();
        } else if (dataObjects.size() > 1) {
            throw new PowerFactoryException("Unsupported Hvdc configuration");
        }
        return Optional.of(dataObjects.get(0));
    }

    private static final class HvdcConfiguration {
        private final DataObject elmTermAc0;
        private final DataObject vsc0;
        private final List<HvdcLne> dcLnes;
        private final DataObject elmTermAc1;
        private final DataObject vsc1;

        private HvdcConfiguration(DataObject elmTermAc0, DataObject vsc0, List<HvdcLne> dcLnes, DataObject elmTermAc1, DataObject vsc1) {
            this.elmTermAc0 = elmTermAc0;
            this.vsc0 = vsc0;
            this.dcLnes = dcLnes;
            this.elmTermAc1 = elmTermAc1;
            this.vsc1 = vsc1;
        }
    }

    private static final class HvdcLne {
        private final DataObject elmTerm0;
        private final DataObject elmLne;
        private final DataObject elmTerm1;

        private HvdcLne(DataObject elmTerm0, DataObject elmLne, DataObject elmTerm1) {
            this.elmTerm0 = elmTerm0;
            this.elmLne = elmLne;
            this.elmTerm1 = elmTerm1;
        }
    }

    void create() {
        hvdcConfigurations.forEach(this::create);
    }

    private void create(HvdcConfiguration hvdcConfiguration) {

        VscModel vscModelR = VscModel.create(hvdcConfiguration.vsc0);
        DcLineModel dcLineModel = DcLineModel.create(hvdcConfiguration.dcLnes, hvdcConfiguration.vsc0);
        VscModel vscModelI = VscModel.create(hvdcConfiguration.vsc1);

        NodeRef nodeRefR = getNodeFromElmTerm(hvdcConfiguration.elmTermAc0);
        VoltageLevel voltageLevelR = getNetwork().getVoltageLevel(nodeRefR.voltageLevelId);

        // Always with internal connection
        int nodeR = voltageLevelR.getNodeBreakerView().getMaximumNodeIndex() + 1;
        createInternalConnection(voltageLevelR, nodeRefR.node, nodeR);

        VscConverterStationAdder adderR = voltageLevelR.newVscConverterStation()
            .setEnsureIdUnicity(true)
            .setId(hvdcConfiguration.vsc0.getLocName())
            .setNode(nodeR)
            .setLossFactor((float) vscModelR.lossFactor)
            .setVoltageSetpoint(vscModelR.voltageSetpoint)
            .setReactivePowerSetpoint(vscModelR.reactivePowerSetpoint)
            .setVoltageRegulatorOn(vscModelR.voltageRegulatorOn);
        VscConverterStation cR = adderR.add();

        NodeRef nodeRefI = getNodeFromElmTerm(hvdcConfiguration.elmTermAc1);
        VoltageLevel voltageLevelI = getNetwork().getVoltageLevel(nodeRefI.voltageLevelId);

        // Always with internal connection
        int nodeI = voltageLevelR.getNodeBreakerView().getMaximumNodeIndex() + 1;
        createInternalConnection(voltageLevelR, nodeRefI.node, nodeI);

        VscConverterStationAdder adderI = voltageLevelI.newVscConverterStation()
            .setEnsureIdUnicity(true)
            .setId(hvdcConfiguration.vsc1.getLocName())
            .setNode(nodeI)
            .setLossFactor((float) vscModelI.lossFactor)
            .setVoltageSetpoint(vscModelI.voltageSetpoint)
            .setReactivePowerSetpoint(vscModelI.reactivePowerSetpoint)
            .setVoltageRegulatorOn(vscModelI.voltageRegulatorOn);
        VscConverterStation cI = adderI.add();

        HvdcLineAdder adder = getNetwork().newHvdcLine()
            .setId(hvdcConfiguration.dcLnes.stream().findFirst().orElseThrow().elmLne.getLocName())
            .setR(dcLineModel.r)
            .setNominalV(dcLineModel.nominalV)
            .setActivePowerSetpoint(dcLineModel.activePowerSetpoint)
            .setMaxP(dcLineModel.maxP)
            .setConvertersMode(ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
            .setConverterStationId1(cR.getId())
            .setConverterStationId2(cI.getId());
        adder.add();
    }

    private static final class DcLineModel {
        private final double r;
        private final double nominalV;
        private final double activePowerSetpoint;
        private final double maxP;

        private DcLineModel(double r, double nominalV, double activePowerSetpoint, double maxP) {
            this.r = r;
            this.nominalV = nominalV;
            this.activePowerSetpoint = activePowerSetpoint;
            this.maxP = maxP;
        }

        private static DcLineModel create(List<HvdcLne> dcLnes, DataObject elmVscRectifier) {

            double g = 0.0;
            for (HvdcLne hvdcLne : dcLnes) {
                DataObject typLne = hvdcLne.elmLne.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();
                double gline = obtainRLine(hvdcLne.elmLne, typLne);
                g += gline == 0.0 ? 0.0 : 1 / gline;
            }
            double r = g == 0.0 ? 0.0 : 1 / g;

            double maxP = elmVscRectifier.getFloatAttributeValue("P_max");
            double activePowerSetpoint = elmVscRectifier.getFloatAttributeValue("psetp");
            return new DcLineModel(r, obtainNominalV(dcLnes), activePowerSetpoint, maxP);
        }

        private static double obtainNominalV(List<HvdcLne> dcLnes) {
            HvdcLne hvdcLne = dcLnes.stream().findFirst().orElseThrow();
            Optional<Float> unom = hvdcLne.elmLne.findFloatAttributeValue("Unom");
            if (unom.isPresent()) {
                return (double) unom.get();
            } else {
                DataObject typLne = hvdcLne.elmLne.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();
                return (double) typLne.getFloatAttributeValue("uline");
            }
        }

        private static double obtainRLine(DataObject elmLne, DataObject typLne) {
            float dline = elmLne.getFloatAttributeValue("dline");
            float rline = typLne.getFloatAttributeValue("rline");
            return rline * dline;
        }
    }

    private static final class VscModel {
        private final double lossFactor;
        private final double voltageSetpoint;
        private final double reactivePowerSetpoint;
        private final boolean voltageRegulatorOn;

        private VscModel(double lossFactor, double voltageSetpoint, double reactivePowerSetpoint, boolean voltageRegulatorOn) {
            this.lossFactor = lossFactor;
            this.voltageSetpoint = voltageSetpoint;
            this.reactivePowerSetpoint = reactivePowerSetpoint;
            this.voltageRegulatorOn = voltageRegulatorOn;
        }

        private static VscModel create(DataObject elmVsc) {
            double pnold = elmVsc.getFloatAttributeValue("Pnold");
            double unom = elmVsc.getFloatAttributeValue("Unom");
            double activeSetpoint = elmVsc.getFloatAttributeValue("psetp");
            double reactiveSetpoint = elmVsc.getFloatAttributeValue("qsetp");
            double voltageSetpoint = elmVsc.getFloatAttributeValue("usetp") * unom;

            double losses = pnold / 1000.0;
            double lossFactor = activeSetpoint != 0.0 ? losses / activeSetpoint * 100 : 0.0;
            return new VscModel(lossFactor, voltageSetpoint, reactiveSetpoint, false);
        }
    }
}
