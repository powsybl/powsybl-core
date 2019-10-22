package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevel.BusBreakerView.SwitchAdder;

public class VoltageLevelBusBreakerViewSwitchAdderAdapter extends AbstractAdapter<VoltageLevel.BusBreakerView.SwitchAdder> implements VoltageLevel.BusBreakerView.SwitchAdder {

    protected VoltageLevelBusBreakerViewSwitchAdderAdapter(final SwitchAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setBus1(final String bus1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setBus2(final String bus2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setOpen(final boolean open) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelBusBreakerViewSwitchAdderAdapter setFictitious(final boolean fictitious) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public SwitchAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
