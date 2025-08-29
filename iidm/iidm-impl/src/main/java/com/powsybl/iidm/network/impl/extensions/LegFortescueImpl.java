package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.LegFortescue;
import com.powsybl.iidm.network.extensions.WindingConnectionType;

import java.util.Objects;

class LegFortescueImpl implements LegFortescue {

    private double rz;
    private double xz;
    private boolean freeFluxes;
    private WindingConnectionType connectionType;
    private double groundingR;
    private double groundingX;

    public LegFortescueImpl(double rz, double xz, boolean freeFluxes, WindingConnectionType connectionType,
                        double groundingR, double groundingX) {
        this.rz = rz;
        this.xz = xz;
        this.freeFluxes = freeFluxes;
        this.connectionType = Objects.requireNonNull(connectionType);
        this.groundingR = groundingR;
        this.groundingX = groundingX;
    }

    /**
     * Free fluxes set to true means that the magnetizing impedance Zm is infinite, i.e. fluxes are free.
     */
    public boolean isFreeFluxes() {
        return freeFluxes;
    }

    public void setFreeFluxes(boolean freeFluxes) {
        this.freeFluxes = freeFluxes;
    }

    /**
     * The zero sequence resistance of the leg.
     */
    public double getRz() {
        return rz;
    }

    public void setRz(double rz) {
        this.rz = rz;
    }

    /**
     * The zero sequence reactance of the leg.
     */
    public double getXz() {
        return xz;
    }

    public void setXz(double xz) {
        this.xz = xz;
    }

    /**
     * Get the winding connection type of the leg, see {@link WindingConnectionType}).
     */
    public WindingConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(WindingConnectionType connectionType) {
        this.connectionType = Objects.requireNonNull(connectionType);
    }

    /**
     * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
     * resistance part of the impedance to ground.
     */
    public double getGroundingR() {
        return groundingR;
    }

    public void setGroundingR(double groundingR) {
        this.groundingR = groundingR;
    }

    /**
     * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
     * reactance part of the impedance to ground.
     */
    public double getGroundingX() {
        return groundingX;
    }

    public void setGroundingX(double groundingX) {
        this.groundingX = groundingX;
    }
}
