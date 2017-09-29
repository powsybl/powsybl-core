/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.Interval;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IntervalAdapter extends XmlAdapter<String, Interval> {

    @Override
    public Interval unmarshal(String v) throws Exception {
        return Interval.parse(v);
    }

    @Override
    public String marshal(Interval v) throws Exception {
        return v.toString();
    }

}
