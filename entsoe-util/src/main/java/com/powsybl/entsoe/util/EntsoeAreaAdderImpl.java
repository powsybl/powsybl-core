/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Substation;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class EntsoeAreaAdderImpl extends AbstractExtensionAdder<Substation, EntsoeArea>
        implements EntsoeAreaAdder {

    private EntsoeGeographicalCode code;

    public EntsoeAreaAdderImpl(Substation extendable) {
        super(extendable);
    }

    @Override
    protected EntsoeArea createExtension(Substation extendable) {
        return new EntsoeAreaImpl(extendable, code);
    }

    @Override
    public EntsoeAreaAdder withCode(EntsoeGeographicalCode code) {
        this.code = code;
        return this;
    }

}
