/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Line;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
@AutoService(ExtensionAdderProvider.class)
public class MergedXnodeAdderImplProvider implements ExtensionAdderProvider<Line, MergedXnode, MergedXnodeAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionsName() {
        return MergedXnode.NAME;
    }

    @Override
    public Class<MergedXnodeAdderImpl> getAdderClass() {
        return MergedXnodeAdderImpl.class;
    }

    @Override
    public MergedXnodeAdderImpl newAdder(Line extendable) {
        return new MergedXnodeAdderImpl(extendable);
    }
}
