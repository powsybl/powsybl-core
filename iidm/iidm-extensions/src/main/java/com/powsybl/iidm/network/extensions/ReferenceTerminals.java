/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferenceTerminals<C extends Connectable<C>> extends Extension<C> {

    String NAME = "referenceTerminals";

    @Override
    default String getName() {
        return NAME;
    }

    ReferenceTerminalAdder newReferenceTerminal();

    List<ReferenceTerminal> getReferenceTerminals();

}
