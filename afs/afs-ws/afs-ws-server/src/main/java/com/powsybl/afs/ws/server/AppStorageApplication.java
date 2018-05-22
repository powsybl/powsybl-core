/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.ws.utils.AfsRestApi;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 */
@ApplicationPath("/rest")
public class AppStorageApplication extends Application {

    public AppStorageApplication() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath("/rest");
        beanConfig.setResourcePackage(AppStorageServer.class.getPackage().getName());
        beanConfig.setScan();
        beanConfig.setInfo(new Info()
                .title("AFS storage API")
                .version(AfsRestApi.VERSION)
                .description("This is the documentation of AFS storage REST API"));
    }
}
