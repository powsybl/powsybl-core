/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;

public class Z0BusTreeNode {

    private int idOfTreeParent;
    private String idBranchFromParent;
    private Bus bus;
    private int level;
    private List<Integer> idOfTreeChildren;

    public Z0BusTreeNode() {
        idOfTreeParent = -1;
        idBranchFromParent = "";
        level = -1;
        idOfTreeChildren = new ArrayList<Integer>();
    }

    public int getIdOfTreeParent() {
        return idOfTreeParent;
    }

    public void setIdOfTreeParent(int idOfTreeParent) {
        this.idOfTreeParent = idOfTreeParent;
    }

    public String getIdBranchFromParent() {
        return idBranchFromParent;
    }

    public void setIdBranchFromParent(String idBranchFromParent) {
        this.idBranchFromParent = idBranchFromParent;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addIdOfTreeChildren(int idOfTreeChildren) {
        this.idOfTreeChildren.add(idOfTreeChildren);
    }

    public void print() {
        LOG.info("idOfTreeParent " + idOfTreeParent);
        LOG.info("idBranchFromParent " + idBranchFromParent);
        LOG.info("bus " + bus.toString());
        LOG.info("level " + level);
        LOG.info("children " + idOfTreeChildren.stream().map(i -> "" + i).collect(Collectors.joining(",")));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Z0BusTreeNode.class);
}
