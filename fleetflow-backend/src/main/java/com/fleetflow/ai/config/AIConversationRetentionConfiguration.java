package com.fleetflow.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix="fleetflow.ai", name="enabled", havingValue="true")
public class AIConversationRetentionConfiguration {}
