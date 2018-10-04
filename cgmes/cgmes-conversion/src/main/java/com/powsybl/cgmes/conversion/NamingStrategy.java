package com.powsybl.cgmes.conversion;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface NamingStrategy {
    String getGeographicalTag(String geo);

    String getId(String type, String id);

    String getName(String type, String name);

    public static final class Identity implements NamingStrategy {
        @Override
        public String getGeographicalTag(String geo) {
            return geo;
        }

        @Override
        public String getId(String type, String id) {
            return id;
        }

        @Override
        public String getName(String type, String name) {
            return name;
        }
    }
}
