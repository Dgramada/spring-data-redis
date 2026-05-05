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

import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.connection.RedisJsonCommands;
import org.springframework.data.redis.serializer.RedisJsonMapper;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Default implementation of {@link JsonOperations}.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
class DefaultJsonOperations<K> extends AbstractOperations<K, Object> implements JsonOperations<K> {

	@SuppressWarnings("unchecked")
	DefaultJsonOperations(RedisTemplate<K, ?> template) {
		super((RedisTemplate<K, Object>) template);
	}

	private RedisJsonMapper jsonMapper() {
		RedisJsonMapper mapper = template.getRedisJsonMapper();
		Assert.state(mapper != null, "RedisJsonMapper is not configured");
		return mapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T array(K key, Function<JsonArray, ? extends JsonOperation<T>> spec) {

		byte[] rawKey = rawKey(key);
		var builder = new DefaultJsonArray();
		spec.apply(builder);

		Assert.notNull(builder.op, "Operation must not be null");
		Assert.notNull(builder.path, "Path must not be null");

		List<@Nullable Long> result = switch (builder.op) {
			case APPEND -> execute(c -> c.jsonCommands().jsonArrAppend(rawKey, builder.path, jsonMapper().toJsonArr(builder.values)));
			case INDEX_OF -> execute(c -> c.jsonCommands().jsonArrIndex(rawKey, builder.path, jsonMapper().toJson(builder.values[0])));
			case INSERT -> execute(c -> c.jsonCommands().jsonArrInsert(rawKey, builder.path, builder.index, jsonMapper().toJsonArr(builder.values)));
			case LENGTH -> execute(c -> c.jsonCommands().jsonArrLen(rawKey, builder.path));
			case TRIM -> execute(c -> c.jsonCommands().jsonArrTrim(rawKey, builder.path, builder.start, builder.stop));
		};

		return (T) result;
	}

	@Override
	public @Nullable Long clear(K key, String path) {

		byte[] rawKey = rawKey(key);

		return execute(c -> c.jsonCommands().jsonClear(rawKey, path));
	}

	@Override
	public @Nullable Long delete(K key, String path) {

		byte[] rawKey = rawKey(key);

		return execute(c -> c.jsonCommands().jsonDel(rawKey, path));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T get(K key, Function<JsonGet, ? extends JsonOperation<T>> spec) {

		byte[] rawKey = rawKey(key);
		var builder = new DefaultJsonGet();
		spec.apply(builder);

		Assert.notEmpty(builder.paths, "Paths must not be empty");

		String result = execute(c -> c.jsonCommands().jsonGet(rawKey, builder.paths));

		if (result == null) {
			return null;
		}

		return (T) new DefaultJsonResult(jsonMapper(), result);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T increment(K key, Function<JsonIncrement, ? extends JsonOperation<T>> spec) {

		byte[] rawKey = rawKey(key);
		var builder = new DefaultJsonIncrement();
		spec.apply(builder);

		Assert.notNull(builder.path, "Path must not be null");
		Assert.notNull(builder.number, "Number must not be null");

		return (T) execute(c -> c.jsonCommands().jsonNumIncrBy(rawKey, builder.path, builder.number));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T merge(K key, Function<JsonMerge, ? extends JsonOperation<T>> spec) {

		byte[] rawKey = rawKey(key);
		var builder = new DefaultJsonMerge();
		spec.apply(builder);

		Assert.notNull(builder.path, "Path must not be null");

		return (T) execute(c -> c.jsonCommands().jsonMerge(rawKey, builder.path, jsonMapper().toJson(builder.value)));
	}

	@Override
	public @Nullable JsonCollectionResult multiGet(Collection<K> keys, String path) {

		byte[][] rawKeys = rawKeys(keys);

		List<@Nullable String> result = execute(c -> c.jsonCommands().jsonMGet(path, rawKeys));

		return result != null ? new DefaultJsonCollectionResult(jsonMapper(), result) : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T set(K key, Function<JsonSet, ? extends JsonOperation<T>> spec) {

		byte[] rawKey = rawKey(key);
		var builder = new DefaultJsonSet();
		spec.apply(builder);

		Assert.notNull(builder.path, "Path must not be null");

		RedisJsonCommands.JsonSetOption option = switch (builder.condition) {
			case UPSERT -> RedisJsonCommands.JsonSetOption.UPSERT;
			case IF_PATH_NOT_EXISTS -> RedisJsonCommands.JsonSetOption.IF_PATH_NOT_EXISTS;
			case IF_PATH_EXISTS -> RedisJsonCommands.JsonSetOption.IF_PATH_EXISTS;
		};

		return (T) execute(c -> c.jsonCommands().jsonSet(rawKey, builder.path, jsonMapper().toJson(builder.value), option));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T string(K key, Function<JsonString, ? extends JsonOperation<T>> spec) {

		byte[] rawKey = rawKey(key);
		var builder = new DefaultJsonString();
		spec.apply(builder);

		Assert.notNull(builder.op, "Operation must not be null");
		Assert.notNull(builder.path, "Path must not be null");

		List<@Nullable Long> result = switch (builder.op) {
			case APPEND -> {
				Assert.notNull(builder.value, "Value must not be null");
				yield execute(c -> c.jsonCommands().jsonStrAppend(rawKey, builder.path, builder.value));
			}
			case LENGTH -> execute(c -> c.jsonCommands().jsonStrLen(rawKey, builder.path));
		};

		return (T) result;
	}

	@Override
	public @Nullable List<@Nullable Boolean> toggle(K key, String path) {

		byte[] rawKey = rawKey(key);

		return execute(c -> c.jsonCommands().jsonToggle(rawKey, path));
	}

	@Override
	public @Nullable List<@Nullable JsonType> type(K key, String path) {

		byte[] rawKey = rawKey(key);

		List<RedisJsonCommands.@Nullable JsonType> result = execute(c -> c.jsonCommands().jsonType(rawKey, path));

		return result != null ? result.stream().map(type -> type != null ? JsonType.valueOf(type.name()) : null).toList() : null;
	}

	abstract static class DefaultPathSpec<P extends PathSpec<P>> implements PathSpec<P> {

		@Nullable String path;

		@Override
		@SuppressWarnings("unchecked")
		public P at(String jsonPath) {
			this.path = jsonPath;
			return (P) this;
		}
	}

	static final class DefaultJsonArray extends DefaultPathSpec<JsonArray> implements JsonArray, LongListResponse {

		public enum Op {
			APPEND,
			INDEX_OF,
			INSERT,
			LENGTH,
			TRIM
		}

		@Nullable Op op;
		int index, start, stop;
		@Nullable Object[] values = new Object[0];

		@Override
		public LongListResponse append(@Nullable Object ... values) {
			this.op = Op.APPEND;
			this.values = values;
			return this;
		}

		@Override
		public LongListResponse indexOf(@Nullable Object value) {
			this.op = Op.INDEX_OF;
			this.values = new @Nullable Object[]{value};
			return this;
		}

		@Override
		public LongListResponse insert(int index, @Nullable Object ... values) {
			this.op = Op.INSERT;
			this.index = index;
			this.values = values;
			return this;
		}

		@Override
		public LongListResponse length() {
			this.op = Op.LENGTH;
			return this;
		}

		@Override
		public LongListResponse trim(int start, int stop) {
			this.op = Op.TRIM;
			this.start = start;
			this.stop = stop;
			return this;
		}

	}

	static final class DefaultJsonGet implements JsonGet, JsonResponse {

		String[] paths = new String[]{};

		@Override
		public JsonResponse at(String... paths) {
			this.paths = paths;
			return this;
		}
	}

	static final class DefaultJsonIncrement extends DefaultPathSpec<JsonIncrement> implements  JsonIncrement, NumericListResponse {

		@Nullable Number number;

		@Override
		public NumericListResponse by(Number number) {
			this.number = number;
			return this;
		}

	}

	static final class DefaultJsonMerge extends DefaultPathSpec<JsonMerge> implements JsonMerge, BooleanResponse {

		@Nullable Object value;

		@Override
		public BooleanResponse with(@Nullable Object value) {
			this.value = value;
			return this;
		}

	}

	static final class DefaultJsonSet extends DefaultPathSpec<JsonSet> implements JsonSet, BooleanResponse {

		enum Condition {
			UPSERT,
			IF_PATH_NOT_EXISTS,
			IF_PATH_EXISTS
		}

		Condition condition = Condition.UPSERT;
		@Nullable Object value;

		@Override
		public JsonSet ifPresent() {
			this.condition = Condition.IF_PATH_EXISTS;
			return this;
		}

		@Override
		public JsonSet ifAbsent() {
			this.condition = Condition.IF_PATH_NOT_EXISTS;
			return this;
		}

		@Override
		public BooleanResponse to(@Nullable Object value) {
			this.value = value;
			return this;
		}
	}

	static final class DefaultJsonString extends DefaultPathSpec<JsonString> implements JsonString, LongListResponse {

		enum Op {
			APPEND,
			LENGTH
		}

		@Nullable Op op;
		@Nullable String value;

		@Override
		public LongListResponse append(String value) {
			this.op = Op.APPEND;
			this.value = value;
			return this;
		}

		@Override
		public LongListResponse length() {
			this.op = Op.LENGTH;
			return this;
		}

	}

	static final class DefaultJsonResult implements JsonResult {

		private final RedisJsonMapper jsonMapper;
		private final @Nullable String result;

		DefaultJsonResult(RedisJsonMapper jsonMapper, @Nullable String result) {
			this.jsonMapper = jsonMapper;
			this.result = result;
		}

		@Override
		public @Nullable <V> V as(Class<V> type) {
			return result != null ? jsonMapper.fromJson(result, type) : null;
		}

		@Override
		public @Nullable <V> V as(ParameterizedTypeReference<V> typeRef) {
			return result != null ? jsonMapper.fromJson(result, typeRef) : null;
		}

		@Override
		public @Nullable String asString() {
			return result;
		}

		@Override
		public byte @Nullable [] asBytes() {
			return result != null ? result.getBytes(StandardCharsets.UTF_8) : null;
		}

		@Override
		public boolean isNull() {
			return result == null;
		}

	}

	static final class DefaultJsonCollectionResult implements JsonCollectionResult {

		private final RedisJsonMapper jsonMapper;
		private final List<@Nullable String> result;

		DefaultJsonCollectionResult(RedisJsonMapper jsonMapper, List<@Nullable String> result) {
			this.jsonMapper = jsonMapper;
			this.result = result;
		}

		@Override
		public <V> List<@Nullable V> as(Class<V> type) {
			return result.stream().map(it -> it != null ? jsonMapper.fromJson(it, type) : null).toList();
		}

		@Override
		public <V> List<@Nullable V> as(ParameterizedTypeReference<V> typeRef) {
			return result.stream().map(it -> it != null ? jsonMapper.fromJson(it, typeRef) : null).toList();
		}

		@Override
		public List<@Nullable String> asString() {
			return result;
		}

		@Override
		public List<byte @Nullable []> asBytes() {
			return result.stream().map(it -> it != null ? it.getBytes(StandardCharsets.UTF_8) : null).toList();
		}

		@Override
		public int size() {
			return result.size();
		}

		@Override
		public boolean isEmpty() {
			return result.isEmpty();
		}

		@Override
		public Stream<JsonResult> stream() {
			return result.stream().map(it -> new DefaultJsonResult(jsonMapper, it));
		}

		@Override
		public Iterator<JsonResult> iterator() {
			return stream().iterator();
		}

	}

}
