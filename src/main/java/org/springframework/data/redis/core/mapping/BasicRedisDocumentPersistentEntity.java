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

import org.jspecify.annotations.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.keyvalue.core.mapping.BasicKeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.redis.core.RedisDocument;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link RedisDocumentPersistentEntity} implementation.
 *
 * @author Yordan Tsintsov
 * @param <T>
 * @since 4.2
 */
public class BasicRedisDocumentPersistentEntity<T>
		extends BasicKeyValuePersistentEntity<T, RedisDocumentPersistentProperty>
		implements RedisDocumentPersistentEntity<T> {

	private final TimeToLiveAccessor timeToLiveAccessor;

	/**
	 * Creates new {@link BasicRedisDocumentPersistentEntity}.
	 *
	 * @param information must not be {@literal null}.
	 * @param keySpaceResolver can be {@literal null}.
	 * @param timeToLiveAccessor must not be {@literal null}.
	 */
	public BasicRedisDocumentPersistentEntity(TypeInformation<T> information,
			@Nullable KeySpaceResolver keySpaceResolver, TimeToLiveAccessor timeToLiveAccessor) {
		super(information, keySpaceResolver);

		Assert.notNull(timeToLiveAccessor, "TimeToLiveAccessor must not be null");
		this.timeToLiveAccessor = timeToLiveAccessor;
	}

	@Override
	public String getIndexName() {

		RedisDocument annotation = findAnnotation(RedisDocument.class);

		if (annotation != null && StringUtils.hasText(annotation.indexName())) {
			return annotation.indexName();
		}

		return getKeySpace() + "Idx";
	}

	@Override
	public TimeToLiveAccessor getTimeToLiveAccessor() {
		return timeToLiveAccessor;
	}

	@Override
	public @Nullable RedisDocumentPersistentProperty getExplicitTimeToLiveProperty() {
		return getPersistentProperty(TimeToLive.class);
	}

	@Override
	protected @Nullable RedisDocumentPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(
			RedisDocumentPersistentProperty property) {

		Assert.notNull(property, "Property must not be null");

		if (!property.isIdProperty()) {
			return null;
		}

		RedisDocumentPersistentProperty currentIdProperty = getIdProperty();

		if (currentIdProperty == null) {
			return property;
		}

		boolean currentIdPropertyIsExplicit = currentIdProperty.isAnnotationPresent(Id.class);
		boolean newIdPropertyIsExplicit = property.isAnnotationPresent(Id.class);

		if (currentIdPropertyIsExplicit && newIdPropertyIsExplicit) {
			throw new MappingException(("Attempt to add explicit id property %s but already have a property %s"
					+ " registered as explicit id; Check your mapping configuration")
					.formatted(property.getName(), currentIdProperty.getName()));
		}

		if (!currentIdPropertyIsExplicit && !newIdPropertyIsExplicit) {
			throw new MappingException(("Attempt to add id property %s but already have a property %s registered as id;"
					+ " Check your mapping configuration").formatted(property.getName(), currentIdProperty.getName()));
		}

		if (newIdPropertyIsExplicit) {
			return property;
		}

		return null;
	}

}
