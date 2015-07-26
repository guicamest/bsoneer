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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.sleepcamel.bsoneer.processor.domain.Bean;

public class PropertyResolvers implements Iterable<PropertyResolver>,
		PropertyResolver {

	private final List<PropertyResolver> resolvers;

	public PropertyResolvers() {
		resolvers = new ArrayList<PropertyResolver>();
		resolvers.add(new BeanResolver());
		resolvers.add(new BsoneeAnnotatedResolver());
		ServiceLoader<PropertyResolver> load = ServiceLoader.load(PropertyResolver.class);
		if ( load != null ){
			Iterator<PropertyResolver> iterator = load.iterator();
			while(iterator.hasNext()){
				resolvers.add(iterator.next());
			}
		}
	}

	@Override
	public Iterator<PropertyResolver> iterator() {
		return resolvers.iterator();
	}

	@Override
	public void resolveProperties(Bean bean) {
		/*
		if (ModelExt.hasAnnotation(bean.getElement(), Constants.IGNORE)) {
			return;
		}
		 */
		for (PropertyResolver resolver : resolvers) {
			resolver.resolveProperties(bean);
		}
	}
}
