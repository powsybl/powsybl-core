/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class SeriesCompensatorConversion extends AbstractBranchConversion {

    public SeriesCompensatorConversion(PropertyBag sc, Context context) {
        super(CgmesNames.SERIES_COMPENSATOR, sc, context);
    }

    @Override
    public void convert() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double gch = 0;
        double bch = 0;
        convertBranch(r, x, gch, bch, CgmesNames.SERIES_COMPENSATOR);
    }

    public static void update(Line line, Context context) {
        updateBranch(line, context);
    }
}
