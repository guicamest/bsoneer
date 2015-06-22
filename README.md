Bsoneer
========

A fast dependency injector for Android and Java.

### How to use

In a Maven project, one would include the `bsoneer` artifact in the dependencies section
of your `pom.xml` and the `bsoneer-compiler` artifact as either  an `optional` or `provided`
dependency:

```xml
<dependencies>
  <dependency>
    <groupId>com.sleepcamel.bsoneer</groupId>
    <artifactId>bsoneer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>com.sleepcamel.bsoneer</groupId>
    <artifactId>bsoneer-compiler</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <optional>true</optional>
  </dependency>
</dependencies>
```

If you do not use maven, gradle, ivy, or other build systems that consume maven-style binary
artifacts, they can be downloaded directly via the [Maven Central Repository][mavensearch].

Developer snapshots are available from [Sonatype's snapshot repository][bsoneer-snap], and
are built on a clean build of the GitHub project's master branch.

License
-------

    Copyright 2015 Sleepcamel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [mavensearch]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.sleepcamel.bsoneer%22
 [bsoneer-snap]: https://oss.sonatype.org/content/repositories/snapshots/com/sleepcamel/bsoneer/

