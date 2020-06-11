/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.util;

import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteElementId;

import java.util.Optional;

import static com.powsybl.ucte.network.UcteElementId.parseUcteElementId;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class UcteAliasesCreation {

    private static final String ALIAS_TRIPLET_TEMPLATE = "%s %s %s";
    private static final String ELEMENT_NAME_PROPERTY_KEY = "elementName";

    private UcteAliasesCreation() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void createAliases(Network network) {
        network.getBranchStream().forEach(branch -> {
            if (branch instanceof TieLine) {
                TieLine tieLine = (TieLine) branch;
                tieLine.addAlias(tieLine.getHalf1().getId());
                tieLine.addAlias(tieLine.getHalf2().getId());
                addHalfElementNameAliases(tieLine);
            }
            addElementNameAlias(branch);
        });
        network.getSwitchStream().forEach(UcteAliasesCreation::addElementNameAlias);
        network.getDanglingLineStream().forEach(UcteAliasesCreation::addElementNameAlias);
    }

    private static void addElementNameAlias(Identifiable identifiable) {
        String elementNameProperty = identifiable.getProperty(ELEMENT_NAME_PROPERTY_KEY);
        if (elementNameProperty != null && !elementNameProperty.isEmpty()) {
            Optional<UcteElementId> ucteElementIdOptional = parseUcteElementId(identifiable.getId());
            if (ucteElementIdOptional.isPresent()) {
                UcteElementId ucteElementId = ucteElementIdOptional.get();
                identifiable.addAlias(String.format(ALIAS_TRIPLET_TEMPLATE, ucteElementId.getNodeCode1(), ucteElementId.getNodeCode2(), elementNameProperty));
            }
        }
    }

    private static void addHalfElementNameAliases(TieLine tieLine) {
        String elementName1Property = tieLine.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_1");
        if (elementName1Property != null && !elementName1Property.isEmpty()) {
            Optional<UcteElementId> ucteElementIdOptional = parseUcteElementId(tieLine.getHalf1().getId());
            if (ucteElementIdOptional.isPresent()) {
                UcteElementId ucteElementId = ucteElementIdOptional.get();
                tieLine.addAlias(String.format(ALIAS_TRIPLET_TEMPLATE, ucteElementId.getNodeCode1(), ucteElementId.getNodeCode2(), elementName1Property));
            }
        }
        String elementName2Property = tieLine.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_2");
        if (elementName2Property != null && !elementName2Property.isEmpty()) {
            Optional<UcteElementId> ucteElementIdOptional = parseUcteElementId(tieLine.getHalf2().getId());
            if (ucteElementIdOptional.isPresent()) {
                UcteElementId ucteElementId = ucteElementIdOptional.get();
                tieLine.addAlias(String.format(ALIAS_TRIPLET_TEMPLATE, ucteElementId.getNodeCode1(), ucteElementId.getNodeCode2(), elementName2Property));
            }
        }
    }
}
