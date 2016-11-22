/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.tools;

import java.util.ArrayList;

import eu.itesla_project.eurostag.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.eurostag.EurostagStabilization;
import eu.itesla_project.eurostag.network.EsgDetailedTwoWindingTransformer.RegulatingMode;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdExportConfig;

/**
 * @author Nicolas GROROD <nicolas.grorod at rte-france.com>
 */
public class EurostagNetworkModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagStabilization.class);

    private final DdExportConfig configExport;

    public EurostagNetworkModifier() {
        this.configExport = DdExportConfig.load();
    }

    // Adds tap-changer transformers before hv loads
    public void hvLoadModelling(EsgNetwork networkEch) {
        if (configExport.getLVLoadModeling()) {
            ArrayList<String> concernedLoads = new ArrayList<>();
            for (EsgLoad load : networkEch.getLoads()) {
                Esg8charName nodeName = load.getZnodlo();
                Esg8charName loadName = load.getZnamlo();
                if (nodeName == null || "".equals(nodeName.toString())) {
                    LOGGER.error("Unconnected load in Esg network :" + loadName.toString());
                } else {
                    EsgNode esgNode = networkEch.getNode(nodeName.toString());
                    if (esgNode.getVbase() <= 100) { // Only HV loads
                        concernedLoads.add(loadName.toString());
                    }
                }
            }
            for (String load : concernedLoads) {
                EsgLoad esgLoad = networkEch.getLoad(load);
                Esg8charName nodeName = esgLoad.getZnodlo();
                Esg8charName loadName = esgLoad.getZnamlo();
                float Pi = esgLoad.getPldstp();
                float Qi = esgLoad.getQldstp();

                // Tfo parameters
                float rate = (float) Math.max(1.0F, 1.5 * Math.sqrt(new Float(Pi * Pi + Qi * Qi).doubleValue()));
                float vBase = 20.0F;
                float ucc = 1.0F;
                float vB = 1.14F;
                float vT = 0.86F;
                float pcu = 0.0F;
                float cmagn = 0.0F;
                float pfer = 0.0F;
                float esat = 1.0F;
                float voltr = vBase;
                int firstPlot = 1;
                int lastPlot = 15;

                // Choose the right initial tap because the load flow doesn't do it
                EsgNode esgNode = networkEch.getNode(nodeName.toString());
                float vCurHV = esgNode.getVinit();
                int ktap8 = Math.round(firstPlot + (lastPlot - firstPlot) / (vB - vT) * (vCurHV - vT));
                ktap8 = Math.max(firstPlot, ktap8);
                ktap8 = Math.min(lastPlot, ktap8);
                int ktpnom = 8;

                // Node parameters
                float vInit = 1.015F;

                // Load parameters
                float Pf = Pi;
                float Qf = Qi - ucc / 100 * (Pi * Pi + Qi * Qi) / rate * 1 / (vInit * vInit);

                Esg8charName lvNodeName = new Esg8charName(loadName.toString().toUpperCase()); // Upper Case for Eurostag 5.1.1
                networkEch.addNode(new EsgNode(esgNode.getArea(), lvNodeName, vBase, vInit, 0, false));
                EsgBranchName transfoName = new EsgBranchName(nodeName, lvNodeName, '1');
                EsgDetailedTwoWindingTransformer newTranfo = new EsgDetailedTwoWindingTransformer(transfoName,
                        EsgBranchConnectionStatus.CLOSED_AT_BOTH_SIDE, cmagn,
                        rate, pcu, pfer, esat, ktpnom, ktap8, lvNodeName,
                        voltr, Float.NaN, Float.NaN, RegulatingMode.VOLTAGE);
                newTranfo.getTaps().add(new EsgDetailedTwoWindingTransformer.Tap(firstPlot, 0.0F, esgNode.getVbase() * vB, vBase, ucc));
                newTranfo.getTaps().add(new EsgDetailedTwoWindingTransformer.Tap(lastPlot, 0.0F, esgNode.getVbase() * vT, vBase, ucc));
                networkEch.addDetailedTwoWindingTransformer(newTranfo);

                networkEch.removeLoad(load);
                networkEch.addLoad(new EsgLoad(EsgConnectionStatus.CONNECTED, loadName, lvNodeName, esgLoad.getPldsti(),
                        esgLoad.getPldstz(), Pf, esgLoad.getQldsti(), esgLoad.getQldstz(), Qf));
            }
        }
    }

}
