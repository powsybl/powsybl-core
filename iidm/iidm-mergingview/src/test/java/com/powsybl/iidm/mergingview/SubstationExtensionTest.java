/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Substation;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class SubstationExtensionTest extends AbstractExtension<Substation> {

    public SubstationExtensionTest(final Substation substation) {
        super(substation);
    }

    @Override
    public String getName() {
        return "SubstationExtensionTest";
    }

}

