/*
 * Copyright (C) 2015 Sleepcamel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sleepcamel.bsoneer.processor.resolver;

import com.sleepcamel.bsoneer.processor.domain.Bean;

public interface PropertyResolver {
	/**
	 * Introspects the given bean and resolves its properties(getters, setters and ids)
	 * @param bean The {@link Bean} to introspect 
	 */
	void resolveProperties(Bean bean);
}
