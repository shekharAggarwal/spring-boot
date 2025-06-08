/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.info;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.documentation.MockMvcEndpointDocumentationTests;
import org.springframework.boot.actuate.info.BuildInfoContributor;
import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.info.JavaInfoContributor;
import org.springframework.boot.actuate.info.OsInfoContributor;
import org.springframework.boot.actuate.info.ProcessInfoContributor;
import org.springframework.boot.actuate.info.SslInfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.info.SslInfo;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.boot.ssl.jks.JksSslStoreBundle;
import org.springframework.boot.ssl.jks.JksSslStoreDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

/**
 * Tests for generating documentation describing the {@link InfoEndpoint}.
 *
 * @author Andy Wilkinson
 */
class InfoEndpointDocumentationTests extends MockMvcEndpointDocumentationTests {

	@Test
	void info() {
		assertThat(this.mvc.get().uri("/actuator/info")).hasStatusOk()
			.apply(MockMvcRestDocumentation.document("info", gitInfo(), buildInfo(), osInfo(), processInfo(),
					javaInfo(), sslInfo()));
	}

	private ResponseFieldsSnippet gitInfo() {
		return responseFields(beneathPath("git"),
				fieldWithPath("branch").description("Name of the Git branch, if any."),
				fieldWithPath("commit").description("Details of the Git commit, if any."),
				fieldWithPath("commit.time").description("Timestamp of the commit, if any.").type(JsonFieldType.VARIES),
				fieldWithPath("commit.id").description("ID of the commit, if any."));
	}

