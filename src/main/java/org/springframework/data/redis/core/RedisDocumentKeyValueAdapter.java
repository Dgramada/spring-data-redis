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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.redis.core.mapping.RedisDocumentMappingContext;
import org.springframework.data.redis.core.mapping.RedisDocumentPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisDocumentPersistentProperty;
import org.springframework.data.util.CloseableIterator;
import org.springframework.util.Assert;

/**
 * Redis specific {@link KeyValueAdapter} implementation storing objects as Redis JSON documents. Each object is written
 * to its own key, built from the keyspace and the id as {@code keyspace:id}, holding the whole object as a JSON
 * document at the root path.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public class RedisDocumentKeyValueAdapter extends AbstractKeyValueAdapter {

	private final RedisOperations<?, ?> redisOps;
	private final RedisJsonOperations<byte[]> jsonOps;
	private final RedisDocumentMappingContext mappingContext;

	public RedisDocumentKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisJsonOperations<byte[]> jsonOps,
			RedisDocumentMappingContext mappingContext) {

		super(new RedisDocumentQueryEngine());

		Assert.notNull(redisOps, "RedisOperations must not be null");
		Assert.notNull(jsonOps, "RedisJsonOperations must not be null");
		Assert.notNull(mappingContext, "RedisDocumentMappingContext must not be null");

		this.redisOps = redisOps;
		this.jsonOps = jsonOps;
		this.mappingContext = mappingContext;
	}

	@Override
	public @Nullable Object put(Object id, Object item, String keyspace) {

		Assert.notNull(id, "Id must not be null");
		Assert.notNull(item, "Item must not be null");
		Assert.notNull(keyspace, "Keyspace must not be null");

		byte[] key = createKey(keyspace, toString(id));

		Object previous = doGet(key, item.getClass());

		jsonOps.value(key).set(item);
		applyTimeToLive(key, item);

		return previous;
	}

	@Override
	public boolean contains(Object id, String keyspace) {

		Assert.notNull(id, "Id must not be null");
		Assert.notNull(keyspace, "Keyspace must not be null");

		byte[] key = createKey(keyspace, toString(id));

		Boolean exists = redisOps.execute((RedisCallback<Boolean>) connection -> connection.keyCommands().exists(key));

		return Boolean.TRUE.equals(exists);
	}

	@Override
	public @Nullable Object get(Object id, String keyspace) {
		return get(id, keyspace, Object.class);
	}

	@Override
	public <T> @Nullable T get(Object id, String keyspace, Class<T> type) {

		Assert.notNull(id, "Id must not be null");
		Assert.notNull(keyspace, "Keyspace must not be null");
		Assert.notNull(type, "Type must not be null");

		byte[] key = createKey(keyspace, toString(id));

		return readBackTimeToLive(key, doGet(key, type));
	}

	@Override
	public @Nullable Object delete(Object id, String keyspace) {
		return delete(id, keyspace, Object.class);
	}

	@Override
	public <T> @Nullable T delete(Object id, String keyspace, Class<T> type) {

		Assert.notNull(id, "Id must not be null");
		Assert.notNull(keyspace, "Keyspace must not be null");
		Assert.notNull(type, "Type must not be null");

		byte[] key = createKey(keyspace, toString(id));

		T value = doGet(key, type);

		if (value != null) {
			jsonOps.value(key).delete();
		}

		return value;
	}

	@Override
	public List<Object> getAllOf(String keyspace) {
		return getAllOf(keyspace, Object.class, -1, -1);
	}

	@Override
	public <T> List<T> getAllOf(String keyspace, Class<T> type) {
		return getAllOf(keyspace, type, -1, -1);
	}

	/**
	 * Get all documents of the given keyspace.
	 *
	 * @param keyspace the keyspace to fetch documents from.
	 * @param type the desired target type.
	 * @param offset index value to start reading.
	 * @param rows maximum number of documents to return.
	 * @return never {@literal null}.
	 * @throws UnsupportedOperationException until Redis Search support is available.
	 * @since 4.2
	 */
	public <T> List<T> getAllOf(String keyspace, Class<T> type, long offset, int rows) {

		Assert.notNull(keyspace, "Keyspace must not be null");
		Assert.notNull(type, "Type must not be null");

		throw requiresRedisSearch("Reading all documents of a keyspace");
	}

	@Override
	public CloseableIterator<Map.Entry<Object, Object>> entries(String keyspace) {
		throw new UnsupportedOperationException("Cursor based iteration over a keyspace is not supported");
	}

	@Override
	public void deleteAllOf(String keyspace) {

		Assert.notNull(keyspace, "Keyspace must not be null");

		throw requiresRedisSearch("Deleting all documents of a keyspace");
	}

	@Override
	public void clear() {
		// nothing to do
	}

	@Override
	public long count(String keyspace) {

		Assert.notNull(keyspace, "Keyspace must not be null");

		throw requiresRedisSearch("Counting the documents of a keyspace");
	}

	@Override
	public void destroy() throws Exception {
		// nothing to do
	}

	private static UnsupportedOperationException requiresRedisSearch(String operation) {
		return new UnsupportedOperationException(("%s requires Redis Search; document repositories support access by id"
				+ " only until Redis Search support is available").formatted(operation));
	}

	private byte[] createKey(String keyspace, String id) {
		return toBytes(keyspace + ":" + id);
	}

	private byte[] toBytes(Object value) {
		return value instanceof byte[] bytes ? bytes : toString(value).getBytes(StandardCharsets.UTF_8);
	}

	private String toString(Object value) {

		if (value instanceof String stringValue) {
			return stringValue;
		}
		if (value instanceof byte[] bytes) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		return String.valueOf(value);
	}

	private <T> @Nullable T doGet(byte[] key, Class<T> type) {

		JsonOperations.JsonResult result = jsonOps.get(key);

		return result.isNull() ? null : result.as(type);
	}

	/**
	 * Writes the remaining time to live of the given key into the {@link TimeToLive} annotated property, if the entity
	 * declares one. Without it the property would hold the value stored on write, causing a subsequent write to reset
	 * or remove the expiration of the document.
	 */
	private <T> @Nullable T readBackTimeToLive(byte[] key, @Nullable T target) {

		if (target == null) {
			return null;
		}

		RedisDocumentPersistentEntity<?> entity = mappingContext.getPersistentEntity(target.getClass());

		if (entity == null || !entity.hasExplicitTimeToLiveProperty()) {
			return target;
		}

		RedisDocumentPersistentProperty ttlProperty = entity.getExplicitTimeToLiveProperty();

		if (ttlProperty == null) {
			return target;
		}

		TimeToLive ttl = ttlProperty.getRequiredAnnotation(TimeToLive.class);

		Long timeToLive = redisOps.execute((RedisCallback<Long>) connection -> TimeUnit.SECONDS == ttl.unit()
				? connection.keyCommands().ttl(key)
				: connection.keyCommands().pTtl(key, ttl.unit()));

		if (timeToLive == null && ttlProperty.getType().isPrimitive()) {
			return target;
		}

		PersistentPropertyAccessor<T> accessor = entity.getPropertyAccessor(target);

		accessor.setProperty(ttlProperty,
				DefaultConversionService.getSharedInstance().convert(timeToLive, ttlProperty.getType()));

		return accessor.getBean();
	}

	private void applyTimeToLive(byte[] key, Object item) {

		RedisDocumentPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(item.getClass());

		if (!entity.isExpiring()) {
			return;
		}

		Long timeToLive = entity.getTimeToLiveAccessor().getTimeToLive(item);

		redisOps.execute((RedisCallback<@Nullable Void>) connection -> {

			if (timeToLive != null && timeToLive > 0) {
				connection.keyCommands().expire(key, timeToLive);
			} else {
				connection.keyCommands().persist(key);
			}

			return null;
		});
	}

}
