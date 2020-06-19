/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.contingency.tasks.CompoundModificationTask;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class Contingency extends AbstractExtendable<Contingency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Contingency.class);

    private final String id;

    private final List<ContingencyElement> elements;

    public Contingency(String id, ContingencyElement... elements) {
        this.id = Objects.requireNonNull(id);
        this.elements = Arrays.asList(elements);
    }

    public Contingency(String id, List<ContingencyElement> elements) {
        this.id = Objects.requireNonNull(id);
        this.elements = new ArrayList<>(elements);
    }

    public String getId() {
        return id;
    }

    public List<ContingencyElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contingency) {
            Contingency other = (Contingency) obj;
            return id.equals(other.id) && elements.equals(other.elements);
        }
        return false;
    }

    public ModificationTask toTask() {
        List<ModificationTask> subTasks = elements.stream().map(ContingencyElement::toTask).collect(Collectors.toList());

        return new CompoundModificationTask(subTasks);
    }

    boolean isValid(Network network) {
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

                default:
                    throw new AssertionError("Unknown contingency element type " + element.getType());
            }
        }
        if (!valid) {
            LOGGER.warn("Contingency '{}' is invalid", id);
        }
        return valid;
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
        if (branch == null || (element.getVoltageLevelId() != null &&
                !(element.getVoltageLevelId().equals(branch.getTerminal1().getVoltageLevel().getId()) ||
                        element.getVoltageLevelId().equals(branch.getTerminal2().getVoltageLevel().getId())))) {
            LOGGER.warn("Branch '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkHvdcLineContingency(Contingency contingency, HvdcLineContingency element, Network network) {
        HvdcLine hvdcLine = network.getHvdcLine(element.getId());
        if (hvdcLine == null || (element.getVoltageLevelId() != null &&
                !(element.getVoltageLevelId().equals(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getId()) ||
                        element.getVoltageLevelId().equals(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getId())))) {
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

    public static ContingencyBuilder builder(String id) {
        return new ContingencyBuilder(id);
    }

    public static Contingency branch(String id) {
        return new Contingency(id, new BranchContingency(id));
    }

    public static Contingency branch(String id, String voltageLevelId) {
        return new Contingency(id, new BranchContingency(id, voltageLevelId));
    }

    public static Contingency busbarSection(String id) {
        return new Contingency(id, new BusbarSectionContingency(id));
    }

    public static Contingency generator(String id) {
        return new Contingency(id, new GeneratorContingency(id));
    }

    public static Contingency hvdcLine(String id) {
        return new Contingency(id, new HvdcLineContingency(id));
    }

    public static Contingency hvdcLine(String id, String voltageLevel) {
        return new Contingency(id, new HvdcLineContingency(id, voltageLevel));
    }

    public static Contingency line(String id) {
        // FIXME(mathbagu): Check that the ID is really a line, not a two windings transformer
        return new Contingency(id, new BranchContingency(id));
    }

    public static Contingency line(String id, String voltageLevelId) {
        // FIXME(mathbagu): Check that the ID is really a line, not a two windings transformer
        return new Contingency(id, new BranchContingency(id, voltageLevelId));
    }

    public static Contingency shuntCompensator(String id) {
        return new Contingency(id, new ShuntCompensatorContingency(id));
    }

    public static Contingency staticVarCompensator(String id) {
        return new Contingency(id, new StaticVarCompensatorContingency(id));
    }

    public static Contingency twoWindingsTransformer(String id) {
        // FIXME(mathbagu): Check that the ID is really a two windings transformer, not a line
        return new Contingency(id, new BranchContingency(id));
    }

    public static Contingency twoWindingsTransformer(String id, String voltageLevelId) {
        // FIXME(mathbagu): Check that the ID is really a two windings transformer, not a line
        return new Contingency(id, new BranchContingency(id, voltageLevelId));
    }
}
