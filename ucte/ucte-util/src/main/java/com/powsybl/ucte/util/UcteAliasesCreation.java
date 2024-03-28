/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.util;

import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteElementId;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.ucte.network.UcteElementId.parseUcteElementId;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class UcteAliasesCreation {

    private static final String ALIAS_TRIPLET_TEMPLATE = "%s %s %s";
    private static final String ELEMENT_NAME_PROPERTY_KEY = "elementName";

    private UcteAliasesCreation() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static void createAliases(Network network) {
        Set<String> duplicatedAliases = new HashSet<>();
        network.getBranchStream().forEach(branch -> addElementNameAlias(branch, duplicatedAliases));
        network.getTieLineStream().forEach(tieLine -> {
            addHalfElementNameAliases(tieLine, duplicatedAliases);
            addElementNameAlias(tieLine, duplicatedAliases);
        });
        network.getSwitchStream().forEach(switchEl -> addElementNameAlias(switchEl, duplicatedAliases));
        network.getDanglingLineStream(DanglingLineFilter.UNPAIRED).forEach(dl -> addElementNameAlias(dl, duplicatedAliases));
    }

    private static void addElementNameAlias(Identifiable<?> identifiable, Set<String> duplicatedAliases) {
        String elementNameProperty = identifiable.getProperty(ELEMENT_NAME_PROPERTY_KEY);
        if (elementNameProperty != null && !elementNameProperty.isEmpty()) {
            Optional<UcteElementId> ucteElementIdOptional = parseUcteElementId(identifiable.getId());
            if (ucteElementIdOptional.isPresent()) {
                UcteElementId ucteElementId = ucteElementIdOptional.get();
                safeAddAlias(identifiable, duplicatedAliases, String.format(ALIAS_TRIPLET_TEMPLATE, ucteElementId.getNodeCode1(), ucteElementId.getNodeCode2(), elementNameProperty));
            }
        }
    }

    private static void addHalfElementNameAliases(TieLine tieLine, Set<String> duplicatedAliases) {
        String elementName1Property = tieLine.getDanglingLine1().getProperty(ELEMENT_NAME_PROPERTY_KEY);
        if (elementName1Property != null && !elementName1Property.isEmpty()) {
            Optional<UcteElementId> ucteElementIdOptional = parseUcteElementId(tieLine.getDanglingLine1().getId());
            if (ucteElementIdOptional.isPresent()) {
                UcteElementId ucteElementId = ucteElementIdOptional.get();
                safeAddAlias(tieLine, duplicatedAliases, String.format(ALIAS_TRIPLET_TEMPLATE, ucteElementId.getNodeCode1(), ucteElementId.getNodeCode2(), elementName1Property));
            }
        }
        String elementName2Property = tieLine.getDanglingLine2().getProperty(ELEMENT_NAME_PROPERTY_KEY);
        if (elementName2Property != null && !elementName2Property.isEmpty()) {
            Optional<UcteElementId> ucteElementIdOptional = parseUcteElementId(tieLine.getDanglingLine2().getId());
            if (ucteElementIdOptional.isPresent()) {
                UcteElementId ucteElementId = ucteElementIdOptional.get();
                safeAddAlias(tieLine, duplicatedAliases, String.format(ALIAS_TRIPLET_TEMPLATE, ucteElementId.getNodeCode1(), ucteElementId.getNodeCode2(), elementName2Property));
            }
        }
    }

    private static void safeAddAlias(Identifiable<?> identifiable, Set<String> duplicatedAliases, String alias) {
        if (duplicatedAliases.contains(alias)) {
            return;
        }

        Identifiable<?> alreadyAssignedIdentifiable = identifiable.getNetwork().getIdentifiable(alias);
        if (alreadyAssignedIdentifiable != null && alreadyAssignedIdentifiable != identifiable) {
            if (!alreadyAssignedIdentifiable.getId().equals(alias)) {
                alreadyAssignedIdentifiable.removeAlias(alias);
            }
            duplicatedAliases.add(alias);
            return;
        }

        identifiable.addAlias(alias);
    }
}
