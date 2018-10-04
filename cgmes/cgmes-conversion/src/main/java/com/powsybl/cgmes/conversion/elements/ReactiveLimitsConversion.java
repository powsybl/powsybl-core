package com.powsybl.cgmes.conversion.elements;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class ReactiveLimitsConversion {

    private ReactiveLimitsConversion() {
    }

    public static void convert(PropertyBag p, Generator g) {
        if (p.containsKey("minQ") && p.containsKey("maxQ")) {
            double minQ = p.asDouble("minQ");
            double maxQ = p.asDouble("maxQ");
            g.newMinMaxReactiveLimits()
                    .setMinQ(minQ)
                    .setMaxQ(maxQ)
                    .add();
        }
        // TODO Reactive capability curves
    }
}
