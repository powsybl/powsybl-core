/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.CountryConversion;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SubstationConversion extends AbstractIdentifiedObjectConversion {

    public SubstationConversion(PropertyBag s, Conversion.Context context) {
        super("Substation", s, context);
    }

    @Override
    public boolean valid() {
        // Only create IIDM Substations for CGMES substations that are not mapped to others
        return !context.substationIdMapping().isMapped(id);
    }

    @Override
    public void convert() {
        String subRegion = p.getId("SubRegion");
        String subRegionName = p.get("subRegionName");
        String regionName = p.get("regionName");

        Country country = CountryConversion.fromRegionName(regionName)
                .orElse(CountryConversion.fromSubregionName(subRegionName)
                        .orElse(CountryConversion.defaultCountry()));
        String geo = subRegion;

        // TODO add naminStrategy (for regions and substations)
        // After applying naming strategy it is possible that two CGMES substations are mapped
        // to the same Network substation, so we should check if corresponding substation has
        // already been created
        String geoTag = context.namingStrategy().getGeographicalTag(geo);

        String iidmSubstationId = context.substationIdMapping().iidm(id);
        Substation substation = context.network().getSubstation(iidmSubstationId);
        assert substation == null;
        context.network().newSubstation()
                .setId(iidmSubstationId)
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setCountry(country)
                .setGeographicalTags(geoTag)
                .add();
    }
}
