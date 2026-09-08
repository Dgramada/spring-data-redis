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
package org.springframework.data.redis.core.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.redis.core.RedisDocument;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link TimeToLiveAccessor} implementation for Redis JSON documents.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
class RedisDocumentTimeToLiveAccessor implements TimeToLiveAccessor {

	private final RedisDocumentMappingContext mappingContext;

	private final Map<Class<?>, Optional<Long>> defaultTimeouts = new ConcurrentHashMap<>();
	private final Map<Class<?>, Optional<PersistentProperty<?>>> timeoutProperties =
			new ConcurrentHashMap<>();
	private final Map<Class<?>, Optional<Method>> timeoutMethods = new ConcurrentHashMap<>();

	/**
	 * Creates new {@link RedisDocumentTimeToLiveAccessor}
	 *
	 * @param mappingContext must not be {@literal null}.
	 */
	public RedisDocumentTimeToLiveAccessor(RedisDocumentMappingContext mappingContext) {

		Assert.notNull(mappingContext, "MappingContext must not be null");

		this.mappingContext = mappingContext;
	}

	@Override
	public @Nullable Long getTimeToLive(Object source) {

		Assert.notNull(source, "Source must not be null");

		if (source instanceof Class<?> type) {
			return resolveDefaultTimeout(type);
		}

		Class<?> type = source.getClass();
		PersistentProperty<?> ttlProperty = resolveTtlProperty(type);

		if (ttlProperty != null) {

			Long timeout = getTimeoutFromProperty(source, type, ttlProperty);

			if (timeout != null) {
				return timeout;
			}
		} else {

			Method timeoutMethod = resolveTimeoutMethod(type);

			if (timeoutMethod != null) {

				Long timeout = getTimeoutFromMethod(source, timeoutMethod);

				if (timeout != null) {
					return timeout;
				}
			}
		}

		return resolveDefaultTimeout(type);
	}

	@Override
	public boolean isExpiringEntity(Class<?> type) {

		Assert.notNull(type, "Type must not be null");

		Long defaultTimeout = resolveDefaultTimeout(type);

		if (defaultTimeout != null && defaultTimeout > 0) {
			return true;
		}

		return resolveTtlProperty(type) != null || resolveTimeoutMethod(type) != null;
	}

	private @Nullable Long getTimeoutFromProperty(Object source, Class<?> type,
			PersistentProperty<?> ttlProperty) {

		Object value = mappingContext.getRequiredPersistentEntity(type).getPropertyAccessor(source)
				.getProperty(ttlProperty);

		if (!(value instanceof Number number)) {
			return null;
		}

		TimeToLive ttl = ttlProperty.findAnnotation(TimeToLive.class);

		return TimeUnit.SECONDS.convert(number.longValue(), ttl != null ? ttl.unit() : TimeUnit.SECONDS);
	}

	private static @Nullable Long getTimeoutFromMethod(Object source, Method timeoutMethod) {

		TimeToLive ttl = AnnotationUtils.findAnnotation(timeoutMethod, TimeToLive.class);

		if (ttl == null) {
			return null;
		}

		ReflectionUtils.makeAccessible(timeoutMethod);

		try {

			Number timeout = (Number) timeoutMethod.invoke(source);

			return timeout != null ? TimeUnit.SECONDS.convert(timeout.longValue(), ttl.unit()) : null;
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException(
					"Not allowed to access method '%s': %s".formatted(timeoutMethod.getName(), ex.getMessage()), ex);
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException("Cannot invoke method '%s' without arguments: %s"
					.formatted(timeoutMethod.getName(), ex.getMessage()), ex);
		} catch (InvocationTargetException ex) {
			throw new IllegalStateException(
					"Cannot access method '%s': %s".formatted(timeoutMethod.getName(), ex.getMessage()), ex);
		}
	}

	private @Nullable Long resolveDefaultTimeout(Class<?> type) {

		Optional<Long> cached = defaultTimeouts.get(type);

		if (cached != null) {
			return cached.orElse(null);
		}

		RedisDocument document = mappingContext.getRequiredPersistentEntity(type).findAnnotation(RedisDocument.class);
		Long timeout = document != null && document.timeToLive() > 0 ? document.timeToLive() : null;

		defaultTimeouts.put(type, Optional.ofNullable(timeout));

		return timeout;
	}

	private @Nullable PersistentProperty<?> resolveTtlProperty(Class<?> type) {

		Optional<PersistentProperty<?>> cached = timeoutProperties.get(type);

		if (cached != null) {
			return cached.orElse(null);
		}

		RedisDocumentPersistentProperty ttlProperty = mappingContext.getRequiredPersistentEntity(type)
				.getPersistentProperty(TimeToLive.class);

		timeoutProperties.put(type, Optional.ofNullable(ttlProperty));

		return ttlProperty;
	}

	private @Nullable Method resolveTimeoutMethod(Class<?> type) {

		Optional<Method> cached = timeoutMethods.get(type);

		if (cached != null) {
			return cached.orElse(null);
		}

		AtomicReference<@Nullable Method> timeoutMethod = new AtomicReference<>();
		ReflectionUtils.doWithMethods(type, timeoutMethod::set,
				method -> ClassUtils.isAssignable(Number.class, method.getReturnType())
						&& AnnotationUtils.findAnnotation(method, TimeToLive.class) != null);

		timeoutMethods.put(type, Optional.ofNullable(timeoutMethod.get()));

		return timeoutMethod.get();
	}

}
