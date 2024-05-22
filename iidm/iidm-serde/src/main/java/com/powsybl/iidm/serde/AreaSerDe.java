/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaAdder;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaSerDe extends AbstractSimpleIdentifiableSerDe<Area, AreaAdder, Network> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AreaSerDe.class);

    static final AreaSerDe INSTANCE = new AreaSerDe();

    static final String ROOT_ELEMENT_NAME = "area";
    static final String ARRAY_ELEMENT_NAME = "areas";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final Area area, final Network parent, final NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute("areaType", context.getAnonymizer().anonymizeString(area.getAreaType().getId()));
        area.getAcNetInterchangeTarget().ifPresent(target -> context.getWriter().writeDoubleAttribute("acNetInterchangeTarget", target));
        area.getAcNetInterchangeTolerance().ifPresent(tolerance -> context.getWriter().writeDoubleAttribute("acNetInterchangeTolerance", tolerance));
    }

    @Override
    protected AreaAdder createAdder(final Network network) {
        return network.newArea();
    }

    @Override
    protected Area readRootElementAttributes(final AreaAdder adder, final Network parent, final NetworkDeserializerContext context) {
        String areaTypeId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("areaType"));
        double acNetInterchangeTarget = context.getReader().readDoubleAttribute("acNetInterchangeTarget", Double.NaN);
        double acNetInterchangeTolerance = context.getReader().readDoubleAttribute("acNetInterchangeTolerance", Double.NaN);
        AreaType areaType = parent.getAreaType(areaTypeId);
        if (areaType == null) {
            throw new PowsyblException("Area Type Identifiable '" + areaTypeId + "' not found");
        }
        return adder.setAreaType(areaType)
                    .setAcNetInterchangeTarget(acNetInterchangeTarget)
                    .setAcNetInterchangeTolerance(acNetInterchangeTolerance)
                    .add();
    }

    @Override
    protected void readSubElements(final Area identifiable, final NetworkDeserializerContext context) {
        // No sub-elements to read: just end reading
        context.getReader().readEndNode();
    }
}
