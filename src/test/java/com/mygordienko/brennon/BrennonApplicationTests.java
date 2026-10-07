	package com.mygordienko.brennon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class BrennonApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:18.4"))
			.withDatabaseName("brennon")
			.withUsername("postgres")
			.withPassword("example")
			.withUrlParam("currentSchema", "brennon")
			.withInitScript("init.sql");

	/**
	 * NOTE: THere is no official Rustfs testcontainer yet.
	 */
	@Container
	static GenericContainer<?> rustFsContainer = new GenericContainer<>(DockerImageName.parse("rustfs/rustfs:1.0.1"))
            .withExposedPorts(9000)
            .withEnv("RUSTFS_VOLUMES", "/data/rustfs")
			.withEnv("RUSTFS_ADDRESS", "0.0.0.0:9000")
			.withEnv("RUSTFS_CONSOLE_ENABLE", "false")
			.withEnv("RUSTFS_ACCESS_KEY", "rustfsadmin")
			.withEnv("RUSTFS_SECRET_KEY", "rustfsadminpassword");

	@DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
		String host = rustFsContainer.getHost();
        Integer mappedPort = rustFsContainer.getMappedPort(9000);

        String accessKeyId = rustFsContainer.getEnvMap().get("RUSTFS_ACCESS_KEY");
		String secretAccessKey = rustFsContainer.getEnvMap().get("RUSTFS_SECRET_KEY");
        registry.add("brennon.ticket.attachments.storage.s3.endpoint", () -> "http://%s:%s".formatted(host, mappedPort));
        registry.add("brennon.ticket.attachments.storage.s3.credentials.accessKeyId", () -> accessKeyId);
		registry.add("brennon.ticket.attachments.storage.s3.credentials.secretAccessKey", () -> secretAccessKey);
    }

	@Test
	void contextLoads() {
	}
}
