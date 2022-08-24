# testcontainers-junit5
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/zorinkirill/testcontainers-junit5/tree/master.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/zorinkirill/testcontainers-junit5/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/zorinkirill/testcontainers-junit5/badge.svg?branch=master)](https://coveralls.io/github/zorinkirill/testcontainers-junit5?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kappadrive.testcontainers/testcontainers-junit5/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.kappadrive.testcontainers/testcontainers-junit5)

Alternative JUnit5 Jupiter Extenstions for Testcontainers Java library

## JUnit5 Jupiter Extensions
This library is an alternative to the original https://github.com/testcontainers/testcontainers-java
`testcontainers-junit-jupiter` module and `@Testcontainers` + `@Container` annotations.

This library allows creating/using custom annotations which can be later reused between multiple tests/test classes.
Coordinates:
```
com.kappadrive.testcontainers:testcontainers-junit5:@version
```

## JUnit4 mock support
Original `testcontainers-java` library requires dependency on JUnit4. If you do not plan to use JUnit4 directly,
instead you can use next mock dependency:
```
com.kappadrive.testcontainers:testcontainers-junit4-mock:@version
```
Disclaimer: It will help only to compile your code, but not to run with JUnit4.

## Core features
### @WithTestContainerSupport
Core annotation is `@WithTestContainerSupport` together with interface `TestContainerFactory`.
It allows to register any custom testcontainer. This is meta annotation, so it requires another custom annotation to work.
Example:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithTestContainerSupport(MyTestContainerFactory.class)
// you can easily support multiple containers of same type on same level
@Repeatable(WithMyTestContainers.class)
public @interface WithMyTestContainer {
    String value();
}
```
Which can be later used on tests:
```java
@WithMyTestContainer("someName")
class SomeTest {
    // or directly
    @Test
    @WithMyTestContainer("anotherName")
    void test() {
        // ...
    }
}
```
`@WithTestContainerSupport` can work on class or method level in JUnit5 Jupiter - test container will be started only once,
when first test/container annotated with it will be started. After all tests will be finished - container will be stopped.

### TestContainerFactory
This interface needs to be implemented in order to create custom container for tests.
It has next methods:
* `getContainerName()` - returns *unique* container name. If container with same name has already started
\- it will not be started second time.
* `createContainer()` - creates new `GenericContainer<?>`.

Custom factory class can implement `AnnotationConsumer` interface, which will inject custom annotation
right after instantiation and before any other methods will be called.
It could be used for parametrization new container via this annotation and highly recommended to use at least for container name:
```java
class MyTestContainerFactory implements TestContainerFactory, AnnotationConsumer<WithMyContainer> {

    private WithMyContainer withMyContainer;

    @Override
    public void accept(WithMyContainer withMyContainer) {
        this.withMyContainer = withMyContainer;
    }

    @Override
    public String getContainerName() {
        return withMyContainer.value();
    }

    @Override
    public GenericContainer<?> createContainer() {
        // return created container based on annotation properties
        // for example: db username, password, external service port, etc.
    }

}
```

### @Container
In some tests it might be required to access the created container. It could be achieved next ways:
* Direct parameter name via a constructor or test method parameter (This option will require compiler option `-parameters` in tests):
```java
class SomeTests {
    @Test
    @WithMyContainer("container")
    void test(GenericContainer<?> container) {
        assertThat(container.isRunning()).isTrue();
    }
}
```
```java
// constructor injection always requires class level annotation
@WithMyContainer("container")
class SomeTests {
    private final GenericContainer<?> container;

    SomeTests(GenericContainer<?> container) {
        this.container = container;
    }

    @Test
    void test() {
        assertThat(container.isRunning()).isTrue();
    }
}
```
* Annotation `@Container(...)` with specified container name:
```java
class SomeTests {
    @Test
    @WithMyContainer("goodName")
    void test(@Container("goodName") GenericContainer<?> container) {
        assertThat(container.isRunning()).isTrue();
    }
}
```
```java
// constructor injection always requires class level annotation
@WithMyContainer("goodName")
class SomeTests {
    private final GenericContainer<?> container;

    SomeTests(@Container("goodName") GenericContainer<?> container) {
        this.container = container;
    }

    @Test
    void test() {
        assertThat(container.isRunning()).isTrue();
    }
}
```
Any custom container type (which matches with `TestContainerFactory#createContainer` runtime class)
and any superclass of it can be used instead of `GenericContainer<?>`.

### @SkipIfNoDocker
Sometimes docker environment (which is required for work) could not be supported
and if you want to skip container tests in this case, you can use `@SkipIfNoDocker` annotation:
```java
@SkipIfNoDocker
@WithMyTestContainer("container")
class SomeTests {
    // ...
}
```

### Accessing testcontainers via Jupiter ExecutionContext
It is possible to access created containers context in any custom `Extension`.
For example:
```java
class MyExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        TestContainers testContainers = TestContainers.getTestContainers(context);
        // do whatever is required
    }
}
```
You just need to support proper ordering of extensions:
```java
@WithMyContainer("container")
@SomeContainerConsumer
class SomeTests {
    // ...
}
```

### @MapToSystemProperty + @WithPropertyMapper
In most of the cases - you want to start container and connect it with tested application.
There is builtin support to map existing container properties into `System`:
```java
@WithMyContainer("container")
// this will use container real host and mapped port for exposed 8080
@MapToSystemProperty(container = "container", property = "server.host", value = "${host}:${port:8080}")
// some extensions consuming new system property
class SomeTests {
    // ...
}
```

Available patterns can be configured via `@WithPropertyMapper`.
Next are supported by default:
* `${host}` - will replace all entries with real container host.
* `${port:8888}` - will replace all entries with actual port mapped to given exposed port.

It is allowed to develop custom property mappers, especially together with custom annotation:
```java
@WithTestContainerSupport(MyDbFactory.class)
@WithPropertyMapper({MyDbUsernamePropertyResolver.class, MyDbPasswordPropertyResolver.class})
public @interface WithMyDb {
    String value();
    String username();
    String password();
}
```

To do it you need to implement `PropertyResolver<?>` interface. During system property resolving
exact container class and all available property resolver generic classes will be matched and applied or skipped.
So, it is preferable to use as lower supertype for your resolver as possible.

### Builtin container annotations
Next container annotations are available by default and can be directly used in tests:
* `@WithContainerFromDockerFile` - creates a new container based on `Dockerfile` and any resources available
in the same resource directory. Uses docker command `HEALTHCHECK` to ensure that container is started.
Therefore, all additional configuration could be done completely by the docker. Note, that due to implementation of Java
resources could not be located just in resources root `/` and all of them should be either in `main` or `test`
(or any other) scope. Otherwise, some of them might be missed during container startup. Example:
```java
@WithContainerFromDockerfile(value = "my-db", resourcePath = "db/my-db", exposedPort = 8081)
class SomeTests {
    // ...
}
```

### Testing with SpringBoot
One of main intentions of this library - to share common testing logic between multiple tests.
If you are testing SpringBoot application, you also may want to pass some properties based on started containers.
You can achieve it next way (considering `@WithMyDb` is already created):
```java
@WithMyDb(value = "db", username = "sa", password = "sa")
@MapToSystemProperty(container = "db", property = "my.db.url", value = "${url}")
@MapToSystemProperty(container = "db", property = "my.db.username", value = "${username}")
@MapToSystemProperty(container = "db", property = "my.db.password", value = "${password}")
// or another custom annotations which can register system properties required for spring boot app
@SpringBootTest
// register some pretest cleanups
public @interface MyAppTest {
}
// ...
// container is shared
@MyAppTest
class SomeTest {
    //...
}
//...
// container is shared
@MyAppTest
class SomeAnotherTest {
    //...
}
```

## Compatibility with Testcontainers library
| Version | Testcontainers version |
| ------- | ---------------------- |
| 0.1.x   | 1.17.3                 |