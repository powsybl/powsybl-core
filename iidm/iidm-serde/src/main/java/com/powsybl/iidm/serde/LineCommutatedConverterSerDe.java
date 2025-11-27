/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class LineCommutatedConverterSerDe extends AbstractAcDcConverterSerDe<LineCommutatedConverter, LineCommutatedConverterAdder> {

    static final LineCommutatedConverterSerDe INSTANCE = new LineCommutatedConverterSerDe();
    static final String ROOT_ELEMENT_NAME = "lineCommutatedConverter";
    static final String ARRAY_ELEMENT_NAME = "lineCommutatedConverters";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final LineCommutatedConverter lcc, final VoltageLevel parent, final NetworkSerializerContext context) {
        super.writeRootElementAttributes(lcc, parent, context);
        context.getWriter().writeEnumAttribute("reactiveModel", lcc.getReactiveModel());
        context.getWriter().writeDoubleAttribute("powerFactor", lcc.getPowerFactor());
        super.writeRootElementPqiAttributes(lcc, context);
    }

    @Override
    protected LineCommutatedConverterAdder createAdder(final VoltageLevel voltageLevel) {
        return voltageLevel.newLineCommutatedConverter();
    }

    @Override
    protected LineCommutatedConverter readRootElementAttributes(final LineCommutatedConverterAdder adder, final VoltageLevel parent, final NetworkDeserializerContext context) {
        super.readRootElementCommonAttributes(adder, parent, context);
        LineCommutatedConverter.ReactiveModel reactiveModel = context.getReader().readEnumAttribute("reactiveModel", LineCommutatedConverter.ReactiveModel.class);
        double powerFactor = context.getReader().readDoubleAttribute("powerFactor");
        LineCommutatedConverter lcc = adder
                .setReactiveModel(reactiveModel)
                .setPowerFactor(powerFactor)
                .add();
        super.readRootElementPqiAttributes(lcc, context);
        return lcc;
    }
}
