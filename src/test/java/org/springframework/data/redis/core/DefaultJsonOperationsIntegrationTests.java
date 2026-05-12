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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.ObjectFactory;
import org.springframework.data.redis.test.condition.EnabledOnCommand;

/**
 * Integration test of {@link DefaultJsonOperations}.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
@ParameterizedClass
@MethodSource("testParams")
class DefaultJsonOperationsIntegrationTests<K, V> {

	private final RedisTemplate<K, V> redisTemplate;
	private final ObjectFactory<K> keyFactory;
	private final JsonOperations<K> jsonOps;

	public DefaultJsonOperationsIntegrationTests(RedisTemplate<K, V> redisTemplate, ObjectFactory<K> keyFactory) {

		this.redisTemplate = redisTemplate;
		this.keyFactory = keyFactory;
		this.jsonOps = redisTemplate.opsForJson();
	}

	static Collection<Object[]> testParams() {
		return AbstractOperationsTestParams.testParams();
	}

	@BeforeEach
	void setUp() {
		redisTemplate.execute((RedisCallback<Object>) connection -> {
			connection.serverCommands().flushDb();
			return null;
		});
	}

	@Test //
	@EnabledOnCommand("JSON.ARRAPPEND")
	void testArrayAppend() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.array(key, spec -> spec.at("$.forsakenDefeated").append(4, 5, 6)))
				.isEqualTo(List.of(6L));
		assertThat(jsonOps.get(key, spec -> spec.at("$.forsakenDefeated")).as(new ParameterizedTypeReference<List<List<Long>>>() {}))
				.isEqualTo(List.of(List.of(1L, 2L, 3L, 4L, 5L, 6L)));
	}

	@Test //
	@EnabledOnCommand("JSON.ARRINDEX")
	void testArrayIndex() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.array(key, spec -> spec.at("$.forsakenDefeated").indexOf(2L)))
				.isEqualTo(List.of(1L));
		assertThat((List<Long>) jsonOps.array(key, spec -> spec.at("$.forsakenDefeated").indexOf(Integer.MAX_VALUE)))
				.isEqualTo(List.of(-1L));
	}

	@Test //
	@EnabledOnCommand("JSON.ARRINSERT")
	void testArrayInsert() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.array(key, spec -> spec.at("$.forsakenDefeated").insert(1, 4, 5, 6)))
				.isEqualTo(List.of(6L));
	}

	@Test //
	@EnabledOnCommand("JSON.ARRLEN")
	void testArrayLength() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.array(key, spec -> spec.at("$.forsakenDefeated").length()))
				.isEqualTo(List.of(3L));
	}

	@Test //
	@EnabledOnCommand("JSON.ARRTRIM")
	void testArrayTrim() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.array(key, spec -> spec.at("$.forsakenDefeated").trim(1, 2)))
				.isEqualTo(List.of(2L));
	}

	@Test //
	@EnabledOnCommand("JSON.CLEAR")
	void testClear() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat(jsonOps.clear(key, "$")).isEqualTo(1);
	}

	@Test //
	@EnabledOnCommand("JSON.DEL")
	void testDelete() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat(jsonOps.delete(key, "$.forsakenDefeated")).isEqualTo(1);
	}

	@Test //
	@EnabledOnCommand("JSON.GET")
	void testGet() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat(jsonOps.get(key, JsonOperations.JsonGet::atRoot).as(new ParameterizedTypeReference<List<DragonReborn>>() {}))
				.isEqualTo(List.of(DRAGON_REBORN));
	}

	@Test //
	@EnabledOnCommand("JSON.NUMINCRBY")
	void testIncrement() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Number>) jsonOps.increment(key, spec -> spec.at("$.age").by(1L)))
				.isEqualTo(List.of(35L));
	}

	@Test //
	@EnabledOnCommand("JSON.MERGE")
	void testMerge() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((Boolean) jsonOps.merge(key, spec -> spec.atRoot().with(Map.of("age", 35))))
				.isTrue();
	}

	@Test //
	@EnabledOnCommand("JSON.MGET")
	void testMultiGet() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat(jsonOps.multiGet(List.of(key), "$").as(new ParameterizedTypeReference<List<DragonReborn>>() {}))
				.isEqualTo(List.of(List.of(DRAGON_REBORN)));
	}

	@Test //
	@EnabledOnCommand("JSON.SET")
	void testSet() {

		K key = keyFactory.instance();

		assertThat((Boolean) jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN))).isTrue();
	}

	@Test //
	@EnabledOnCommand("JSON.STRAPPEND")
	void testStringAppend() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.string(key, spec -> spec.at("$.name").append("foo")))
				.isEqualTo(List.of(15L));
	}

	@Test //
	@EnabledOnCommand("JSON.STRLEN")
	void testStringLength() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat((List<Long>) jsonOps.string(key, spec -> spec.at("$.name").length()))
				.isEqualTo(List.of(12L));
	}

	@Test //
	@EnabledOnCommand("JSON.TOGGLE")
	void testToggle() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat(jsonOps.toggle(key, "$.madness"))
				.isEqualTo(List.of(true));
	}

	@Test //
	@EnabledOnCommand("JSON.TYPE")
	void testType() {

		K key = keyFactory.instance();

		jsonOps.set(key, spec -> spec.atRoot().to(DRAGON_REBORN));

		assertThat(jsonOps.type(key, "$.name")).isEqualTo(List.of(JsonOperations.JsonType.STRING));
	}

	record Callandor(
			String name,
			double length,
			double width
	) {}

	record DragonReborn(
			String name,
			long age,
			boolean madness,
			List<String> titles,
			List<Long> forsakenDefeated,
			Callandor callandor
	) {}

	private static final Callandor CALLANDOR = new Callandor("Callandor", 10.0, 1.0);

	private static final DragonReborn DRAGON_REBORN = new DragonReborn(
			"Rand al'Thor",
			34,
			false,
			List.of("Dragon Reborn", "Lord of the Morning"),
			List.of(1L, 2L, 3L),
			CALLANDOR
	);

}
