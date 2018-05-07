/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.HvdcLine;

/**
*
* @author Ferrari Giovanni <giovanni.ferrari@techrain.eu>
*/
public class FooExtension extends AbstractExtension<HvdcLine> {

    @Override
    public String getName() {
        return "Foo";
    }

}
