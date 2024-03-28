/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationList;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class Contingency extends AbstractExtendable<Contingency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Contingency.class);

    private final String id;

    private final List<ContingencyElement> elements;

    private final String name;

    public Contingency(String id, String name, List<ContingencyElement> elements) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.elements = new ArrayList<>(Objects.requireNonNull(elements));
    }

    public Contingency(String id, String name, ContingencyElement... elements) {
        this(id, name, Arrays.asList(elements));
    }

    public Contingency(String id, List<ContingencyElement> elements) {
        this(id, null, elements);
    }

    public Contingency(String id, ContingencyElement... elements) {
        this(id, Arrays.asList(elements));
    }

    public String getId() {
        return id;
    }

    public List<ContingencyElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contingency other) {
            return id.equals(other.id) && Objects.equals(name, other.name) && elements.equals(other.elements);
        }
        return false;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public void addElement(ContingencyElement element) {
        Objects.requireNonNull(element);
        elements.add(element);
    }

    public void removeElement(ContingencyElement element) {
        Objects.requireNonNull(element);
        elements.remove(element);
    }

    public NetworkModification toModification() {
        return new NetworkModificationList(elements.stream().map(ContingencyElement::toModification).collect(Collectors.toList()));
    }

    public boolean isValid(Network network) {
        Objects.requireNonNull(network);
        boolean valid = true;
        for (ContingencyElement element : elements) {
            switch (element.getType()) {
                case GENERATOR:
                    valid = checkGeneratorContingency(this, (GeneratorContingency) element, network);
                    break;

                case STATIC_VAR_COMPENSATOR:
                    valid = checkStaticVarCompensatorContingency(this, (StaticVarCompensatorContingency) element, network);
                    break;

                case SHUNT_COMPENSATOR:
                    valid = checkShuntCompensatorContingency(this, (ShuntCompensatorContingency) element, network);
                    break;

                case BRANCH:
                    valid = checkBranchContingency(this, (BranchContingency) element, network);
                    break;

                case HVDC_LINE:
                    valid = checkHvdcLineContingency(this, (HvdcLineContingency) element, network);
                    break;

                case BUSBAR_SECTION:
                    valid = checkBusbarSectionContingency(this, (BusbarSectionContingency) element, network);
                    break;

                case DANGLING_LINE:
                    valid = checkDanglingLineContingency(this, (DanglingLineContingency) element, network);
                    break;

                case LINE:
                    valid = checkLineContingency(this, (LineContingency) element, network);
                    break;

                case TWO_WINDINGS_TRANSFORMER:
                    valid = checkTwoWindingsTransformerContingency(this, (TwoWindingsTransformerContingency) element, network);
                    break;

                case THREE_WINDINGS_TRANSFORMER:
                    valid = checkThreeWindingsTransformerContingency(this, (ThreeWindingsTransformerContingency) element, network);
                    break;

                case LOAD:
                    valid = checkLoadContingency(this, (LoadContingency) element, network);
                    break;

                case BUS:
                    valid = checkBusContingency(this, (BusContingency) element, network);
                    break;

                case TIE_LINE:
                    valid = checkTieLineContingency(this, (TieLineContingency) element, network);
                    break;

                default:
                    throw new IllegalStateException("Unknown contingency element type " + element.getType());
            }
        }
        if (!valid) {
            LOGGER.warn("Contingency '{}' is invalid", id);
        }
        return valid;
    }

    /**
     * Return a list of valid contingencies.
     * @deprecated Use {@link ContingencyList#getValidContingencies(List, Network)} ()} instead.
     */
    @Deprecated(since = "4.0.0")
    public static List<Contingency> checkValidity(List<Contingency> contingencies, Network network) {
        return ContingencyList.getValidContingencies(contingencies, network);
    }

    private static boolean checkGeneratorContingency(Contingency contingency, GeneratorContingency element, Network network) {
        if (network.getGenerator(element.getId()) == null) {
            LOGGER.warn("Generator '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkStaticVarCompensatorContingency(Contingency contingency, StaticVarCompensatorContingency element, Network network) {
        if (network.getStaticVarCompensator(element.getId()) == null) {
            LOGGER.warn("StaticVarCompensator '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkShuntCompensatorContingency(Contingency contingency, ShuntCompensatorContingency element, Network network) {
        if (network.getShuntCompensator(element.getId()) == null) {
            LOGGER.warn("ShuntCompensator '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkBranchContingency(Contingency contingency, BranchContingency element, Network network) {
        Branch branch = network.getBranch(element.getId());
        if (branch == null
                || element.getVoltageLevelId() != null
                    && !(element.getVoltageLevelId().equals(branch.getTerminal1().getVoltageLevel().getId())
                        || element.getVoltageLevelId().equals(branch.getTerminal2().getVoltageLevel().getId()))) {
            LOGGER.warn("Branch '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkLineContingency(Contingency contingency, LineContingency element, Network network) {
        Line line = network.getLine(element.getId());
        if (line == null
                || element.getVoltageLevelId() != null
                    && !(element.getVoltageLevelId().equals(line.getTerminal1().getVoltageLevel().getId())
                        || element.getVoltageLevelId().equals(line.getTerminal2().getVoltageLevel().getId()))) {
            LOGGER.warn("Line '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkTwoWindingsTransformerContingency(Contingency contingency, TwoWindingsTransformerContingency element, Network network) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(element.getId());
        if (twt == null
                || element.getVoltageLevelId() != null
                    && !(element.getVoltageLevelId().equals(twt.getTerminal1().getVoltageLevel().getId())
                        || element.getVoltageLevelId().equals(twt.getTerminal2().getVoltageLevel().getId()))) {
            LOGGER.warn("TwoWindingsTransformer '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkHvdcLineContingency(Contingency contingency, HvdcLineContingency element, Network network) {
        HvdcLine hvdcLine = network.getHvdcLine(element.getId());
        if (hvdcLine == null
                || element.getVoltageLevelId() != null
                    && !(element.getVoltageLevelId().equals(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getId())
                        || element.getVoltageLevelId().equals(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getId()))) {
            LOGGER.warn("HVDC line '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkBusbarSectionContingency(Contingency contingency, BusbarSectionContingency element, Network network) {
        if (network.getBusbarSection(element.getId()) == null) {
            LOGGER.warn("Busbar section '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkDanglingLineContingency(Contingency contingency, DanglingLineContingency element, Network network) {
        if (network.getDanglingLine(element.getId()) == null) {
            LOGGER.warn("Dangling line '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkThreeWindingsTransformerContingency(Contingency contingency, ThreeWindingsTransformerContingency element, Network network) {
        if (network.getThreeWindingsTransformer(element.getId()) == null) {
            LOGGER.warn("ThreeWindingsTransformer '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkLoadContingency(Contingency contingency, LoadContingency element, Network network) {
        if (network.getLoad(element.getId()) == null) {
            LOGGER.warn("Load '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkBusContingency(Contingency contingency, BusContingency element, Network network) {
        if (network.getBusBreakerView().getBus(element.getId()) == null) {
            LOGGER.warn("Bus '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkTieLineContingency(Contingency contingency, TieLineContingency element, Network network) {
        TieLine tieLine = network.getTieLine(element.getId());
        if (tieLine == null
                || element.getVoltageLevelId() != null
                    && !(element.getVoltageLevelId().equals(tieLine.getDanglingLine1().getTerminal().getVoltageLevel().getId())
                        || element.getVoltageLevelId().equals(tieLine.getDanglingLine2().getTerminal().getVoltageLevel().getId()))) {
            LOGGER.warn("Tie line '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    public static ContingencyBuilder builder(String id) {
        return new ContingencyBuilder(id);
    }

    /**
     * Creates a new contingency on the battery whose id is given
     */
    public static Contingency battery(String id) {
        return builder(id).addBattery(id).build();
    }

    /**
     * Creates a new contingency on the branch whose id is given
     */
    public static Contingency branch(String id) {
        return builder(id).addBranch(id).build();
    }

    /**
     * Creates a new contingency on the branch whose id is given, on the side of the given voltage level
     */
    public static Contingency branch(String id, String voltageLevelId) {
        return builder(id).addBranch(id, voltageLevelId).build();
    }

    /**
     * Creates a new contingency on the busbar section whose id is given
     */
    public static Contingency busbarSection(String id) {
        return builder(id).addBusbarSection(id).build();
    }

    /**
     * Creates a new contingency on the generator whose id is given
     */
    public static Contingency generator(String id) {
        return builder(id).addGenerator(id).build();
    }

    /**
     * Creates a new contingency on the hvdc line whose id is given
     */
    public static Contingency hvdcLine(String id) {
        return builder(id).addHvdcLine(id).build();
    }

    /**
     * Creates a new contingency on the hvdc line whose id is given, on the side of the given voltage level
     */
    public static Contingency hvdcLine(String id, String voltageLevelId) {
        return builder(id).addHvdcLine(id, voltageLevelId).build();
    }

    /**
     * Creates a new contingency on the line whose id is given
     */
    public static Contingency line(String id) {
        return builder(id).addLine(id).build();
    }

    /**
     * Creates a new contingency on the line whose id is given, on the side of the given voltage level
     */
    public static Contingency line(String id, String voltageLevelId) {
        return builder(id).addLine(id, voltageLevelId).build();
    }

    /**
     * Creates a new contingency on the tie line whose id is given
     */
    public static Contingency tieLine(String id) {
        return builder(id).addTieLine(id).build();
    }

    /**
     * Creates a new contingency on the tie line whose id is given, on the side of the given voltage level
     */
    public static Contingency tieLine(String id, String voltageLevelId) {
        return builder(id).addTieLine(id, voltageLevelId).build();
    }

    /**
     * Creates a new contingency on the shunt compensator whose id is given
     */
    public static Contingency shuntCompensator(String id) {
        return builder(id).addShuntCompensator(id).build();
    }

    /**
     * Creates a new contingency on the static var compensator whose id is given
     */
    public static Contingency staticVarCompensator(String id) {
        return builder(id).addStaticVarCompensator(id).build();
    }

    /**
     * Creates a new contingency on the two-windings transformer whose id is given
     */
    public static Contingency twoWindingsTransformer(String id) {
        return builder(id).addTwoWindingsTransformer(id).build();
    }

    /**
     * Creates a new contingency on the two-windings transformer whose id is given, on the side of the given voltage level
     */
    public static Contingency twoWindingsTransformer(String id, String voltageLevelId) {
        return builder(id).addTwoWindingsTransformer(id, voltageLevelId).build();
    }

    /**
     * Creates a new contingency on the dangline line whose id is given
     */
    public static Contingency danglingLine(String id) {
        return builder(id).addDanglingLine(id).build();
    }

    /**
     * Creates a new contingency on the three-windings transformer whose id is given
     */
    public static Contingency threeWindingsTransformer(String id) {
        return builder(id).addThreeWindingsTransformer(id).build();
    }

    /**
     * Creates a new contingency on the load whose id is given
     */
    public static Contingency load(String loadId) {
        return builder(loadId).addLoad(loadId).build();
    }

    /**
     * Creates a new contingency on the bus whose id is given
     */
    public static Contingency bus(String busId) {
        return builder(busId).addBus(busId).build();
    }

}
