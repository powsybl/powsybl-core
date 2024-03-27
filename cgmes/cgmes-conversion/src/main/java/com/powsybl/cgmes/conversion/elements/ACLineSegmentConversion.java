/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Optional;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ACLineSegmentConversion extends AbstractBranchConversion implements EquipmentAtBoundaryConversion {

    private DanglingLine danglingLine;

    public ACLineSegmentConversion(PropertyBag line, Context context) {
        super(CgmesNames.AC_LINE_SEGMENT, line, context);
    }

    @Override
    public boolean valid() {
        // An AC line segment end voltage level may be null
        // (when it is in the boundary and the boundary nodes are not converted)
        // So we do not use the generic validity check for conducting equipment
        // or branch. We only ensure we have nodes at both ends
        for (int k = 1; k <= 2; k++) {
            if (nodeId(k) == null) {
                missing(nodeIdPropertyName() + k);
                return false;
            }
        }
        return true;
    }

    @Override
    public void convert() {
        convertLine();
    }

    @Override
    public void convertAtBoundary() {
        if (isBoundary(1)) {
            convertLineAtBoundary(1);
        } else if (isBoundary(2)) {
            convertLineAtBoundary(2);
        } else {
            throw new ConversionException("Boundary must be at one end of the line");
        }
    }

    @Override
    public Optional<DanglingLine> getDanglingLine() {
        return Optional.ofNullable(danglingLine);
    }

    public boolean isConnectedAtBothEnds() {
        return terminalConnected(1) && terminalConnected(2);
    }

    public static void convertToTieLine(Context context, DanglingLine dl1, DanglingLine dl2) {
        TieLineAdder adder = context.network().newTieLine()
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId());
        identify(context, adder, context.namingStrategy().getIidmId("TieLine", TieLineUtil.buildMergedId(dl1.getId(), dl2.getId())),
                TieLineUtil.buildMergedName(dl1.getId(), dl2.getId(), dl1.getNameOrId(), dl2.getNameOrId()));
        adder.add();
    }

    private void convertLine() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double gch = p.asDouble("gch", 0.0);
        double bch = p.asDouble("bch");
        convertBranch(r, x, gch, bch);
    }

    private void convertLineAtBoundary(int boundarySide) {
        // If we have created buses and substations for boundary nodes,
        // convert as a regular line
        if (context.config().convertBoundary()) {
            convertLine();
        } else {
            double r = p.asDouble("r");
            double x = p.asDouble("x");
            double gch = p.asDouble("gch", 0.0);
            double bch = p.asDouble("bch");

            String eqInstance = p.get("graph");
            danglingLine = convertToDanglingLine(eqInstance, boundarySide, r, x, gch, bch);
        }
    }
}
