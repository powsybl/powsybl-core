/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.NodeRef;
import com.powsybl.powerfactory.model.DataObject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractConverter {

    private final ImportContext importContext;

    private final Network network;

    AbstractConverter(ImportContext importContext, Network network) {
        this.importContext = Objects.requireNonNull(importContext);
        this.network = Objects.requireNonNull(network);
    }

    Network getNetwork() {
        return network;
    }

    static double microFaradToSiemens(double frnom, double capacitance) {
        return 2 * Math.PI * frnom * capacitance * 1.0e-6;
    }

    static double microSiemensToSiemens(double susceptance) {
        return susceptance * 1.0e-6;
    }

    static double impedanceFromPerUnitToEngineeringUnits(double impedance, double vnom, double sbase) {
        return impedance * vnom * vnom / sbase;
    }

    static double admittanceFromPerUnitToEngineeringUnits(double admitance, double vnom, double sbase) {
        return admitance * sbase / (vnom * vnom);
    }

    List<NodeRef> checkNodes(DataObject obj, int connections) {
        List<NodeRef> nodeRefs = importContext.objIdToNode.get(obj.getId());
        if (nodeRefs == null || nodeRefs.size() != connections) {
            throw new PowsyblException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connections for '" + obj + "'");
        }
        return nodeRefs.stream().sorted(Comparator.comparing(nodoref -> nodoref.busIndexIn)).collect(Collectors.toList());
    }
}
