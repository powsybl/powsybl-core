package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Terminal;

public class TerminalNodeBreakerViewAdapter extends AbstractAdapter<Terminal.NodeBreakerView> implements Terminal.NodeBreakerView {

    protected TerminalNodeBreakerViewAdapter(Terminal.NodeBreakerView delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public int getNode() {
        return getDelegate().getNode();
    }
}
