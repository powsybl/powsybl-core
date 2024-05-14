/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.*;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public class DcMapping {

    public DcMapping(Context context) {
        this.context = Objects.requireNonNull(context);
        this.cgmesConverters = new HashMap<>();
        this.cgmesDcLineSegments = new HashMap<>();
    }

    public void initialize() {
        context.cgmes().acDcConverters()
            .forEach(pb -> this.cgmesConverters.put(pb.getId("ACDCConverter"), new CgmesConverter(pb)));
        context.cgmes().dcLineSegments()
            .forEach(pb -> this.cgmesDcLineSegments.put(pb.getId("DCLineSegment"), new CgmesDcLineSegment(pb)));
    }

    PropertyBag getCgmesConverterPropertyBag(String id) {
        CgmesConverter cgmesConverter = this.cgmesConverters.get(id);
        if (cgmesConverter != null) {
            return cgmesConverter.propertyBag;
        }
        return null;
    }

    void setCgmesConverterUsed(String id) {
        CgmesConverter cgmesConverter = this.cgmesConverters.get(id);
        if (cgmesConverter != null) {
            cgmesConverter.used();
        }
    }

    void reportCgmesConvertersNotUsed() {
        this.cgmesConverters.entrySet().stream()
            .filter(c -> !c.getValue().used)
            .forEach(c -> {
                String what = "AcDcConverter Id: " + c.getKey();
                context.ignored(what, "Dc configuration not supported");
            });
    }

    PropertyBag getCgmesDcLineSegmentPropertyBag(String id) {
        CgmesDcLineSegment cgmesDcLineSegment = this.cgmesDcLineSegments.get(id);
        if (cgmesDcLineSegment != null) {
            return cgmesDcLineSegment.propertyBag;
        }
        return null;
    }

    void setCgmesDcLineSegmentUsed(String id) {
        CgmesDcLineSegment cgmesDcLineSegment = this.cgmesDcLineSegments.get(id);
        if (cgmesDcLineSegment != null) {
            cgmesDcLineSegment.setUsed();
        }
    }

    void reportCgmesDcLineSegmentNotUsed() {
        this.cgmesDcLineSegments.entrySet().stream()
            .filter(c -> !c.getValue().used)
            .forEach(c -> {
                String what = "DcLineSegment Id: " + c.getKey();
                context.ignored(what, "Ground DcLineSegment or Dc configuration not supported");
            });
    }

    private static class CgmesConverter {
        private PropertyBag propertyBag;
        private boolean used;

        CgmesConverter(PropertyBag propertyBag) {
            this.propertyBag = propertyBag;
            this.used = false;
        }

        void used() {
            this.used = true;
        }
    }

    private static class CgmesDcLineSegment {
        private PropertyBag propertyBag;
        private boolean used;

        CgmesDcLineSegment(PropertyBag propertyBag) {
            this.propertyBag = propertyBag;
            this.used = false;
        }

        void setUsed() {
            this.used = true;
        }
    }

    private final Context context;
    private final Map<String, CgmesConverter> cgmesConverters;
    private final Map<String, CgmesDcLineSegment>cgmesDcLineSegments;
}
