/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class BranchObservabilitySerDe<T extends Branch<T>> extends AbstractExtensionSerDe<T, BranchObservability<T>> {

    private static final String QUALITY_P = "qualityP";
    private static final String QUALITY_Q = "qualityQ";

    private static final String SIDE = "side";
    private static final String STANDARD_DEVIATION = "standardDeviation";
    private static final String REDUNDANT = "redundant";

    public BranchObservabilitySerDe() {
        super("branchObservability", "network", BranchObservability.class, "branchObservability.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/branch_observability/1_0", "bo");
    }

    @Override
    public void write(BranchObservability<T> branchObservability, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("observable", branchObservability.isObservable(), false);

        // qualityP1
        writeOptionalQuality(context, branchObservability.getQualityP1(), QUALITY_P, TwoSides.ONE);

        // qualityP2
        writeOptionalQuality(context, branchObservability.getQualityP2(), QUALITY_P, TwoSides.TWO);

        // qualityQ1
        writeOptionalQuality(context, branchObservability.getQualityQ1(), QUALITY_Q, TwoSides.ONE);

        // qualityQ2
        writeOptionalQuality(context, branchObservability.getQualityQ2(), QUALITY_Q, TwoSides.TWO);
    }

    private void writeOptionalQuality(SerializerContext context, ObservabilityQuality<T> quality, String type, TwoSides side) {
        if (quality != null) {
            context.getWriter().writeStartNode(getNamespaceUri(), type);
            context.getWriter().writeEnumAttribute(SIDE, side);
            context.getWriter().writeDoubleAttribute(STANDARD_DEVIATION, quality.getStandardDeviation());
            context.getWriter().writeOptionalBooleanAttribute(REDUNDANT, quality.isRedundant().orElse(null));
            context.getWriter().writeEndNode();
        }
    }

    @Override
    public BranchObservability<T> read(T identifiable, DeserializerContext context) {
        boolean observable = context.getReader().readBooleanAttribute("observable", false);

        BranchObservabilityAdder<T> adder = identifiable.newExtension(BranchObservabilityAdder.class)
                .withObservable(observable);

        context.getReader().readChildNodes(elementName -> {
            var side = context.getReader().readEnumAttribute(SIDE, TwoSides.class);
            var standardDeviation = context.getReader().readDoubleAttribute(STANDARD_DEVIATION);
            var redundant = context.getReader().readOptionalBooleanAttribute(REDUNDANT).orElse(null);
            context.getReader().readEndNode();
            switch (elementName) {
                case QUALITY_P -> readQualityP(standardDeviation, redundant, side, adder);
                case QUALITY_Q -> readQualityQ(standardDeviation, redundant, side, adder);
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'branchObservability'");
            }
        });

        return adder.add();
    }

    private void readQualityP(double standardDeviation, Boolean redundant, TwoSides side, BranchObservabilityAdder<T> adder) {
        if (side == TwoSides.ONE) {
            adder.withStandardDeviationP1(standardDeviation);
            if (redundant != null) {
                adder.withRedundantP1(redundant);
            }
        } else if (side == TwoSides.TWO) {
            adder.withStandardDeviationP2(standardDeviation);
            if (redundant != null) {
                adder.withRedundantP2(redundant);
            }
        }
    }

    private void readQualityQ(double standardDeviation, Boolean redundant, TwoSides side, BranchObservabilityAdder<T> adder) {
        if (side == TwoSides.ONE) {
            adder.withStandardDeviationQ1(standardDeviation);
            if (redundant != null) {
                adder.withRedundantQ1(redundant);
            }
        } else if (side == TwoSides.TWO) {
            adder.withStandardDeviationQ2(standardDeviation);
            if (redundant != null) {
                adder.withRedundantQ2(redundant);
            }
        }
    }
}
