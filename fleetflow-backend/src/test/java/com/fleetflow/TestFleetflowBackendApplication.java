package com.fleetflow;

import org.springframework.boot.SpringApplication;

public class TestFleetflowBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(FleetflowBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
