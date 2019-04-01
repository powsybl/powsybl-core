/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.Errors;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractObjectConversion {

    public AbstractObjectConversion(String type, PropertyBag properties, Context context) {
        this.type = Objects.requireNonNull(type);
        this.p = Objects.requireNonNull(properties);
        this.ps = null;
        this.context = Objects.requireNonNull(context);
    }

    public AbstractObjectConversion(String type, PropertyBags properties, Context context) {
        this.type = Objects.requireNonNull(type);
        this.p = null;
        this.ps = Objects.requireNonNull(properties);
        this.context = Objects.requireNonNull(context);
    }

    public boolean insideBoundary() {
        return false;
    }

    public void convertInsideBoundary() {
        throw new ConversionException("No implementation at this level");
    }

    public abstract boolean valid();

    public abstract void convert();

    public abstract String what();

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

    public boolean presentMandatoryProperty(Errors.Invalid code, String pname) {
        if (!p.containsKey(pname)) {
            invalid(code, String.format("Missing property %s", pname));
            return false;
        }
        return true;
    }

    public boolean inRange(Errors.Invalid code, String p, int x, int xmin, int xmax) {
        if (x < xmin || x > xmax) {
            invalid(code, String.format("%s value %d not in range [%d, %d]", p, x, xmin, xmax));
            return false;
        }
        return true;
    }

    public void invalid(Errors.Invalid code, String reason) {
        context.invalid(code, what(), reason);
    }

    public void ignored(Errors.Ignored code, String reason) {
        context.ignored(code, what(), reason);
    }

    public void ignored(Errors.Ignored code, String what, String reason) {
        context.ignored(code, complete(what), reason);
    }

    public void invalid(Errors.Invalid code, String what, String reason, double defaultValue) {
        String reason1 = String.format("%s. Used default value %f", reason, defaultValue);
        context.invalid(code, complete(what), reason1);
    }

    public void pending(Errors.Pending code, String what, String reason) {
        context.pending(code, complete(what), reason);
    }

    public void fixed(Errors.Fixes code, String what, String reason) {
        context.fixed(code, complete(what), reason);
    }

    public void fixed(Errors.Fixes code, String what, String reason, double wrong, double fixed) {
        context.fixed(code, complete(what), reason, wrong, fixed);
    }

    public void missing(Errors.Missing code, String what) {
        context.missing(code, complete(what));
    }

    public void missing(Errors.Missing code, String what, double defaultValue) {
        context.missing(code, complete(what), defaultValue);
    }

    protected final String type;
    protected final PropertyBag p;
    protected final PropertyBags ps;
    protected final Context context;
}
