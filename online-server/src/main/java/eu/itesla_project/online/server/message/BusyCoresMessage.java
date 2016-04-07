/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server.message;

import javax.json.stream.JsonGenerator;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class BusyCoresMessage extends Message<int[]> {

    public BusyCoresMessage(int[] status) {
        super(status);
    }

    @Override
    protected String getType() {
        return "busyCores";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartArray("busyCores");
        for (int busyCores : body) {
            generator.write(busyCores);
        }
        generator.writeEnd();
    }

}
