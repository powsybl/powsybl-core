/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.GeneratorTripping;
import com.powsybl.contingency.tasks.AbstractTrippingTask;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorContingency extends AbstractInjectionContingency {

    public GeneratorContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.GENERATOR;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new GeneratorTripping(id);
    }

}
