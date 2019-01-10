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

public class Z0BusTreeLevel {

    private List<Integer> idOfTreeNodes;

    public Z0BusTreeLevel() {
        idOfTreeNodes = new ArrayList<Integer>();
    }

    public void addIdOfTreeNodes(int idOfTreeNodes) {
        this.idOfTreeNodes.add(idOfTreeNodes);
    }

    public List<Integer> getIdOfTreeNodes() {
        return idOfTreeNodes;
    }

    public void print() {
        LOG.info("tree nodes " + idOfTreeNodes.stream().map(i -> "" + i).collect(Collectors.joining(",")));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Z0BusTreeLevel.class);
}