	private ResponseFieldsSnippet buildInfo() {
		return responseFields(beneathPath("build"),
				fieldWithPath("artifact").description("Artifact ID of the application, if any.").optional(),
				fieldWithPath("group").description("Group ID of the application, if any.").optional(),
				fieldWithPath("name").description("Name of the application, if any.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("version").description("Version of the application, if any.").optional(),
				fieldWithPath("time").description("Timestamp of when the application was built, if any.")
					.type(JsonFieldType.VARIES)
					.optional());
	}

	private ResponseFieldsSnippet osInfo() {
		return responseFields(beneathPath("os"), osInfoField("name", "Name of the operating system"),
				osInfoField("version", "Version of the operating system"),
				osInfoField("arch", "Architecture of the operating system"));
	}

	private FieldDescriptor osInfoField(String field, String desc) {
		return fieldWithPath(field).description(desc + " (as obtained from the 'os." + field + "' system property).")
			.type(JsonFieldType.STRING)
			.optional();
	}

	private ResponseFieldsSnippet processInfo() {
		return responseFields(beneathPath("process"),
				fieldWithPath("pid").description("Process ID.").type(JsonFieldType.NUMBER),
				fieldWithPath("parentPid").description("Parent Process ID (or -1).").type(JsonFieldType.NUMBER),
				fieldWithPath("owner").description("Process owner.").type(JsonFieldType.STRING),
				fieldWithPath("cpus").description("Number of CPUs available to the process.")
					.type(JsonFieldType.NUMBER),
				fieldWithPath("memory").description("Memory information."),
				fieldWithPath("memory.heap").description("Heap memory."),
				fieldWithPath("memory.heap.init").description("Number of bytes initially requested by the JVM."),
				fieldWithPath("memory.heap.used").description("Number of bytes currently being used."),
				fieldWithPath("memory.heap.committed").description("Number of bytes committed for JVM use."),
				fieldWithPath("memory.heap.max")
					.description("Maximum number of bytes that can be used by the JVM (or -1)."),
				fieldWithPath("memory.nonHeap").description("Non-heap memory."),
				fieldWithPath("memory.nonHeap.init").description("Number of bytes initially requested by the JVM."),
				fieldWithPath("memory.nonHeap.used").description("Number of bytes currently being used."),
				fieldWithPath("memory.nonHeap.committed").description("Number of bytes committed for JVM use."),
				fieldWithPath("memory.nonHeap.max")
					.description("Maximum number of bytes that can be used by the JVM (or -1)."));
	}

	private ResponseFieldsSnippet javaInfo() {
		return responseFields(beneathPath("java"),
				fieldWithPath("version").description("Java version, if available.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("vendor").description("Vendor details."),
				fieldWithPath("vendor.name").description("Vendor name, if available.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("vendor.version").description("Vendor version, if available.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("runtime").description("Runtime details."),
				fieldWithPath("runtime.name").description("Runtime name, if available.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("runtime.version").description("Runtime version, if available.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("jvm").description("JVM details."),
				fieldWithPath("jvm.name").description("JVM name, if available.").type(JsonFieldType.STRING).optional(),
				fieldWithPath("jvm.vendor").description("JVM vendor, if available.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("jvm.version").description("JVM version, if available.")
					.type(JsonFieldType.STRING)
					.optional());
	}

	private ResponseFieldsSnippet sslInfo() {
		return responseFields(beneathPath("ssl"),
				fieldWithPath("bundles").description("SSL bundles information.").type(JsonFieldType.ARRAY),
				fieldWithPath("bundles[].name").description("Name of the SSL bundle.").type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains").description("Certificate chains in the bundle.")
					.type(JsonFieldType.ARRAY),
				fieldWithPath("bundles[].certificateChains[].alias").description("Alias of the certificate chain.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates").description("Certificates in the chain.")
					.type(JsonFieldType.ARRAY),
				fieldWithPath("bundles[].certificateChains[].certificates[].subject")
					.description("Subject of the certificate.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].version")
					.description("Version of the certificate.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].issuer")
					.description("Issuer of the certificate.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].validityStarts")
					.description("Certificate validity start date.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].serialNumber")
					.description("Serial number of the certificate.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].validityEnds")
					.description("Certificate validity end date.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].validity")
					.description("Certificate validity information.")
					.type(JsonFieldType.OBJECT),
				fieldWithPath("bundles[].certificateChains[].certificates[].validity.status")
					.description("Certificate validity status.")
					.type(JsonFieldType.STRING),
				fieldWithPath("bundles[].certificateChains[].certificates[].signatureAlgorithmName")
					.description("Signature algorithm name.")
					.type(JsonFieldType.STRING));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		InfoEndpoint endpoint(List<InfoContributor> infoContributors) {
			return new InfoEndpoint(infoContributors);
		}

		@Bean
		GitInfoContributor gitInfoContributor() {
			Properties properties = new Properties();
			properties.put("branch", "main");
			properties.put("commit.id", "df027cf1ec5aeba2d4fedd7b8c42b88dc5ce38e5");
			properties.put("commit.id.abbrev", "df027cf");
			properties.put("commit.time", Long.toString(Instant.now().getEpochSecond()));
			GitProperties gitProperties = new GitProperties(properties);
			return new GitInfoContributor(gitProperties);
		}

		@Bean
		BuildInfoContributor buildInfoContributor() {
			Properties properties = new Properties();
			properties.put("group", "com.example");
			properties.put("artifact", "application");
			properties.put("version", "1.0.3");
			BuildProperties buildProperties = new BuildProperties(properties);
			return new BuildInfoContributor(buildProperties);
		}

		@Bean
		OsInfoContributor osInfoContributor() {
			return new OsInfoContributor();
		}

		@Bean
		ProcessInfoContributor processInfoContributor() {
			return new ProcessInfoContributor();
		}

		@Bean
		JavaInfoContributor javaInfoContributor() {
			return new JavaInfoContributor();
		}

		@Bean
		SslInfo sslInfo() {
			DefaultSslBundleRegistry sslBundleRegistry = new DefaultSslBundleRegistry();
			JksSslStoreDetails keyStoreDetails = JksSslStoreDetails.forLocation("classpath:test.p12")
				.withPassword("secret");
			SslStoreBundle sslStoreBundle = new JksSslStoreBundle(keyStoreDetails, null);
			sslBundleRegistry.registerBundle("test-0", SslBundle.of(sslStoreBundle));
			return new SslInfo(sslBundleRegistry, Duration.ofDays(7));
		}

		@Bean
		SslInfoContributor sslInfoContributor(SslInfo sslInfo) {
			return new SslInfoContributor(sslInfo);
		}

	}

}
