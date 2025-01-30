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
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;
import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.EMPTY;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractIdentifiedObjectConversion extends AbstractObjectConversion {

    public AbstractIdentifiedObjectConversion(String type, PropertyBag properties, Context context) {
        super(type, properties, context);

        this.id = properties.getId(type);
        this.name = p.get("name");
        if (this.name == null) {
            missing("name");
        }
    }

    public AbstractIdentifiedObjectConversion(String type, PropertyBags propertiess, Context context) {
        super(type, propertiess, context);

        this.id = ps.get(0).getId(type);
        this.name = ps.get(0).get("name");
        if (this.name == null) {
            missing("name");
        }
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String iidmId() {
        return context.namingStrategy().getIidmId(type, id);
    }

    public String iidmName() {
        return context.namingStrategy().getIidmName(type, name);
    }

    // Identification

    public void identify(IdentifiableAdder<?, ?> adder) {
        identify(adder, iidmId(), iidmName());
    }

    public void identify(IdentifiableAdder<?, ?> adder, String duplicatedTag) {
        identify(adder, iidmId() + duplicatedTag, iidmName() + duplicatedTag);
    }

    public void identify(IdentifiableAdder<?, ?> adder, String id, String name) {
        identify(context, adder, id, name);
    }

    public static void identify(Context context, IdentifiableAdder<?, ?> adder, String id, String name) {
        adder
                .setId(id)
                .setName(name)
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity());
    }

    @Override
    public String what() {
        if (name != null) {
            return String.format("%s %s (%s)", type, name, id);
        } else {
            return String.format("%s %s", type, id);
        }
    }

    @Override
    protected String complete(String what) {
        if (name != null) {
            return String.format("%s at %s %s (%s)", what, type, name, id);
        } else {
            return String.format("%s at %s %s", what, type, id);
        }
    }

    protected static double defaultValue(DefaultValueDouble defaultValue, Context context) {
        List<Conversion.Config.DefaultValue> validDefaultValues = new ArrayList<>();
        if (defaultValue.equipmentValue != null) {
            validDefaultValues.add(EQ);
        }
        if (defaultValue.previousValue != null) {
            validDefaultValues.add(PREVIOUS);
        }
        if (defaultValue.defaultValue != null) {
            validDefaultValues.add(DEFAULT);
        }
        validDefaultValues.add(EMPTY);
        return defaultValue(defaultValue, getDefaultValueSelector(validDefaultValues, context));
    }

    private static double defaultValue(DefaultValueDouble defaultValue, Conversion.Config.DefaultValue defaultValueSelector) {
        return switch (defaultValueSelector) {
            case EQ -> defaultValue.equipmentValue;
            case PREVIOUS -> defaultValue.previousValue;
            case DEFAULT -> defaultValue.defaultValue;
            case EMPTY -> defaultValue.emptyValue;
        };
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelector(List<Conversion.Config.DefaultValue> validDefaultValues, Context context) {
        return context.config().updateDefaultValuesPriority().stream().filter(validDefaultValues::contains).findFirst().orElse(EMPTY);
    }

    public record DefaultValueDouble(Double equipmentValue, Double previousValue, Double defaultValue, double emptyValue) {
    }

    protected final String id;
    protected final String name;
}
