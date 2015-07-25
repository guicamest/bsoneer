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

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sleepcamel.bsoneer.processor.domain.Bean;
import com.sleepcamel.bsoneer.processor.domain.ElementExt;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.domain.Visibility;

public class BeanResolver implements PropertyResolver {
	@Override
	public void resolveProperties(Bean bean) {
		Bean topBean = bean;
		TypeElement type = bean.getElement();

		while(type != null){
			for (Element enclosed : type.getEnclosedElements()) {
				if (ElementExt.isProperty(enclosed) && ElementExt.getVisibility(enclosed) != Visibility.PRIVATE) {
					String name = ElementExt.getPropertyName(enclosed);
					Property property = bean.property(name);
					if (ElementExt.isGetter(enclosed)) {
						property.setGetter(enclosed);
					} else if (ElementExt.isSetter(enclosed)) {
						property.setSetter(enclosed);
					} else {
						property.setField(enclosed);
					}
	
					if (ElementExt.hasAnnotation(enclosed, Deprecated.class.getName())) {
						property.setDeprecated(true);
					}
				}
			}
			bean = bean.getSuperclass().orNull();
			if ( bean != null ){
				type = bean.getElement();
			}else{
				type = null;
			}
		}
		topBean.resolveRawProperties();
	}

}
