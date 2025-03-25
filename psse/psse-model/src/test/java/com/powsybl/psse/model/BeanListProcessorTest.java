package com.powsybl.psse.model;

import com.powsybl.psse.model.io.BeanListProcessor;
import com.powsybl.psse.model.io.LineParser;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseGenerator;
import org.junit.jupiter.api.Test;

import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanListProcessorTest {
    @Test
    void testBus() {
        var line = "2,'Bus 2       ',138.0,3,4,5,6,1.037,0.44";
        var parser = new LineParser();
        var columns = parser.parseLine(line);
        assertEquals(9, columns.length);


        String[] headers = {"i", STR_NAME, STR_BASKV, STR_IDE, STR_AREA, STR_ZONE, STR_OWNER, STR_VM, STR_VA};
        var processor = new BeanListProcessor<PsseBus>(PsseBus.class, headers);
        processor.processLine(columns);

        var buses = processor.getBeans();
        assertEquals(1, buses.size());
        var bus = buses.get(0);
        assertEquals(2, bus.getI());
        assertEquals("Bus 2       ", bus.getName());
        assertEquals(138.0, bus.getBaskv(), 1e-6);
        assertEquals(3, bus.getIde());
        assertEquals(4, bus.getArea());
        assertEquals(5, bus.getZone());
        assertEquals(6, bus.getOwner());
        assertEquals(1.037, bus.getVm(), 1e-6);
        assertEquals(0.44, bus.getVa(), 1e-6);
    }

    @Test
    void testGen() {
        var line = "1,2,'1 ',0.01938,0.05917,0.0528,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1,1,0.0,1,1.0";
        var parser = new LineParser();
        var columns = parser.parseLine(line);
        assertEquals(18, columns.length);

        String[] headers = {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
        var processor = new BeanListProcessor<PsseGenerator>(PsseGenerator.class, headers);
        processor.processLine(columns);
    }
}
