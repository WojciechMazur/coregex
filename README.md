# coregex
[![Build Status](https://github.com/SimY4/coregex/workflows/Build%20and%20Test/badge.svg)](https://github.com/SimY4/coregex/actions?query=workflow%3A"Build+and+Test")
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Maven Central](https://img.shields.io/maven-central/v/com.github.simy4.coregex/coregex-core.svg)](https://search.maven.org/search?q=g:com.github.simy4.coregex)
[![Javadocs](http://www.javadoc.io/badge/com.github.simy4.coregex/coregex-core.svg)](http://www.javadoc.io/doc/com.github.simy4.coregex/coregex-core)

A handy utility for generating strings that match given regular expression criteria.

# Supported generators

- [Jqwik](https://jqwik.net/) 
- [JUnit Quickcheck](https://pholser.github.io/junit-quickcheck)
- [scalacheck](https://scalacheck.org/)

# Usage
## Jqwik
Include the following dependency into your project:

```groovy
testImplementation "com.github.simy4.coregex:coregex-jqwik"
```

Use the provided `Regex` annotation to generate a string that would match the regular expression predicate:

```java
class MyTest {
    @Property
    void myProperty(@ForAll @Regex("[a-zA-Z]{3}") String str) {
        assertThat(str).hasLength(3);
    }
}
```

## JUnit Quickcheck
Include the following dependency into your project:

```groovy
testImplementation "com.github.simy4.coregex:coregex-junit-quickcheck"
```

Use the provided `Regex` annotation to generate a string that would match the regular expression predicate:

```java
@RunWith(JUnitQuickcheck.class)
public class MyTest {
    @Property
    public void myProperty(@Regex("[a-zA-Z]{3}") String str) {
        assertThat(str).hasLength(3);
    }
}
```

## scalacheck
Include the following dependency into your project:

```scala
libraryDependencies ++= Seq("com.github.simy4.coregex" %% "coregex-scalacheck" % Test)
```

Use the provided `CoregexInstances` trait to constrain string arbitraries:

```scala
object MySpecification extends Properties("MySpecification") with CoregexInstances {
    property("myProperty") = forAll { (str: String Matching "[a-zA-Z]{3}") =>
      3 == str.length  
    }
}
```