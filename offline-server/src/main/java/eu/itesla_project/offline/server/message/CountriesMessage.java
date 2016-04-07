/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.iidm.network.Country;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CountriesMessage extends Message {

    private final Country [] countries;
    
    public CountriesMessage(Country [] countries) {
        this.countries = countries;
    }

    @Override
    protected String getType() {
        return "countries";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartArray("countries");
        for(Country country : countries) {
            generator.writeStartObject();
            generator.write("id", country.name());
            generator.write("label", country.name() + ", " + country.getName());
            generator.writeEnd();
        }
        generator.writeEnd();
    }

}
