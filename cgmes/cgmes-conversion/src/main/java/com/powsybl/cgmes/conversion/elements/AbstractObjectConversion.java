/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractObjectConversion {

    public AbstractObjectConversion(String type, PropertyBag properties, Conversion.Context context) {
        this.type = type;
        this.p = properties;
        this.ps = null;
        this.context = context;
    }

    public AbstractObjectConversion(String type, PropertyBags propertiess, Conversion.Context context) {
        this.type = type;
        this.p = null;
        this.ps = propertiess;
        this.context = context;
    }

    public boolean insideBoundary() {
        return false;
    }

    public void convertInsideBoundary() {
        throw new ConversionException("No implementation at this level");
    }

    public abstract boolean valid();

    public abstract void convert();

    protected abstract String what();

    protected abstract String complete(String what);

    public static int fromContinuous(double value) {
        // ShuntCompensator sections and TapChanger step/position
        // coming from SV or SSH may be continuous,
        // we must read these values as floating point numbers
        // and convert them to integers for IIDM.
        // We should consider storing these attributes in IIDM also as real numbers.
        // This method is used only to clearly identify the affected properties
        return (int) value;
    }

    public boolean presentMandatoryProperty(String pname) {
        if (!p.containsKey(pname)) {
            invalid(String.format("Missing property %s", pname));
            return false;
        }
        return true;
    }

    public boolean inRange(String p, int x, int xmin, int xmax) {
        if (x < xmin || x > xmax) {
            invalid(String.format("%s value %d not in range [%d, %d]", p, x, xmin, xmax));
            return false;
        }
        return true;
    }

    public void invalid(String reason) {
        context.invalid(what(), reason);
    }

    public void ignored(String reason) {
        context.ignored(what(), reason);
    }

    public void ignored(String what, String reason) {
        context.ignored(complete(what), reason);
    }

    public void invalid(String what, String reason, double defaultValue) {
        String reason1 = String.format("%s. Used default value %f", reason, defaultValue);
        context.invalid(complete(what), reason1);
    }

    public void pending(String what, String reason) {
        context.pending(complete(what), reason);
    }

    public void fixed(String what, String reason) {
        context.fixed(complete(what), reason);
    }

    public void fixed(String what, String reason, double wrong, double fixed) {
        context.fixed(complete(what), reason, wrong, fixed);
    }

    public void missing(String what) {
        context.missing(complete(what));
    }

    public void missing(String what, double defaultValue) {
        context.missing(complete(what), defaultValue);
    }

    protected final String type;
    protected final PropertyBag p;
    protected final PropertyBags ps;
    protected final Conversion.Context context;
}
