/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.CountryConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class SubstationConversion extends AbstractIdentifiedObjectConversion {

    public SubstationConversion(PropertyBag s, Context context) {
        super(CgmesNames.SUBSTATION, s, context);
    }

    @Override
    public boolean valid() {
        // Only create IIDM Substations for CGMES substations that are not mapped to others
        return !context.substationIdMapping().substationIsMapped(id);
    }

    @Override
    public void convert() {
        String subRegionName = p.get("subRegionName");
        String regionName = p.get("regionName");

        Country country = CountryConversion.fromRegionName(regionName)
                .orElseGet(() -> CountryConversion.fromSubregionName(subRegionName)
                        .orElse(null));

        // After applying naming strategy it is possible that two CGMES substations are mapped
        // to the same Network substation, so we should check if corresponding substation has
        // already been created
        String iidmSubstationId = context.substationIdMapping().substationIidm(id);
        Substation substation = context.network().getSubstation(iidmSubstationId);
        if (substation != null) {
            throw new IllegalStateException("Substation should be null");
        }
        SubstationAdder adder = context.network().newSubstation()
                .setId(iidmSubstationId)
                .setName(iidmName())
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                .setCountry(country);
        if (subRegionName != null) {
            adder.setGeographicalTags(subRegionName);
        }
        Substation s = adder.add();
        addAliasesAndProperties(s, p.getId("SubRegion"), p.getId("Region"), regionName);
    }

    private void addAliasesAndProperties(Substation s, String subRegionId, String regionId, String regionName) {
        int index = 0;
        for (String mergedSub : context.substationIdMapping().mergedSubstations(s.getId())) {
            index++;
            s.addAlias(mergedSub, "MergedSubstation" + index, context.config().isEnsureIdAliasUnicity());
        }
        s.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId", subRegionId);
        s.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId", regionId);
        s.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionName", regionName);
    }
}
