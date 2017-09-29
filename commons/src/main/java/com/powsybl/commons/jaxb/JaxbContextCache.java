/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.jaxb;

import com.powsybl.commons.exceptions.UncheckedJaxbException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JaxbContextCache {

    public static final JaxbContextCache DEFAULT = new JaxbContextCache();

    private final Map<Class<?>, JAXBContext> cache = new HashMap<>();

    public <U> JAXBContext createContext(Class<U> aClass) {
        // creation of the jaxb context is very slow and could be cached
        // for next runs (JAXBContext is thread safe but not Marshaller)
        JAXBContext jaxbContext = cache.get(aClass);
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(aClass);
                cache.put(aClass, jaxbContext);
            } catch (JAXBException e) {
                throw new UncheckedJaxbException(e);
            }
        }
        return jaxbContext;
    }

    public void clear() {
        cache.clear();
    }

}
