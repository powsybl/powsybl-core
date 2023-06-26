package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

public class LineSeries extends AbstractBranchSeries {

    private final DoubleSeries b1;
    private final DoubleSeries b2;
    private final DoubleSeries g1;
    private final DoubleSeries g2;
    private final DoubleSeries r;
    private final DoubleSeries x;

    LineSeries(UpdatingDataframe dataframe) {
        super(dataframe);
        this.b1 = dataframe.getDoubles("b1");
        this.b2 = dataframe.getDoubles("b2");
        this.g1 = dataframe.getDoubles("g1");
        this.g2 = dataframe.getDoubles("g2");
        this.r = dataframe.getDoubles("r");
        this.x = dataframe.getDoubles("x");
    }

    LineAdder create(Network network, int row) {
        LineAdder adder = network.newLine();
        setBranchAttributes(adder, row);
        applyIfPresent(b1, row, adder::setB1);
        applyIfPresent(b2, row, adder::setB2);
        applyIfPresent(g1, row, adder::setG1);
        applyIfPresent(g2, row, adder::setG2);
        applyIfPresent(r, row, adder::setR);
        applyIfPresent(x, row, adder::setX);
        return adder;
    }
}
