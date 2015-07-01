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
package com.sleepcamel.bsoneer.tests.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

import com.google.testing.compile.JavaFileObjects;
import com.sleepcamel.bsoneer.processor.BsonProcessor;

/**
 * Internal test utilities.
 */
public class ProcessorTestUtils {
  public static Iterable<? extends Processor> bsoneerProcessors() {
    return Arrays.asList(
        new BsonProcessor());
  }
  
  public static JavaFileObject codecFor(String clazz) throws IOException{
	  StringBuffer sb = new StringBuffer();
	  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ProcessorTestUtils.class.getResourceAsStream("codecTemplate.txt")));
	  String line;
	  while((line = bufferedReader.readLine()) != null){
		  sb.append(line).append("\n");
//		  $$CLASS_NAME$$
	  }
	  
	  return JavaFileObjects.forSourceString(clazz, sb.toString().replaceAll("\\$\\$CLASS_NAME\\$\\$", clazz));
  }
}
