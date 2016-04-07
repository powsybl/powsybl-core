/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import org.joda.time.LocalDate;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgGeneralParameters {

    private static final int DEFAULT_MAX_NUM_ITERATION = 20;
    private static final float DEFAULT_TOLERANCE = 0.005f;
    private static final StartMode DEFAULT_START_MODE = StartMode.FLAT_START;
    private static final float DEFAULT_SNREF = 100.f;
    private static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL = false;
    private static final boolean DEFAULT_SVC_VOLTAGE_CONTROL = false;
    private static final LocalDate DEFAULT_EDIT_DATE = LocalDate.now();

    public enum StartMode {
        FLAT_START,
        WARM_START
    }

    /**
     * In interactive mode, if the maximum number of iterations is reached
     * before calculation convergence, the user will be requested the number
     * of additional iterations he requires.
     * In batch mode, the computation stops when the maximum number of
     * iterations is reached.
     */
    private int maxNumIteration;

    /**
     * max. permitted tolerance on mismatches in p.u.
     */
    private float tolerance;

    /**
     * Eurostag initialization voltage
     */
    private StartMode startMode;

    /**
     * Base power as reference for the Network
     */
    private float snref;

    private boolean transformerVoltageControl;

    private boolean svcVoltageControl;

    private LocalDate editDate;

    public EsgGeneralParameters(int maxNumIteration, float snref, StartMode startMode, boolean svcVoltageControl, float tolerance, boolean transformerVoltageControl, LocalDate editDate) {
        this.maxNumIteration = maxNumIteration;
        this.snref = snref;
        this.startMode = Objects.requireNonNull(startMode);
        this.svcVoltageControl = svcVoltageControl;
        this.tolerance = tolerance;
        this.transformerVoltageControl = transformerVoltageControl;
        this.editDate = Objects.requireNonNull(editDate);
    }

    public EsgGeneralParameters() {
        this(DEFAULT_MAX_NUM_ITERATION, DEFAULT_SNREF, DEFAULT_START_MODE, DEFAULT_SVC_VOLTAGE_CONTROL, DEFAULT_TOLERANCE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL, DEFAULT_EDIT_DATE);
    }

    public int getMaxNumIteration() {
        return maxNumIteration;
    }

    public void setMaxNumIteration(int maxNumIteration) {
        this.maxNumIteration = maxNumIteration;
    }

    public float getTolerance() {
        return tolerance;
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }

    public StartMode getStartMode() {
        return startMode;
    }

    public void setStartMode(StartMode startMode) {
        this.startMode = startMode;
    }

    public float getSnref() {
        return snref;
    }

    public void setSnref(float snref) {
        this.snref = snref;
    }

    public boolean isTransformerVoltageControl() {
        return transformerVoltageControl;
    }

    public void setTransformerVoltageControl(boolean transformerVoltageControl) {
        this.transformerVoltageControl = transformerVoltageControl;
    }

    public boolean isSvcVoltageControl() {
        return svcVoltageControl;
    }

    public void setSvcVoltageControl(boolean svcVoltageControl) {
        this.svcVoltageControl = svcVoltageControl;
    }

    public LocalDate getEditDate() {
        return editDate;
    }

    public void setEditDate(LocalDate editDate) {
        this.editDate = Objects.requireNonNull(editDate);
    }
}
