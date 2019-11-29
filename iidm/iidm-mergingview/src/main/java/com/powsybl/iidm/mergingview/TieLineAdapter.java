/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends AbstractIdentifiableAdapter<Line> implements TieLine {

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public boolean isTieLine() {
        return getDelegate().isTieLine();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public Terminal getTerminal1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Terminal getTerminal2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Terminal getTerminal(final Side side) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Terminal getTerminal(final String voltageLevelId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Side getSide(final Terminal terminal) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimits getCurrentLimits(final Side side) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isOverloaded() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isOverloaded(final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getOverloadDuration() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean checkPermanentLimit(final Side side, final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean checkPermanentLimit(final Side side) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean checkPermanentLimit1(final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean checkPermanentLimit1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean checkPermanentLimit2(final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean checkPermanentLimit2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Overload checkTemporaryLimits(final Side side) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Overload checkTemporaryLimits1(final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Overload checkTemporaryLimits1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Overload checkTemporaryLimits2(final float limitReduction) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Overload checkTemporaryLimits2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line setG1(final double g1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line setG2(final double g2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line setB1(final double b1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line setB2(final double b2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public String getUcteXnodeCode() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HalfLine getHalf1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HalfLine getHalf2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
