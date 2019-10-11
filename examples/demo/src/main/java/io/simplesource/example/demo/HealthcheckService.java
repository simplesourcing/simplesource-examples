package io.simplesource.example.demo;

import java.util.function.Supplier;

// TODO add elasticsearch healthcheck as that needs to be up to work
public class HealthcheckService {
    private final Supplier<Boolean> simpleSourceServiceIsHealthy;
    private final Supplier<Boolean> simpleSourceCommandApiSetIsHealthy;

    public HealthcheckService(Supplier<Boolean> simpleSourceServiceIsHealthy, Supplier<Boolean> simpleSourceCommandApiSetIsHealthy) {
        this.simpleSourceServiceIsHealthy = simpleSourceServiceIsHealthy;
        this.simpleSourceCommandApiSetIsHealthy = simpleSourceCommandApiSetIsHealthy;
    }

    public boolean isHealthy() {
        return simpleSourceServiceIsHealthy.get() && simpleSourceCommandApiSetIsHealthy.get();
    }
}
