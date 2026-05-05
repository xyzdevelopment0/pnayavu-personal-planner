package com.maximovich.planner.dtos.async;

public record CounterRaceResponse(
    int threads,
    int incrementsPerThread,
    int expected,
    int unsafeCounter,
    int atomicCounter,
    int synchronizedCounter,
    int lostUnsafeUpdates
) {
}
