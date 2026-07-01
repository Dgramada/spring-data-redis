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

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisJsonSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

/**
 * Helper class that simplifies Redis JSON data access.
 * <p>
 * Keys are serialized with {@link JdkSerializationRedisSerializer} by default, which produces binary (non human-readable)
 * key representations.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public class RedisJsonTemplate<K> extends RedisAccessor {

	private final RedisSerializer<K> keySerializer;
	private final RedisJsonSerializer jsonSerializer;
	private final boolean enableTransactionSupport;

	private final JsonOperations<K> jsonOperations = new DefaultJsonOperations<>(this);

	@SuppressWarnings("unchecked")
	public RedisJsonTemplate(RedisConnectionFactory connectionFactory) {
		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
		setConnectionFactory(connectionFactory);
		this.keySerializer = (RedisSerializer<K>) RedisSerializer.string();
		this.jsonSerializer = (RedisJsonSerializer) RedisSerializer.json();
		this.enableTransactionSupport = false;
	}

	public RedisJsonTemplate(RedisConnectionFactory connectionFactory, RedisSerializer<K> keySerializer, RedisJsonSerializer jsonSerializer, boolean enableTransactionSupport) {
		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
		Assert.notNull(keySerializer, "KeySerializer must not be null!");
		Assert.notNull(jsonSerializer, "JsonSerializer must not be null!");
		setConnectionFactory(connectionFactory);
		this.keySerializer = keySerializer;
		this.jsonSerializer = jsonSerializer;
		this.enableTransactionSupport = enableTransactionSupport;
	}

	/**
	 * Returns the key serializer used by this template.
	 *
	 * @return the key serializer used by this template.
	 */
	public RedisSerializer<K> getKeySerializer() {
		return keySerializer;
	}

	/**
	 * Returns the JSON serializer used by this template.
	 *
	 * @return the JSON serializer used by this template
	 */
	public RedisJsonSerializer getJsonSerializer() {
		return jsonSerializer;
	}

	/**
	 * Returns whether this template participates in ongoing transactions.
	 *
	 * @return {@literal true} if transaction support is enabled; {@literal false} otherwise.
	 */
	public boolean isEnableTransactionSupport() {
		return enableTransactionSupport;
	}

	/**
	 * Returns the operations performed on JSON values.
	 *
	 * @return JSON operations
	 */
	public JsonOperations<K> opsForJson() {
		return jsonOperations;
	}

	/**
	 * Executes the given action within a {@link RedisConnection} obtained from the configured
	 * {@link RedisConnectionFactory}, releasing the connection once the action completes.
	 *
	 * @param <T>    return type
	 * @param action callback object that specifies the Redis action; must not be {@literal null}.
	 * @return object returned by the action.
	 * @since 4.2
	 */
	<T extends @Nullable Object> T execute(RedisCallback<T> action) {

		Assert.notNull(action, "Callback object must not be null");

		RedisConnectionFactory factory = getRequiredConnectionFactory();
		RedisConnection connection = RedisConnectionUtils.getConnection(factory, enableTransactionSupport);

		try {
			return action.doInRedis(connection);
		} finally {
			RedisConnectionUtils.releaseConnection(connection, factory);
		}
	}

}
