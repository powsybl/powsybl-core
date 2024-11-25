/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.iidm.network.*;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class HvdcConverter extends AbstractConverter {

    private final Map<DataObject, List<DataObject>> elmTermsConnectedToVscs = new HashMap<>();
    private final List<Configuration> configurations = new ArrayList<>();
    private final Set<DataObject> dcElmLnes = new HashSet<>();
    private final Set<DataObject> dcElmTerms = new HashSet<>();
    private final Set<DataObject> usedVscs = new HashSet<>();

    HvdcConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    boolean isDcLink(DataObject elmLne) {
        return dcElmLnes.contains(elmLne);
    }

    boolean isDcNode(DataObject elmTerm) {
        return dcElmTerms.contains(elmTerm);
    }

    void computeConfigurations(List<DataObject> elmTerms, List<DataObject> elmVscs) {
        if (elmVscs.isEmpty()) {
            return;
        }
        computeElmTermsConnectedToVscs(elmTerms, elmVscs);
        for (DataObject elmVsc : elmVscs) {
            if (!usedVscs.contains(elmVsc)) {
                computeConfiguration(elmVsc).ifPresent(this::addConfiguration);
            }
        }
    }

    private void computeElmTermsConnectedToVscs(List<DataObject> elmTerms, List<DataObject> elmVscs) {
        for (DataObject elmTerm : elmTerms) {
            getDataObjectsConnectedToElmTerm(elmTerm)
                    .filter(elmVscs::contains).findFirst()
                    .ifPresent(elmVsc -> elmTermsConnectedToVscs.computeIfAbsent(elmVsc, k -> new ArrayList<>()).add(elmTerm));
        }
    }

    private void addConfiguration(Configuration c) {
        configurations.add(c);
        dcElmLnes.addAll(c.getElmLnes());
        dcElmTerms.addAll(c.getElmTerms());
        usedVscs.add(c.vsc0);
        usedVscs.add(c.vsc1);
    }

    private static Stream<DataObject> getDataObjectsConnectedToElmTerm(DataObject elmTerm) {
        return elmTerm.getChildren().stream()
                .map(staCubic -> staCubic.findObjectAttributeValue(DataAttributeNames.OBJ_ID)
                        .flatMap(DataObjectRef::resolve))
                .flatMap(Optional::stream);
    }

    private Optional<Configuration> computeConfiguration(DataObject elmVsc0) {
        List<DCLink> links = computeDcLinksConnectedToVsc(elmVsc0);
        Optional<DataObject> elmVsc1 = findVsc1(links);
        if (elmVsc1.isPresent()) {
            List<DataObject> configurationDcElmTerms = links.stream().flatMap(l -> Stream.of(l.elmTerm0, l.elmTerm1)).collect(Collectors.toList());
            Optional<DataObject> elmTermAc0 = findAcElmTerm(elmVsc0, configurationDcElmTerms);
            if (elmTermAc0.isPresent()) {
                Optional<DataObject> elmTermAc1 = findAcElmTerm(elmVsc1.get(), configurationDcElmTerms);
                if (elmTermAc1.isPresent()) {
                    return Optional.of(new Configuration(elmTermAc0.get(), elmVsc0, links, elmTermAc1.get(), elmVsc1.get()));
                }
            }
        }
        return Optional.empty();
    }

    private List<DCLink> computeDcLinksConnectedToVsc(DataObject elmVsc) {
        List<DCLink> dcLinks = new ArrayList<>();
        for (DataObject elmTerm : elmTermsConnectedToVscs.get(elmVsc)) {

            List<DataObject> elmLnes = getDataObjectsConnectedToElmTerm(elmTerm)
                .filter(dataObject -> dataObject.getDataClassName().equals("ElmLne"))
                .toList();

            for (DataObject elmLne : elmLnes) {
                otherElmTerm(elmTerm, elmLne, elmVsc)
                        .ifPresent(dataObject -> dcLinks.add(new DCLink(elmTerm, elmLne, dataObject)));
            }
        }
        return dcLinks;
    }

    private Optional<DataObject> otherElmTerm(DataObject elmTerm, DataObject elmLne, DataObject elmVsc) {
        // Search only in elmTerms connected to other VSCs
        return elmTermsConnectedToVscs.entrySet().stream()
                .filter(e -> !Objects.equals(elmVsc, e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                // Recheck that elmTerm is different from the given one
            .filter(otherElmTerm -> !elmTerm.equals(otherElmTerm) && isElmTermConnectedToLne(otherElmTerm, elmLne))
            .findFirst();
    }

    private static boolean isElmTermConnectedToLne(DataObject elmTerm, DataObject elmLne) {
        return getDataObjectsConnectedToElmTerm(elmTerm).anyMatch(elmLne::equals);
    }

    private Optional<DataObject> findVsc1(List<DCLink> links) {
        if (!links.isEmpty()) {
            DataObject elmTerm1 = links.get(0).elmTerm1;
            Optional<DataObject> elmVsc1 = findTheOnlyOneDataObject(vscConnectedToElmTerm(elmTerm1));
            if (elmVsc1.isPresent()) {
                // Check other VSC is at end 1 for all the links in the HVDC configuration
                List<DataObject> elmTermsConnectedToOtherVsc = elmTermsConnectedToVscs.get(elmVsc1.get());
                if (links.stream().allMatch(l -> elmTermsConnectedToOtherVsc.contains(l.elmTerm1))) {
                    return elmVsc1;
                }
            }
        }
        return Optional.empty();
    }

    private static List<DataObject> vscConnectedToElmTerm(DataObject elmTerm) {
        return getDataObjectsConnectedToElmTerm(elmTerm)
            .filter(dataObject -> dataObject.getDataClassName().equals("ElmVsc"))
            .collect(Collectors.toList());
    }

    private Optional<DataObject> findAcElmTerm(DataObject elmVsc, List<DataObject> dcElmTerms) {
        List<DataObject> elmTermsAc = elmTermsConnectedToVscs.get(elmVsc).stream()
                .filter(elmTerm -> !dcElmTerms.contains(elmTerm))
                .toList();
        return findTheOnlyOneDataObject(elmTermsAc);
    }

    private static Optional<DataObject> findTheOnlyOneDataObject(List<DataObject> dataObjects) {
        if (dataObjects.isEmpty()) {
            return Optional.empty();
        } else if (dataObjects.size() > 1) {
            throw new PowerFactoryException("Unsupported Hvdc configuration");
        }
        return Optional.of(dataObjects.get(0));
    }

    private static final class Configuration {
        private final DataObject elmTermAc0;
        private final DataObject vsc0;
        private final List<DCLink> links;
        private final DataObject elmTermAc1;
        private final DataObject vsc1;

        private Configuration(DataObject elmTermAc0, DataObject vsc0, List<DCLink> links, DataObject elmTermAc1, DataObject vsc1) {
            this.elmTermAc0 = elmTermAc0;
            this.vsc0 = vsc0;
            this.links = links;
            this.elmTermAc1 = elmTermAc1;
            this.vsc1 = vsc1;
        }

        Collection<DataObject> getElmLnes() {
            return links.stream().map(l -> l.elmLne).collect(Collectors.toList());
        }

        Collection<DataObject> getElmTerms() {
            return links.stream()
                    .flatMap(l -> Stream.of(l.elmTerm0, l.elmTerm1))
                    .collect(Collectors.toList());
        }
    }

    private static final class DCLink {
        private final DataObject elmTerm0;
        private final DataObject elmLne;
        private final DataObject elmTerm1;

        private DCLink(DataObject elmTerm0, DataObject elmLne, DataObject elmTerm1) {
            this.elmTerm0 = elmTerm0;
            this.elmLne = elmLne;
            this.elmTerm1 = elmTerm1;

        }
    }

    void create() {
        configurations.forEach(this::create);
    }

    private void create(Configuration configuration) {

        VscModel vscModelR = VscModel.create(configuration.vsc0);
        DcLineModel dcLineModel = DcLineModel.create(configuration.links, configuration.vsc0);
        VscModel vscModelI = VscModel.create(configuration.vsc1);

        NodeRef nodeRefR = getNodeFromElmTerm(configuration.elmTermAc0);
        VoltageLevel voltageLevelR = getNetwork().getVoltageLevel(nodeRefR.voltageLevelId);

        // Always with internal connection
        int nodeR = voltageLevelR.getNodeBreakerView().getMaximumNodeIndex() + 1;
        createInternalConnection(voltageLevelR, nodeRefR.node, nodeR);

        VscConverterStationAdder adderR = voltageLevelR.newVscConverterStation()
            .setEnsureIdUnicity(true)
            .setId(configuration.vsc0.getLocName())
            .setNode(nodeR)
            .setLossFactor((float) vscModelR.lossFactor)
            .setVoltageSetpoint(vscModelR.voltageSetpoint)
            .setReactivePowerSetpoint(vscModelR.reactivePowerSetpoint)
            .setVoltageRegulatorOn(vscModelR.voltageRegulatorOn);
        VscConverterStation cR = adderR.add();

        NodeRef nodeRefI = getNodeFromElmTerm(configuration.elmTermAc1);
        VoltageLevel voltageLevelI = getNetwork().getVoltageLevel(nodeRefI.voltageLevelId);

        // Always with internal connection
        int nodeI = voltageLevelI.getNodeBreakerView().getMaximumNodeIndex() + 1;
        createInternalConnection(voltageLevelI, nodeRefI.node, nodeI);

        VscConverterStationAdder adderI = voltageLevelI.newVscConverterStation()
            .setEnsureIdUnicity(true)
            .setId(configuration.vsc1.getLocName())
            .setNode(nodeI)
            .setLossFactor((float) vscModelI.lossFactor)
            .setVoltageSetpoint(vscModelI.voltageSetpoint)
            .setReactivePowerSetpoint(vscModelI.reactivePowerSetpoint)
            .setVoltageRegulatorOn(vscModelI.voltageRegulatorOn);
        VscConverterStation cI = adderI.add();

        HvdcLineAdder adder = getNetwork().newHvdcLine()
            .setId(configuration.links.stream().findFirst().orElseThrow().elmLne.getLocName())
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

        private static DcLineModel create(List<DCLink> dcLnes, DataObject elmVscRectifier) {

            double g = 0.0;
            for (DCLink hvdcLne : dcLnes) {
                DataObject typLne = hvdcLne.elmLne.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();
                double gline = obtainRLine(hvdcLne.elmLne, typLne);
                g += gline == 0.0 ? 0.0 : 1 / gline;
            }
            double r = g == 0.0 ? 0.0 : 1 / g;

            double maxP = elmVscRectifier.getFloatAttributeValue("P_max");
            double activePowerSetpoint = elmVscRectifier.getFloatAttributeValue("psetp");
            return new DcLineModel(r, obtainNominalV(dcLnes), activePowerSetpoint, maxP);
        }

        private static double obtainNominalV(List<DCLink> dcLnes) {
            DCLink hvdcLne = dcLnes.stream().findFirst().orElseThrow();
            Optional<Float> unom = hvdcLne.elmLne.findFloatAttributeValue("Unom");
            if (unom.isPresent()) {
                return unom.get();
            } else {
                DataObject typLne = hvdcLne.elmLne.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();
                return typLne.getFloatAttributeValue("uline");
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
