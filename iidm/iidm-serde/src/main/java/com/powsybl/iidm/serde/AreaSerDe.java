/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.OptionalDouble;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaSerDe extends AbstractSimpleIdentifiableSerDe<Area, AreaAdder, Network> {

    static final AreaSerDe INSTANCE = new AreaSerDe();
    static final String ROOT_ELEMENT_NAME = "area";
    static final String ARRAY_ELEMENT_NAME = "areas";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final Area area, final Network parent, final NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute("areaType", context.getAnonymizer().anonymizeString(area.getAreaType()));
        Double interchangeTarget = null;
        if (area.getInterchangeTarget().isPresent()) {
            interchangeTarget = area.getInterchangeTarget().getAsDouble();
        }
        context.getWriter().writeOptionalDoubleAttribute("interchangeTarget", interchangeTarget);
    }

    @Override
    protected void writeSubElements(final Area area, final Network parent, final NetworkSerializerContext context) {
        writeVoltageLevels(area, context);
        AreaBoundarySerDe.INSTANCE.write(area, context);
    }

    private void writeVoltageLevels(Area area, NetworkSerializerContext context) {
        for (VoltageLevel voltageLevel : IidmSerDeUtil.sorted(area.getVoltageLevels(), context.getOptions())) {
            VoltageLevelRefSerDe.writeVoltageLevelRef(voltageLevel, context);
        }
    }

    @Override
    protected AreaAdder createAdder(final Network network) {
        return network.newArea();
    }

    @Override
    protected Area readRootElementAttributes(final AreaAdder adder, final Network parent, final NetworkDeserializerContext context) {
        String areaType = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("areaType"));
        adder.setAreaType(areaType);
        OptionalDouble interchangeTarget = context.getReader().readOptionalDoubleAttribute("interchangeTarget");
        interchangeTarget.ifPresent(adder::setInterchangeTarget);
        return adder.add();
    }

    @Override
    protected void readSubElements(final Area area, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case VoltageLevelRefSerDe.ROOT_ELEMENT_NAME -> VoltageLevelRefSerDe.readVoltageLevelRef(context, area.getNetwork(), area::addVoltageLevel);
                case AreaBoundarySerDe.ROOT_ELEMENT_NAME -> AreaBoundarySerDe.INSTANCE.read(area, context);
                default -> readSubElement(elementName, area, context);
            }
        });
    }
}
