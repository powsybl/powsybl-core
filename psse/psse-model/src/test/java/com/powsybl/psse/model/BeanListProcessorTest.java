package com.powsybl.psse.model;

import com.powsybl.psse.model.io.BeanListProcessor;
import com.powsybl.psse.model.io.LineParser;
import com.powsybl.psse.model.pf.PsseBus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanListProcessorTest {
    @Test
    void testBus() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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
}
