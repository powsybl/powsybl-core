/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.cgmes.model.CgmesNames.DC_LINE_SEGMENT;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCLink {

    private double r;
    private double ratedUdc;

    private final PropertyBag converter1;
    private final PropertyBag converter2;
    private final PropertyBag dcLine1;
    private final PropertyBag dcLine2;

    private static final Logger LOG = LoggerFactory.getLogger(DCLink.class);

    public DCLink(PropertyBag converter1, PropertyBag converter2, PropertyBag dcLine1, PropertyBag dcLine2) {
        this.converter1 = converter1;
        this.converter2 = converter2;
        this.dcLine1 = dcLine1;
        this.dcLine2 = dcLine2;

        computeR();
        computeRatedUdc();
    }

    private void computeR() {
        double r1 = dcLine1.asDouble("r");
        r1 = Double.isNaN(r1) ? 0.1 : r1;
        if (dcLine2 == null) {
            r = r1;
        } else {
            double r2 = dcLine2.asDouble("r", 0.1);
            r2 = Double.isNaN(r2) ? 0.1 : r2;
            r = r1 + r2;
        }

        if (r < 0.0) {
            String dcLine1Id = dcLine1.getId(DC_LINE_SEGMENT);
            LOG.warn("Invalid r for DCLink with DCLineSegment: {}. Was: {}, fixed to 0.1.", dcLine1Id, r);
            r = 0.1;
        }
    }

    private void computeRatedUdc() {
        ratedUdc = converter1.asDouble(CgmesNames.RATED_UDC);
        if (ratedUdc == 0.0) {
            ratedUdc = converter2.asDouble(CgmesNames.RATED_UDC);
        }
    }

    public double getR() {
        return r;
    }

    public double getRatedUdc() {
        return ratedUdc;
    }

    public PropertyBag getConverter1() {
        return converter1;
    }

    public PropertyBag getConverter2() {
        return converter2;
    }

    public PropertyBag getDcLine1() {
        return dcLine1;
    }

    public PropertyBag getDcLine2() {
        return dcLine2;
    }
}
