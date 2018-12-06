# [ngoy](https://krizzdewizz.github.io/ngoy-website)


A template engine for the JVM, based on the Angular component architecture.

ngoy == Template + JVM + Angular

enjoy ngoy!

[![master](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=master)](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=master) [![develop](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=develop)](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=develop)

## Examples

### Simple

```java
Ngoy.renderString("hello {{name}}", Context.of("name", "world"), System.out);

// hello world
```

### App with components, directives, pipes and services

```java
@Component(selector = "app", template = "hello {{ name | uppercase }}")
public class App {
    public String name = "world";

    public static void main(String[] args) {
        Ngoy.app(App.class)
                .build()
                .render(System.out);
    }
}

// hello WORLD
```

## Getting Started

Clone [ngoy-starter-web](https://github.com/krizzdewizz/ngoy-starter-web) to get started.

Run the complete [Tour of Heroes](https://github.com/krizzdewizz/ngoy-tour-of-heroes) tutorial rewrite using ngoy.

Checkout the [examples collection](https://github.com/krizzdewizz/ngoy-examples).

Visit the ngoy [website](https://krizzdewizz.github.io/ngoy-website).

## Distribution

You can download the ngoy binaries from [here](https://github.com/krizzdewizz/ngoy/releases) or via jitpack.io:

`build.gradle`:
```
repositories {
	maven { url "https://jitpack.io" }
}

dependencies {
	implementation("com.github.krizzdewizz:ngoy:1.0.0-rc3")
}
```

## Feedback

We would love to hear your feedback! Please file an issue [here](https://github.com/krizzdewizz/ngoy/issues).

## Development

[Travis CI](https://travis-ci.com/krizzdewizz/ngoy)

### Build
```
gradle clean buildAll
```

