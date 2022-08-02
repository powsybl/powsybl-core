/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Injection;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public interface InjectionDiagramDataAdder<I extends Injection<I>> extends ExtensionAdder<I, InjectionDiagramData<I>> {

    InjectionDiagramDataAdder<I> addDiagramDetails(DiagramPoint point, double rotation);

    @Override
    default Class<InjectionDiagramData> getExtensionClass() {
        return InjectionDiagramData.class;
    }
}
