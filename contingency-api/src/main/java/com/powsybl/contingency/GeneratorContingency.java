/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.GeneratorTripping;
import com.powsybl.contingency.tasks.AbstractTrippingTask;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorContingency implements ContingencyElement {

    private final String id;

    public GeneratorContingency(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.GENERATOR;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new GeneratorTripping(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GeneratorContingency) {
            GeneratorContingency other = (GeneratorContingency) obj;
            return id.equals(other.id);
        }
        return false;
    }
}
