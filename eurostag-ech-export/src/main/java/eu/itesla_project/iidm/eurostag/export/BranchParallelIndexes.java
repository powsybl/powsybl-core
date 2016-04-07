/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import eu.itesla_project.iidm.network.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BranchParallelIndexes {

    private final Map<String, Character> parallelIndexes;

    public static BranchParallelIndexes build(Network network, EurostagEchExportConfig config) {
        Multimap<String, Identifiable> map = HashMultimap.create();
        for (TwoTerminalsConnectable ttc:  Iterables.concat(network.getLines(), network.getTwoWindingsTransformers())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(ttc.getTerminal1(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = ConnectionBus.fromTerminal(ttc.getTerminal2(), config, EchUtil.FAKE_NODE_NAME2);
            if (bus1.getId().compareTo(bus2.getId()) < 0) {
                map.put(bus1.getId() + bus2.getId(), ttc);
            } else {
                map.put(bus2.getId() + bus1.getId(), ttc);
            }
        }
        for (VoltageLevel vl : network.getVoltageLevels()) {
            for (Switch s : EchUtil.getSwitches(vl, config)) {
                Bus bus1 = EchUtil.getBus1(vl, s.getId(), config);
                Bus bus2 = EchUtil.getBus2(vl, s.getId(), config);
                if (bus1.getId().compareTo(bus2.getId()) < 0) {
                    map.put(bus1.getId() + bus2.getId(), s);
                } else {
                    map.put(bus2.getId() + bus1.getId(), s);
                }
            }
        }
        Map<String, Character> parallelIndexes = new HashMap<>();
        for (Map.Entry<String, Collection<Identifiable>> entry : map.asMap().entrySet()) {
            List<Identifiable> eqs = new ArrayList<>(entry.getValue());
            Collections.sort(eqs, (o1, o2) -> o1.getId().compareTo(o2.getId()));
            if (eqs.size() >= 2) {
                char index = '0';
                for (Identifiable l : eqs) {
                    index = incParallelIndex(index);
                    parallelIndexes.put(l.getId(), index);
                }
            }
        }
        return new BranchParallelIndexes(parallelIndexes);
    }

    private BranchParallelIndexes(Map<String, Character> parallelIndexes) {
        this.parallelIndexes = parallelIndexes;
    }

    private static char incParallelIndex(char index) {
        if (index == 'Z') {
            throw new RuntimeException("Number max of parallel index reached");
        }
        if (index == '9') {
            return 'A';
        } else {
            return ++index;
        }
    }

    public char getParallelIndex(String iidmId) {
        Character index = parallelIndexes.get(iidmId);
        if (index == null) {
            return '1';
        }
        return index;
    }

    public Map<String, Character> toMap() {
        return parallelIndexes;
    }
}
