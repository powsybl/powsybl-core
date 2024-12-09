package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Line;

import java.util.Objects;

public abstract class AbstractIdenticalLinesTest {
    public boolean areLinesIdentical(Line line1, Line line2) {
        boolean areIdentical = false;

        if (line1 != null && line2 != null) {
            areIdentical = line1.getR() == line2.getR()
                    && line1.getX() == line2.getX()
                    && line1.getG1() == line2.getG1()
                    && line1.getG2() == line2.getG2()
                    && line1.getB1() == line2.getB1()
                    && line1.getB2() == line2.getB2()
                    && Objects.equals(line1.getTerminal1().getVoltageLevel().getId(), line2.getTerminal1().getVoltageLevel().getId())
                    && Objects.equals(line1.getTerminal2().getVoltageLevel().getId(), line2.getTerminal2().getVoltageLevel().getId());
        }
        return areIdentical;
    }
}


