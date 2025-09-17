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
    @Override
    public boolean isFreeFluxes() {
        return freeFluxes;
    }

    @Override
    public void setFreeFluxes(boolean freeFluxes) {
        this.freeFluxes = freeFluxes;
    }

    /**
     * The zero sequence resistance of the leg.
     */
    @Override
    public double getRz() {
        return rz;
    }

    @Override
    public void setRz(double rz) {
        this.rz = rz;
    }

    /**
     * The zero sequence reactance of the leg.
     */
    @Override
    public double getXz() {
        return xz;
    }

    @Override
    public void setXz(double xz) {
        this.xz = xz;
    }

    /**
     * Get the winding connection type of the leg, see {@link WindingConnectionType}).
     */
    @Override
    public WindingConnectionType getConnectionType() {
        return connectionType;
    }

    @Override
    public void setConnectionType(WindingConnectionType connectionType) {
        this.connectionType = Objects.requireNonNull(connectionType);
    }

    /**
     * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
     * resistance part of the impedance to ground.
     */
    @Override
    public double getGroundingR() {
        return groundingR;
    }

    @Override
    public void setGroundingR(double groundingR) {
        this.groundingR = groundingR;
    }

    /**
     * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
     * reactance part of the impedance to ground.
     */
    @Override
    public double getGroundingX() {
        return groundingX;
    }

    @Override
    public void setGroundingX(double groundingX) {
        this.groundingX = groundingX;
    }
}
