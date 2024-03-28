/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class TopologyTester {

    TopologyTester(CgmesModel cgmes, Network n) {
        this.cgmes = cgmes;
        this.network = n;
    }

    // TODO a topologicalNode contains connectivity nodes linked by non-retained
    // closed switches
    // For current validation we are not taking into account the retained flag
    // When we create the voltage-level connectivity at node-breaker level we will
    // be able to
    // set the retained flag for each switch and this problem will be avoided
    // For connectivity created at bus-breaker level we can not set the "retained"
    // flag
    boolean test(boolean strict) {
        // Only makes sense if the network has been obtained
        // from CGMES node-breaker detailed data
        if (!network.getProperty(Conversion.NETWORK_PS_CGMES_MODEL_DETAIL)
                .equals(Conversion.NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER)) {
            return true;
        }
        Map<String, Set<String>> tpcns = new HashMap<>();
        Map<String, String> cn2tp = new HashMap<>();

        LOG.info("testTopology (strict : {})", strict);
        LOG.info("    preparing mapping between CGMES connectivityNodes and topologicalNodes ...");
        PropertyBags cgmescn = cgmes.connectivityNodes();
        Set<String> boundarycn = cgmes.boundaryNodes().stream()
            .map(bnp -> bnp.getId("ConnectivityNode"))
            .collect(Collectors.toSet());
        cgmescn.forEach(cnp -> {
            String cn = cnp.getId("ConnectivityNode");
            // Ignore connectivity nodes belonging to boundaries
            if (!boundarycn.contains(cn)) {
                String tp = cnp.getId("TopologicalNode");
                tpcns.computeIfAbsent(tp, x -> new HashSet<>()).add(cn);
                cn2tp.put(cn, tp);
            }
        });

        LOG.info("    preparing mapping between IIDM busbarSections and busBreaker buses ...");
        Map<String, Set<String>> mbbbss = new HashMap<>();
        Map<String, String> bbs2mb = new HashMap<>();
        network.getVoltageLevels().forEach(vl -> {
            vl.getNodeBreakerView().getBusbarSections().forEach(bbs -> {
                Bus b = bbs.getTerminal().getBusBreakerView().getBus();
                if (b != null) {
                    mbbbss.computeIfAbsent(b.getId(), x -> new HashSet<>()).add(bbs.getId());
                    bbs2mb.put(bbs.getId(), b.getId());
                }
            });
        });

        // Review all topological nodes present in CGMES model
        int numNodes = 0;
        int numFails = 0;
        int numWarnings = 0;
        Set<String> badTPs = new HashSet<>();
        Iterator<Map.Entry<String, Set<String>>> k = tpcns.entrySet().iterator();
        LOG.info("    analyzing all CGMES topologicalNodes ...");
        while (k.hasNext()) {
            Map.Entry<String, Set<String>> e = k.next();
            String tp = e.getKey();
            Set<String> cns = e.getValue();
            String cn = cns.iterator().next();

            // For the topology test to be valid,
            // BusbarSections should have been created in IIDM
            // for each connectivity node
            String bbs = cn;

            String mb = bbs2mb.get(bbs);
            Set<String> bbss = mbbbss.get(mb);
            boolean hasBbss = bbss != null;

            if (LOG.isInfoEnabled()) {
                LOG.info("    analyzing topologicalNode {}", tp);
                LOG.info("        connectivityNodes in same CGMES topologicalNode {} {} {}",
                        cns.size(),
                        Arrays.toString(cns.toArray()),
                        tp);
                if (hasBbss) {
                    LOG.info("        busbarSections in same IIDM mergedBus           {} {} {}",
                            bbss.size(),
                            Arrays.toString(bbss.toArray()),
                            mb);
                }
            }

            numNodes++;
            if (!cns.equals(bbss)) {
                if (strict) {
                    reportTopologyError(cn, cns, bbss);
                    numFails++;
                } else {
                    // All connectivity nodes in the same topological node
                    // that are not in the set of bus bar sections must be invalid
                    // (they should not have a merged bus)
                    Set<String> cns1 = new HashSet<>(cns);
                    if (hasBbss) {
                        cns1.removeAll(bbss);
                    }
                    cns1 = cns1.stream()
                            .filter(cn1 -> bbs2mb.get(cn1) != null)
                            .collect(Collectors.toSet());
                    if (!cns1.isEmpty()) {
                        badTPs.add(tp);
                        if (hasBbss) {
                            reportTopologyError(cn, cns, bbss, cns1);
                            numFails++;
                        } else {
                            reportTopologyWarning(cn, cns, cns1);
                            numWarnings++;
                        }
                    }
                }
            }
        }
        LOG.info("    completed analyzing all {} topologicalNodes", numNodes);
        if (!badTPs.isEmpty()) {
            LOG.warn("    bad topologicalNodes : {} / {}", badTPs.size(), numNodes);
            badTPs.forEach(tp -> LOG.warn("        {}", tp));
        }
        if (numFails > 0 || numWarnings > 0) {
            String reason = String.format(
                    "testTopology. Failed %d, warnings %d of %d topologicalNodes analyzed",
                    numFails,
                    numWarnings,
                    numNodes);
            LOG.error(reason);
            return false;
        }
        return true;
    }

    private void reportTopologyError(String cn, Set<String> cns, Set<String> cbs) {
        LOG.error("    Fail, connectivityNode {}", cn);
        if (LOG.isDebugEnabled()) {
            LOG.debug("        cns  : {}", cns);
            LOG.debug("        cbs  : {}", cbs);
        }
    }

    private void reportTopologyWarning(String cn, Set<String> cns, Set<String> cbs1) {
        LOG.warn("    TopologicalNode contains invalid connectivityNodes, connectivityNode {}", cn);
        if (LOG.isDebugEnabled()) {
            LOG.debug("        cns  : {}", cns);
            LOG.debug("        cbs1 : {}", cbs1);
        }
    }

    private void reportTopologyError(String cn, Set<String> cns, Set<String> cbs, Set<String> cbs1) {
        LOG.error("    Fail after removing invalid connectivityNodes, connectivityNode {}", cn);
        if (LOG.isDebugEnabled()) {
            LOG.error("        cns  : {}", cns);
            LOG.error("        cbs  : {}", cbs);
            LOG.error("        cbs1 : {}", cbs1);
        }
    }

    private final CgmesModel cgmes;
    private final Network network;

    private static final Logger LOG = LoggerFactory.getLogger(TopologyTester.class);
}
