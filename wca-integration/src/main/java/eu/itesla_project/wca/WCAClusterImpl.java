/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.modules.wca.WCACluster;
import eu.itesla_project.modules.wca.WCAClusterNum;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class WCAClusterImpl implements WCACluster {

    private final Contingency contingency;

    private final WCAClusterNum num;

    private final WCAClusterOrigin origin;

    private final List<String> causes;

    WCAClusterImpl(Contingency contingency, WCAClusterNum num, WCAClusterOrigin origin, List<String> causes) {
        this.contingency = Objects.requireNonNull(contingency);
        this.num =  Objects.requireNonNull(num);
        this.origin = Objects.requireNonNull(origin);
        this.causes = causes;
    }

    @Override
    public Contingency getContingency() {
        return contingency;
    }

    @Override
    public WCAClusterNum getNum() {
        return num;
    }

    @Override
    public String getOrigin() {
        return origin.toString();
    }

    @Override
    public List<String> getCauses() {
        return causes;
    }
}
