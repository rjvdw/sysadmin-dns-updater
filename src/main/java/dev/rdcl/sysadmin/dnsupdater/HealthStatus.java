package dev.rdcl.sysadmin.dnsupdater;

import java.util.Optional;

public class HealthStatus {

    /// If healthy, counts the number of failures. If unhealthy, counts the number of successes.
    private int count = 0;

    /// Whether the system is healthy or unhealthy.
    private boolean healthy = true;

    /// If true, indicates that the health status has changed since the last time it was checked.
    private boolean statusHasChanged = false;

    /// The number of consecutive successes needed for the service to be considered to be healthy.
    private final int healthyThreshold;

    /// The number of consecutive failures needed for the service to be considered to be unhealthy.
    private final int unhealthyThreshold;

    /// @param healthyThreshold   The number of consecutive successes needed for the service to be considered healthy.
    /// @param unhealthyThreshold The number of consecutive failures needed for the service to be considered unhealthy.
    public HealthStatus(int healthyThreshold, int unhealthyThreshold) {
        this.healthyThreshold = healthyThreshold;
        this.unhealthyThreshold = unhealthyThreshold;
    }

    /// Register a success. If healthy, this will reset the failure count. If unhealthy, this will count towards the
    /// success count.
    public synchronized void registerSuccess() {
        if (healthy) {
            count = 0;
        } else {
            count += 1;
            if (count >= healthyThreshold) {
                statusHasChanged = true;
                healthy = true;
                count = 0;
            }
        }
    }

    /// Register a failure. If healthy, this will count towards the failure count. If unhealthy, this will reset the
    /// success count.
    public synchronized void registerFailure() {
        if (healthy) {
            count += 1;
            if (count >= unhealthyThreshold) {
                statusHasChanged = true;
                healthy = false;
                count = 0;
            }
        } else {
            count = 0;
        }
    }

    /// Checks if the health status has changed since the previous check.
    ///
    /// @return An optional containing a boolean value representing the health status if the health status has changed
    /// since the previous check. An empty optional if the status did not change.
    public synchronized Optional<Boolean> checkIfChanged() {
        if (statusHasChanged) {
            statusHasChanged = false;
            return Optional.of(healthy);
        }

        return Optional.empty();
    }

}
