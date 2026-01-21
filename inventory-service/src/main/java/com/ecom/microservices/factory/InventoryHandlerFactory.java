package com.ecom.microservices.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InventoryHandlerFactory {

    private final List<InventoryHandler> handlers;
    private final Map<String, InventoryHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (InventoryHandler handler : handlers) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    public InventoryHandler getHandler(String type) {
        InventoryHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No inventory handler found for type: " + type);
        }
        return handler;
    }

    public InventoryHandler getDefaultHandler() {
        return getHandler(FefoInventoryHandler.TYPE);
    }

}
