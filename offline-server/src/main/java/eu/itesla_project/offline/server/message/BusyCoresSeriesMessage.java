/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.offline.monitoring.BusyCoresSeries;
import javax.json.stream.JsonGenerator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusyCoresSeriesMessage extends Message {

    private final BusyCoresSeries busyCoresSeries;
    
    public BusyCoresSeriesMessage(BusyCoresSeries busyCoresSeries) {
        this.busyCoresSeries = busyCoresSeries;
    }

    @Override
    protected String getType() {
        return "busyCoresSeries";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartObject("busyCoresSeries");
        generator.write("availableCores", busyCoresSeries.getAvailableCores());
        generator.writeStartArray("values");
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm:ss");
        for (BusyCoresSeries.Value value : busyCoresSeries.getValues()) {
            generator.writeStartObject()
                    .write("date", value.getDate().toString(fmt))
                    .write("busyCores", value.getBusyCores())
                    .writeEnd();
        }
        generator.writeEnd()
                .writeEnd();
    }

}
