package com.mygordienko.brennon;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.profile.internal.ProfileImageStoragePort;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class BrennonApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ProfileImageStoragePort profileImageStoragePort;

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
        registry.add("brennon.profile.images.storage.s3.endpoint", () -> "http://%s:%s".formatted(host, mappedPort));
        registry.add("brennon.profile.images.storage.s3.credentials.accessKeyId", () -> accessKeyId);
        registry.add("brennon.profile.images.storage.s3.credentials.secretAccessKey", () -> secretAccessKey);
    }

	@Test
	void contextLoads() {
	}

	@Test
	void profilePageRenders() throws Exception {
		mockMvc.perform(get("/profile").with(user("john@mail.com").authorities(
				new SimpleGrantedAuthority("user:read"),
				new SimpleGrantedAuthority("user:write"))))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Choose profile image")));
	}

	@Test
	void currentUserProfileFragmentRenders() throws Exception {
		mockMvc.perform(get("/current-user-profile").with(user("john@mail.com").authorities(
				new SimpleGrantedAuthority("user:read"),
				new SimpleGrantedAuthority("user:write"))))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("John Smith")));
	}

	@Test
	void profileImageIsPrivatelyCacheableByBrowser() throws Exception {
		// NOTE: "john@mail.com" user is created by the initial DB migration
		var profileOwnerId = BrennonUserId.of("1");
		var fileId = FileId.of("profile-image-caching-test");
		profileImageStoragePort.saveTemp(profileOwnerId, fileId, new ByteArrayInputStream("image".getBytes()), "image.png");
		profileImageStoragePort.moveToFinal(profileOwnerId, fileId);

		mockMvc.perform(get("/api/v1/profile/image").with(user("john@mail.com").authorities(
				new SimpleGrantedAuthority("user:read"))))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL,
					allOf(containsString("private"), containsString("max-age=600"))));
	}

	@Test
	void profileImageRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/profile/image"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
	}

	@Test
	void otherEndpointsAreStillNotCacheable() throws Exception {
		mockMvc.perform(get("/profile").with(user("john@mail.com").authorities(
				new SimpleGrantedAuthority("user:read"),
				new SimpleGrantedAuthority("user:write"))))
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, not(containsString("private"))));
	}
}
