/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.elements.areainterchange;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesControlArea {
    private final String id;
    private final String name;
    private final String energyIdentCodeEic;
    private final List<CgmesTieFlow> tieFlows = new ArrayList<>();
    private final double netInterchange;

    public CgmesControlArea(String id, String name, String energyIdentCodeEic, double netInterchange) {
        this.id = id;
        this.name = name;
        this.energyIdentCodeEic = energyIdentCodeEic;
        this.netInterchange = netInterchange;
    }

    public void addTieFLow(String tieFlowId, String tieFlowTerminal) {
        tieFlows.add(new CgmesTieFlow(tieFlowId, tieFlowTerminal));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEnergyIdentCodeEic() {
        return energyIdentCodeEic;
    }

    public List<CgmesTieFlow> getTieFlows() {
        return tieFlows;
    }

    public double getNetInterchange() {
        return netInterchange;
    }

}
