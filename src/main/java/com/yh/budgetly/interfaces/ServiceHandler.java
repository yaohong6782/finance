package com.yh.budgetly.interfaces;

import java.util.Map;

public interface ServiceHandler<R, T> {
    default R save(T request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default R save(T request, Map<String, String> headers) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default R retrieve(T request) {
        throw new UnsupportedOperationException("Not implemented");
    }
}