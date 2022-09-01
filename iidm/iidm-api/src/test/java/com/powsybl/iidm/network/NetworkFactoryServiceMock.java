package com.powsybl.iidm.network;

import com.google.auto.service.AutoService;
import org.mockito.Mockito;

@AutoService(NetworkFactoryService.class)
public class NetworkFactoryServiceMock implements NetworkFactoryService {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public NetworkFactory createNetworkFactory() {
        return Mockito.mock(NetworkFactory.class);
    }
}
