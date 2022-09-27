package guru.sfg.beer.inventory.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication//(exclude = ArtemisAutoConfiguration.class)
public class BeerInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeerInventoryApplication.class, args);
    }

}
