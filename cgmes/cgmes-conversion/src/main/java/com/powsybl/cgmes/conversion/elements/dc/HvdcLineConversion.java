/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class HvdcLineConversion extends AbstractIdentifiedObjectConversion {

    private final DCLink dcLink;

    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    public HvdcLineConversion(DCLink dcLink, Context context) {
        // Use line 1 id as the main identifier of this line.
        // If present, line 2 id is added as an alias.
        super(DC_LINE_SEGMENT, dcLink.getDcLine1(), context);
        this.dcLink = dcLink;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {
        HvdcLineAdder adder = context.network().newHvdcLine()
                .setR(dcLink.getR())
                .setNominalV(dcLink.getRatedUdc())
                .setActivePowerSetpoint(dcLink.getTargetP())
                .setMaxP(DEFAULT_MAXP_FACTOR * dcLink.getTargetP())
                .setConvertersMode(dcLink.getMode())
                .setConverterStationId1(context.namingStrategy().getIidmId(ACDC_CONVERTER, dcLink.getConverter1().getId(ACDC_CONVERTER)))
                .setConverterStationId2(context.namingStrategy().getIidmId(ACDC_CONVERTER, dcLink.getConverter2().getId(ACDC_CONVERTER)));
        identify(adder);
        HvdcLine hvdcLine = adder.add();

        // Add aliases
        hvdcLine.addAlias(dcLink.getDcLine1().getId(DC_TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
        hvdcLine.addAlias(dcLink.getDcLine1().getId(DC_TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
        if (dcLink.getDcLine2() != null) {
            hvdcLine.addAlias(dcLink.getDcLine2().getId(DC_LINE_SEGMENT), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_LINE_SEGMENT2);
        }
    }

}
