package com.maximovich.planner.common;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("%s with id %d was not found".formatted(resourceName, id));
    }
}
