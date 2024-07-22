/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class DcLineSegmentConversion extends AbstractIdentifiedObjectConversion {

    static final double DEFAULT_MAX_P = 0.0;
    static final double DEFAULT_ACTIVE_POWER_SET_POINT = 0.0;

    DcLineSegmentConversion(PropertyBag l, HvdcLine.ConvertersMode mode, double r, double ratedUdc,
        String converterId1, String converterId2, boolean isDuplicated, Context context) {
        super("DCLineSegment", l, context);

        this.mode = mode;
        this.r = r;
        this.ratedUdc = ratedUdc;
        this.converterId1 = Objects.requireNonNull(converterId1);
        this.converterId2 = Objects.requireNonNull(converterId2);
        this.isDuplicated = isDuplicated;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {

        HvdcLineAdder adder = context.network().newHvdcLine()
            .setR(r)
            .setNominalV(ratedUdc)
            .setActivePowerSetpoint(DEFAULT_ACTIVE_POWER_SET_POINT)
            .setMaxP(DEFAULT_MAX_P)
            .setConvertersMode(mode)
            .setConverterStationId1(context.namingStrategy().getIidmId("ACDCConverter", converterId1))
            .setConverterStationId2(context.namingStrategy().getIidmId("ACDCConverter", converterId2));
        identify(adder, isDuplicated ? "-1" : "");
        HvdcLine hvdcLine = adder.add();

        addHvdcAliasesAndProperties(super.p, isDuplicated ? "-1" : "", context.cgmes(), hvdcLine);
    }

    // We do not use "#n" to guarantee uniqueness since the getId() method does not support more than one '#' character
    private static void addHvdcAliasesAndProperties(PropertyBag pb, String duplicatedTag, CgmesModel cgmesModel, HvdcLine hvdcLine) {
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(pb.getId(CgmesNames.DC_TERMINAL + 1));
        String dcNode1 = CgmesDcConversion.getDcNode(cgmesModel, t1);
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(pb.getId(CgmesNames.DC_TERMINAL + 2));
        String dcNode2 = CgmesDcConversion.getDcNode(cgmesModel, t2);

        // connectivityNode, topologicalNode or both ???
        hvdcLine.addAlias(t1.id() + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL + 1);
        hvdcLine.addAlias(t2.id() + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL + 2);
        hvdcLine.addAlias(dcNode1 + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + 1);
        hvdcLine.addAlias(dcNode2 + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + 2);
    }

    private final HvdcLine.ConvertersMode mode;
    private final double r;
    private final double ratedUdc;
    private final String converterId1;
    private final String converterId2;
    private final boolean isDuplicated;
}
