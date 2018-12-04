package {{pack}};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class {{className}}WebApplication {

	public static void main(String[] args) {
		SpringApplication.run({{className}}WebApplication.class, args);
	}
}
