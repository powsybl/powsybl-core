/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ThreeWindingsTransformerToBeEstimatedSerDe extends AbstractExtensionSerDe<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated> {

    public ThreeWindingsTransformerToBeEstimatedSerDe() {
        super("threeWindingsTransformerToBeEstimated", "network", ThreeWindingsTransformerToBeEstimated.class,
                "threeWindingsTransformerToBeEstimated.xsd", "http://www.powsybl.org/schema/iidm/ext/three_windings_transformer_to_be_estimated/1_0", "threettbe");
    }

    @Override
    public void write(ThreeWindingsTransformerToBeEstimated extension, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("ratioTapChanger1Status", extension.shouldEstimateRatioTapChanger1());
        context.getWriter().writeBooleanAttribute("ratioTapChanger2Status", extension.shouldEstimateRatioTapChanger2());
        context.getWriter().writeBooleanAttribute("ratioTapChanger3Status", extension.shouldEstimateRatioTapChanger3());
        context.getWriter().writeBooleanAttribute("phaseTapChanger1Status", extension.shouldEstimatePhaseTapChanger1());
        context.getWriter().writeBooleanAttribute("phaseTapChanger2Status", extension.shouldEstimatePhaseTapChanger2());
        context.getWriter().writeBooleanAttribute("phaseTapChanger3Status", extension.shouldEstimatePhaseTapChanger3());
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated read(ThreeWindingsTransformer extendable, DeserializerContext context) {
        var extension = extendable.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChanger1Status(context.getReader().readBooleanAttribute("ratioTapChanger1Status"))
                .withRatioTapChanger2Status(context.getReader().readBooleanAttribute("ratioTapChanger2Status"))
                .withRatioTapChanger3Status(context.getReader().readBooleanAttribute("ratioTapChanger3Status"))
                .withPhaseTapChanger1Status(context.getReader().readBooleanAttribute("phaseTapChanger1Status"))
                .withPhaseTapChanger2Status(context.getReader().readBooleanAttribute("phaseTapChanger2Status"))
                .withPhaseTapChanger3Status(context.getReader().readBooleanAttribute("phaseTapChanger3Status"))
                .add();
        context.getReader().readEndNode();
        return extension;
    }
}
