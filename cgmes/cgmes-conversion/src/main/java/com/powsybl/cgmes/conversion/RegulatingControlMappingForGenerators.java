/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface RegulatingControlMappingForGenerators {

    void initialize(GeneratorAdder adder);

    void add(String generatorId, PropertyBag sm);

    void applyRegulatingControls(Network network);

    static RegulatingControlMappingForGenerators create(boolean fixSsh, RegulatingControlMapping parent, Context context) {
        if (fixSsh) {
            return new FixedRegulatingControlMappingForGenerators(parent, context);
        } else {
            return new UnfixedRegulatingControlMappingForGenerators(parent, context);
        }
    }
}
