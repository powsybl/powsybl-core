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
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class TwoWindingsTransformerToBeEstimatedSerDe extends AbstractExtensionSerDe<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated> {

    public TwoWindingsTransformerToBeEstimatedSerDe() {
        super("twoWindingsTransformerToBeEstimated", "network", TwoWindingsTransformerToBeEstimated.class,
                "twoWindingsTransformerToBeEstimated.xsd",
                "http://www.powsybl.org/schema/iidm/ext/two_windings_transformer_to_be_estimated/1_0", "twottbe");
    }

    @Override
    public void write(TwoWindingsTransformerToBeEstimated extension, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("ratioTapChangerStatus", extension.shouldEstimateRatioTapChanger());
        context.getWriter().writeBooleanAttribute("phaseTapChangerStatus", extension.shouldEstimatePhaseTapChanger());
    }

    @Override
    public TwoWindingsTransformerToBeEstimated read(TwoWindingsTransformer extendable, DeserializerContext context) {
        var extension = extendable.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChangerStatus(context.getReader().readBooleanAttribute("ratioTapChangerStatus"))
                .withPhaseTapChangerStatus(context.getReader().readBooleanAttribute("phaseTapChangerStatus"))
                .add();
        context.getReader().readEndNode();
        return extension;
    }
}
