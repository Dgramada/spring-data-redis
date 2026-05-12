/*
 * Copyright 2026-present the original author or authors.
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
package org.springframework.data.redis.core;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.DefaultJsonOperations.DefaultJsonCollectionResult;
import org.springframework.data.redis.serializer.JacksonRedisJsonMapper;
import org.springframework.data.redis.serializer.RedisJsonMapper;

/**
 * Unit tests for {@link DefaultJsonCollectionResult}.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
class DefaultJsonCollectionResultUnitTests {

	private final RedisJsonMapper mapper = JacksonRedisJsonMapper.createDefault();

	@Test
	void testAsClassDeserializesEachEntry() {

		DefaultJsonCollectionResult result = new DefaultJsonCollectionResult(mapper, List.of("1", "2", "3"));

		assertThat(result.as(Long.class)).containsExactly(1L, 2L, 3L);
	}

	@Test
	void testAsTypeRefDeserializesGenericEntries() {

		DefaultJsonCollectionResult result = new DefaultJsonCollectionResult(mapper, List.of("[1,2]", "[3,4]"));

		assertThat(result.as(new ParameterizedTypeReference<List<Long>>() {}))
				.containsExactly(List.of(1L, 2L), List.of(3L, 4L));
	}

	@Test
	void testAsStringReturnsRawEntries() {

		List<String> raw = Arrays.asList("{\"a\":1}", null, "{\"a\":2}");

		assertThat(new DefaultJsonCollectionResult(mapper, raw).asString()).isEqualTo(raw);
	}

	@Test
	void testAsBytesReturnsUtf8EncodingPerEntry() {

		DefaultJsonCollectionResult result = new DefaultJsonCollectionResult(mapper, Arrays.asList("foo", null));

		List<byte[]> bytes = result.asBytes();

		assertThat(bytes).hasSize(2);
		assertThat(bytes.get(0)).isEqualTo("foo".getBytes(StandardCharsets.UTF_8));
		assertThat(bytes.get(1)).isNull();
	}

}
