/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.model.PowerFlow;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class BoundaryLine {

    BoundaryLine(String id, String name, String modelIidmVoltageLevelId, String modelBus,
        boolean modelTconnected, int modelNode, String modelTerminalId, String boundaryTerminalId,
        PowerFlow modelPowerFlow) {
        this.id = id;
        this.name = name;
        this.modelIidmVoltageLevelId = modelIidmVoltageLevelId;
        this.modelBus = modelBus;
        this.modelTconnected = modelTconnected;
        this.modelNode = modelNode;
        this.modelTerminalId = modelTerminalId;
        this.boundaryTerminalId = boundaryTerminalId;
        this.modelPowerFlow = modelPowerFlow;
        r = 0.0;
        x = 0.0;
        g1 = 0.0;
        b1 = 0.0;
        g2 = 0.0;
        b2 = 0.0;
    }

    public void setParameters(double r, double x, double g1, double b1, double g2, double b2) {
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
    }

    public String getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getModelIidmVoltageLevelId() {
        return modelIidmVoltageLevelId;
    }

    String getModelBus() {
        return modelBus;
    }

    boolean isModelTconnected() {
        return modelTconnected;
    }

    int getModelNode() {
        return modelNode;
    }

    String getModelTerminalId() {
        return modelTerminalId;
    }

    String getBoundaryTerminalId() {
        return boundaryTerminalId;
    }

    double getR() {
        return r;
    }

    double getX() {
        return x;
    }

    double getG1() {
        return g1;
    }

    double getB1() {
        return b1;
    }

    double getG2() {
        return g2;
    }

    double getB2() {
        return b2;
    }

    PowerFlow getModelPowerFlow() {
        return modelPowerFlow;
    }

    private final String id;
    private final String name;
    private final String modelIidmVoltageLevelId;
    private final String modelBus;
    private final boolean modelTconnected;
    private final int modelNode;
    private final String modelTerminalId;
    private final String boundaryTerminalId;
    private double r;
    private double x;
    private double g1;
    private double b1;
    private double g2;
    private double b2;
    private final PowerFlow modelPowerFlow;
}
