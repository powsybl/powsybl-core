/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;

public class Z0BusTree {

    private String root;
    private List<Z0BusTreeNode> treeNodes;
    private List<Z0BusTreeLevel> treeLevels;

    public Z0BusTree() {
        root = "";
        treeNodes = new ArrayList<Z0BusTreeNode>();
        treeLevels = new ArrayList<Z0BusTreeLevel>();
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Z0BusTreeNode getTreeNode(int pos) {
        return treeNodes.get(pos);
    }

    public void addTreeNode(Z0BusTreeNode treeNode) {
        this.treeNodes.add(treeNode);
    }

    public Z0BusTreeLevel getTreeLevel(int pos) {
        return treeLevels.get(pos);
    }

    public void addTreeLevel(Z0BusTreeLevel treeLevel) {
        this.treeLevels.add(treeLevel);
    }

    public int treeNodeSize() {
        return treeNodes.size();
    }

    public int treeLevelSize() {
        return treeLevels.size();
    }

    public boolean contains(Bus bus) {
        return treeNodes.stream().anyMatch(treeNode -> treeNode.getBus().equals(bus));
    }

    public void print() {
        LOG.info("root " + root);
        treeNodes.forEach(n -> n.print());
        treeLevels.forEach(l -> l.print());
    }

    private static final Logger LOG = LoggerFactory.getLogger(Z0BusTree.class);
}
