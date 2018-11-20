[![master](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=master)](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=master) [![develop](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=develop)](https://travis-ci.com/krizzdewizz/ngoy.svg?branch=develop)

# ngoy

A template engine for the JVM, based on the Angular+ component architecture.

ngoy == Template + JVM + Angular

enjoy ngoy!

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
}

public static void main(String[] args) {
    Ngoy.app(App.class)
            .build()
            .render(System.out);
}

// hello WORLD
```

Clone [ngoy-starter-web](https://github.com/krizzdewizz/ngoy-starter-web) to get started.

More docs about to come...
