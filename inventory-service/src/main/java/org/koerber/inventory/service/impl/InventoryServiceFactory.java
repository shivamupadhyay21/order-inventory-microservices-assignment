package org.koerber.inventory.service.impl;


import lombok.RequiredArgsConstructor;
import org.koerber.inventory.service.InventoryService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryServiceFactory {
    private final ApplicationContext context;

    public InventoryService getService(String type) {
        return (InventoryService) context.getBean("defaultInventoryService");
    }
}
