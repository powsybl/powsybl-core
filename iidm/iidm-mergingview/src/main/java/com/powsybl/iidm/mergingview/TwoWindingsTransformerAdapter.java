/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapter extends AbstractIdentifiableAdapter<TwoWindingsTransformer> implements TwoWindingsTransformer {

    protected TwoWindingsTransformerAdapter(final TwoWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public PhaseTapChangerAdapter getPhaseTapChanger() {
        return getIndex().getPhaseTapChanger(getDelegate().getPhaseTapChanger());
    }

    @Override
    public RatioTapChangerAdapter getRatioTapChanger() {
        return getIndex().getRatioTapChanger(getDelegate().getRatioTapChanger());
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter getTerminal2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter getTerminal(final Side side) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter getTerminal(final String voltageLevelId) {
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
    public RatioTapChangerAdderAdapter newRatioTapChanger() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter newPhaseTapChanger() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public SubstationAdapter getSubstation() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setRatedU1(final double ratedU1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setRatedU2(final double ratedU2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
