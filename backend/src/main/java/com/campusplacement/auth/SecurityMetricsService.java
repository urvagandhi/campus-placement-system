package com.campusplacement.auth;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * Service for exposing security metrics to Prometheus/Grafana.
 */
@Service
@RequiredArgsConstructor
public class SecurityMetricsService {

    private final MeterRegistry meterRegistry;
    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Counter refreshSuccessCounter;
    private Counter refreshFailureCounter;
    private Counter tokenReuseCounter;
    private Counter unauthorizedDeviceCounter;
    private Counter idleTimeoutCounter;

    @PostConstruct
    public void init() {
        loginSuccessCounter = Counter.builder("security.login.success")
                .description("Successful login attempts")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("security.login.failure")
                .description("Failed login attempts")
                .register(meterRegistry);

        refreshSuccessCounter = Counter.builder("security.token.refresh.success")
                .description("Successful token refresh attempts")
                .register(meterRegistry);

        refreshFailureCounter = Counter.builder("security.token.refresh.failure")
                .description("Failed token refresh attempts")
                .register(meterRegistry);

        tokenReuseCounter = Counter.builder("security.token.reuse.detected")
                .description("Token reuse/theft detection events")
                .register(meterRegistry);

        unauthorizedDeviceCounter = Counter.builder("security.unauthorized.device")
                .description("Requests from unauthorized devices")
                .register(meterRegistry);

        idleTimeoutCounter = Counter.builder("security.idle.timeout")
                .description("Sessions expired due to inactivity")
                .register(meterRegistry);
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailure() {
        loginFailureCounter.increment();
    }

    public void incrementRefreshSuccess() {
        refreshSuccessCounter.increment();
    }

    public void incrementRefreshFailure() {
        refreshFailureCounter.increment();
    }

    public void incrementTokenReuse() {
        tokenReuseCounter.increment();
    }

    public void incrementUnauthorizedDevice() {
        unauthorizedDeviceCounter.increment();
    }

    public void incrementIdleTimeout() {
        idleTimeoutCounter.increment();
    }
}
