package He_thong_quan_ly.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QuanCoffeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanCoffeeApplication.class, args);
	}

}
