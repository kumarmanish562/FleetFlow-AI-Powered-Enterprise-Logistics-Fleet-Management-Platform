package com.fleetflow.ai.tools;

import com.fleetflow.ai.security.AIDataAccessPolicy;
import com.fleetflow.analytics.service.AnalyticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RouteAnalyticsTools {
    private final AnalyticsService analytics;
    private final AIDataAccessPolicy access;

    public RouteAnalyticsTools(AnalyticsService analytics, AIDataAccessPolicy access) {
        this.analytics = analytics;
        this.access = access;
    }

    @Tool(description = "Return backend-calculated delivery counts grouped by route for a date range. Maximum range is 90 days; dates use the server reporting timezone.")
    public Object getRouteAnalytics(
            @ToolParam(description = "Start date, YYYY-MM-DD") LocalDate from,
            @ToolParam(description = "End date, YYYY-MM-DD") LocalDate to) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getRouteAnalytics", scope ->
                analytics.routeDelaysForAI(from, to, scope.organizationId()));
    }

    @Tool(description = "Return current organization-scoped fleet totals by available, assigned, maintenance and inactive status. Read-only.")
    public Object getFleetAnalytics() {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getFleetAnalytics", scope ->
                analytics.fleetForAI(scope.organizationId()));
    }

    @Tool(description = "Return backend-calculated delivery outcomes, on-time rate and average delivery duration for a maximum 90-day UTC reporting window. Read-only.")
    public Object getDeliveryPerformance(
            @ToolParam(description = "Start date, YYYY-MM-DD") LocalDate from,
            @ToolParam(description = "End date, YYYY-MM-DD") LocalDate to) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getDeliveryPerformance", scope ->
                analytics.deliveryPerformanceForAI(from, to, scope.organizationId()));
    }
}
