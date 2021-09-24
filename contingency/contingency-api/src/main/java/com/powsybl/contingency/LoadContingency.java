/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.LoadTripping;
import com.powsybl.contingency.tasks.AbstractTrippingTask;

/**
 * @author Hadrien Godard <hadrien.godard at artelys.com>
 */
public class LoadContingency extends AbstractInjectionContingency {

    public LoadContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.LOAD;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new LoadTripping(id);
    }

}
