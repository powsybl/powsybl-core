/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteElement implements UcteRecord {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteElement.class);

    private final UcteElementId id;
    private UcteElementStatus status;
    private float resistance;
    private float reactance;
    private float susceptance;
    private Integer currentLimit; // FIXME: should not be null
    private String elementName;

    protected UcteElement(UcteElementId id, UcteElementStatus status, float resistance, float reactance, float susceptance, Integer currentLimit, String elementName) {
        this.id = Objects.requireNonNull(id);
        this.status = Objects.requireNonNull(status);
        this.resistance = resistance;
        this.reactance = reactance;
        this.susceptance = susceptance;
        this.currentLimit = currentLimit;
        this.elementName = elementName;
    }

    /**
     * Gets element id.
     * @return element id
     */
    public UcteElementId getId() {
        return id;
    }

    /**
     * Gets element status.
     * @return element status
     */
    public UcteElementStatus getStatus() {
        return status;
    }

    /**
     * Sets element status.
     * @param status element status
     */
    public void setStatus(UcteElementStatus status) {
        this.status = status;
    }

    /**
     * Gets resistance R (Ω).
     * @return resistance R (Ω)
     */
    public float getResistance() {
        return resistance;
    }

    /**
     * Sets resistance R (Ω).
     * @param resistance resistance R (Ω)
     */
    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    /**
     * Gets reactance X (Ω).
     * <p>The absolute value of the reactance for lines has to be greater than or
     * equal to 0.050 Ω (to avoid division by values near to zero in load flow calculation)
     * @return reactance X (Ω)
     */
    public float getReactance() {
        return reactance;
    }

    /**
     * Sets reactance X (Ω).
     * <p>The absolute value of the reactance for lines has to be greater than or
     * equal to 0.050 Ω (to avoid division by values near to zero in load flow calculation)
     * @param reactance reactance X (Ω)
     */
    public void setReactance(float reactance) {
        this.reactance = reactance;
    }

    /**
     * Gets susceptance B (μS).
     * @return susceptance B (μS)
     */
    public float getSusceptance() {
        return susceptance;
    }

    /**
     * Sets susceptance B (μS).
     * @param susceptance susceptance B (μS)
     */
    public void setSusceptance(float susceptance) {
        this.susceptance = susceptance;
    }

    /**
     * Gets current limit Ι (A).
     * @return current limit Ι (A)
     */
    public Integer getCurrentLimit() {
        return currentLimit;
    }

    /**
     * Sets current limit Ι (A).
     * @param currentLimit current limit Ι (A)
     */
    public void setCurrentLimit(Integer currentLimit) {
        this.currentLimit = currentLimit;
    }

    /**
     * Gets element name (optional).
     * @return element name
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Sets element name (optional).
     * @param elementName element name
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    private static final float MIN_X = 0.05f;

    @Override
    public void fix() {
        switch (status) {
            case EQUIVALENT_ELEMENT_IN_OPERATION:
            case EQUIVALENT_ELEMENT_OUT_OF_OPERATION:
            case REAL_ELEMENT_IN_OPERATION:
            case REAL_ELEMENT_OUT_OF_OPERATION:
                if (Math.abs(reactance) < MIN_X) {
                    float oldReactance = reactance;
                    reactance = reactance >= 0 ? MIN_X : -MIN_X;
                    LOGGER.warn("Small reactance {} of element '{}' fixed to {}", oldReactance, id, reactance);
                }
                break;
            case BUSBAR_COUPLER_IN_OPERATION:
            case BUSBAR_COUPLER_OUT_OF_OPERATION:
                // nothing to do
                break;
            default:
                throw new AssertionError("Unexpected UcteElementStatus value: " + status);
        }
        if (currentLimit == null) {
            LOGGER.warn("Missing current limit for element '{}'", id);
        } else if (currentLimit <= 0) {
            LOGGER.warn("Invalid current limit {} for element '{}'", currentLimit, id);
            currentLimit = null;
        }
    }

    @Override
    public String toString() {
        return id.toString();
    }

}
