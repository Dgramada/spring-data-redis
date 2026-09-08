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

import org.springframework.data.core.TypeInformation;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.redis.core.TimeToLiveAccessor;

/**
 * Redis JSON document specific {@link MappingContext}.
 *
 * @author Yordan Tsintsov
 * @since 4.2
 */
public class RedisDocumentMappingContext
		extends KeyValueMappingContext<RedisDocumentPersistentEntity<?>, RedisDocumentPersistentProperty> {

	private final TimeToLiveAccessor timeToLiveAccessor;

	/**
	 * Creates new {@link RedisDocumentMappingContext}.
	 */
	public RedisDocumentMappingContext() {
		this.timeToLiveAccessor = new RedisDocumentTimeToLiveAccessor(this);
	}

	@Override
	protected <T> RedisDocumentPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
		return new BasicRedisDocumentPersistentEntity<>(typeInformation, getKeySpaceResolver(), timeToLiveAccessor);
	}

	@Override
	protected RedisDocumentPersistentProperty createPersistentProperty(Property property,
			RedisDocumentPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
		return new RedisDocumentPersistentProperty(property, owner, simpleTypeHolder);
	}

	@Override
	protected boolean shouldCreatePersistentEntityFor(TypeInformation<?> typeInformation) {
		if (typeInformation.isMap() || typeInformation.isCollectionLike()) {
			return false;
		}
		return super.shouldCreatePersistentEntityFor(typeInformation);
	}

}
