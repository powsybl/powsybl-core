package com.powsybl.shortcircuit.interceptors;

import com.google.auto.service.AutoService;

@AutoService(ShortCircuitAnalysisInterceptorExtension.class)
public class ShortCircuitAnalysisInterceptorExtensionMock implements ShortCircuitAnalysisInterceptorExtension {
    @Override
    public String getName() {
        return "ShortCircuitInterceptorExtensionMock";
    }

    @Override
    public ShortCircuitAnalysisInterceptor createInterceptor() {
        return new ShortCircuitAnalysisInterceptorMock();
    }
}
