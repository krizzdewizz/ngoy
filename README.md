# ngoy

A template engine for the JVM, based on the Angular+ component architecture.

ngoy == Template + JVM + Angular

enjoy ngoy!

## Simple

```java
Ngoy.renderString("hello {{name}}", Context.of("name", "world"), System.out);
// output: hello world
```

## App with components, directives, pipes and services

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
// output: hello WORLD
```

See also [ngoy-starter-web](https://github.com/krizzdewizz/ngoy-starter-web)

More docs about to come...
