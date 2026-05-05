package com.maximovich.planner.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("%s with id %d was not found".formatted(resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String id) {
        super("%s with id %s was not found".formatted(resourceName, id));
    }
}
