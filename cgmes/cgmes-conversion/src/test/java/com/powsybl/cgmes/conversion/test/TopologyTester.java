/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TopologyTester {

    public TopologyTester(CgmesModel cgmes, Network n) {
        this.cgmes = cgmes;
        this.network = n;
    }

    // TODO a topologicalNode contains connectivity nodes linked by non-retained closed switches
    // For current validation we are not taking into account the retained flag
    // When we create the voltage-level connectivity at node-breaker level we will be able to
    // set the retained flag for each switch and this problem will be avoided
    // For connectivity created at bus-breaker level we can not set the "retained" flag
    public boolean test(boolean strict) {
        // Only makes sense if the network has been obtained
        // from CGMES node-breaker detailed data
        if (!network.getProperties().getProperty(Conversion.NETWORK_PS_CGMES_MODEL_DETAIL)
                .equals(Conversion.NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER)) {
            return true;
        }
        Map<String, Set<String>> tpcns = new HashMap<>();
        Map<String, String> cn2tp = new HashMap<>();

        LOG.info("testTopology (strict : {})", strict);
        LOG.info("    preparing connectivityNodes - topologicalNodes ...");
        PropertyBags cgmescn = cgmes.connectivityNodes();
        LOG.info("    query for connectivityNodes completed");
        cgmescn.forEach(cnp -> {
            String cn = cnp.getId("ConnectivityNode");
            String tp = cnp.getId("TopologicalNode");
            tpcns.computeIfAbsent(tp, x -> new HashSet<>()).add(cn);
            cn2tp.put(cn, tp);
        });
        LOG.info("    completed connectivityNodes - topologicalNodes");

        LOG.info("    preparing configuredBuses - mergedBuses ...");
        Map<String, Set<String>> mbcbs = new HashMap<>();
        Map<String, String> cb2mb = new HashMap<>();
        network.getBusBreakerView().getBusStream().forEach(b -> {
            String cb = b.getId();
            Bus mbb = b.getVoltageLevel().getBusView().getMergedBus(cb);
            if (mbb != null) {
                String mb = mbb.getId();
                mbcbs.computeIfAbsent(mb, x -> new HashSet<>()).add(cb);
                cb2mb.put(cb, mb);
            }
        });
        LOG.info("    completed configuredBuses - mergedBuses");

        // Review all connectivity nodes present in CGMES model
        int numNodes = 0;
        int numFails = 0;
        Set<String> badTPs = new HashSet<>();
        Iterator<Map.Entry<String, String>> k = cn2tp.entrySet().iterator();
        LOG.info("    analyzing all connectivityNodes ...");
        while (k.hasNext()) {
            Map.Entry<String, String> cntp = k.next();
            String cn = cntp.getKey();
            String tp = cntp.getValue();
            Set<String> cns = tpcns.get(tp);

            String cb = cn;
            String mb = cb2mb.get(cb);
            Set<String> cbs = mbcbs.get(mb);

            numNodes++;
            if (!cns.equals(cbs)) {
                if (strict) {
                    reportTopologyError(cn, cns, cbs);
                } else {
                    // All connectivity nodes in the same topological node
                    // that are not in the set of configured buses must be invalid
                    // (they should not have a merged bus)
                    Set<String> cbs1 = new HashSet<>(cns);
                    if (cbs != null) {
                        cbs1.removeAll(cbs);
                    }
                    cbs1 = cbs1.stream()
                            .filter(cb1 -> cb2mb.get(cb1) != null)
                            .collect(Collectors.toSet());
                    if (!cbs1.isEmpty()) {
                        badTPs.add(tp);
                        if (cbs != null) {
                            reportTopologyError(cn, cns, cbs, cbs1);
                            numFails++;
                        } else {
                            reportTopologyWarning(cn, cns, cbs1);
                        }
                    }
                }
            }
        }
        LOG.info("    completed analyzing all connectivityNodes");
        if (numFails > 0) {
            String reason = String.format(
                    "testTopology. Failed %d of %d connectivityNodes analyzed",
                    numFails,
                    numNodes);
            LOG.error(reason);
            return false;
        }
        if (!badTPs.isEmpty()) {
            LOG.warn("    Bad topologicalNodes : {} / {}", badTPs.size(), tpcns.keySet().size());
            badTPs.forEach(tp -> LOG.warn("        {}", tp));
        }
        LOG.info("testTopology completed");
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
