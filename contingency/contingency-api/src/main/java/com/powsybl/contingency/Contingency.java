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
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class Contingency extends AbstractExtendable<Contingency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Contingency.class);

    private String id;

    private final List<ContingencyElement> elements;

    public Contingency(String id, ContingencyElement... elements) {
        this(id, Arrays.asList(elements));
    }

    public Contingency(String id, List<ContingencyElement> elements) {
        this.id = Objects.requireNonNull(id);
        this.elements = new ArrayList<>(Objects.requireNonNull(elements));
    }

    private static boolean checkGeneratorContingency(Contingency contingency, GeneratorContingency element, Network network) {
        if (network.getGenerator(element.getId()) == null) {
            LOGGER.warn("Generator '{}' of contingency '{}' not found", element.getId(), contingency.getId());
            return false;
        }
        return true;
    }

    private static boolean checkSidedContingency(Contingency contingency, AbstractSidedContingency element, Network network) {
        Branch branch = network.getBranch(element.getId());
        if (branch == null || (element.getVoltageLevelId() != null &&
                !(element.getVoltageLevelId().equals(branch.getTerminal1().getVoltageLevel().getId()) ||
                        element.getVoltageLevelId().equals(branch.getTerminal2().getVoltageLevel().getId())))) {
            LOGGER.warn("Branch or HVDC line '{}' of contingency '{}' not found", element.getId(), contingency.getId());
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

    private static boolean isValid(Contingency contingency, Network network) {
        Objects.requireNonNull(contingency);
        Objects.requireNonNull(network);
        boolean valid = true;
        for (ContingencyElement element : contingency.getElements()) {
            switch (element.getType()) {
                case GENERATOR:
                    valid = checkGeneratorContingency(contingency, (GeneratorContingency) element, network);
                    break;

                case BRANCH:
                case HVDC_LINE:
                    valid = checkSidedContingency(contingency, (AbstractSidedContingency) element, network);
                    break;

                case BUSBAR_SECTION:
                    valid = checkBusbarSectionContingency(contingency, (BusbarSectionContingency) element, network);
                    break;

                default:
                    throw new AssertionError("Unknown contingency element type " + element.getType());
            }
        }
        if (!valid) {
            LOGGER.warn("Contingency '{}' is invalid", contingency.getId());
        }
        return valid;
    }

    public static List<Contingency> checkValidity(List<Contingency> contingencies, Network network) {
        Objects.requireNonNull(contingencies);
        Objects.requireNonNull(network);
        return contingencies.stream()
                .filter(c -> isValid(c, network))
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public void addElement(ContingencyElement element) {
        Objects.requireNonNull(element);
        elements.add(element);
    }

    public void removeElement(ContingencyElement element) {
        Objects.requireNonNull(element);
        elements.remove(element);
    }

    public Collection<ContingencyElement> getElements() {
        return Collections.unmodifiableCollection(elements);
    }

    public ModificationTask toTask() {
        List<ModificationTask> subTasks = elements.stream().map(ContingencyElement::toTask).collect(Collectors.toList());

        return new CompoundModificationTask(subTasks);
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
}
