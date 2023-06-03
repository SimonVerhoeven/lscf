package dev.simonverhoeven.lscf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.catalog.FunctionTypeUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import java.util.function.Supplier;
import java.util.random.RandomGeneratorFactory;

@SpringBootConfiguration
public class LscfApplication implements ApplicationContextInitializer<GenericApplicationContext> {
	public Supplier<String> generateRandomNumber() {
		return () -> "Your random number is: " +  RandomGeneratorFactory.of("Xoshiro256PlusPlus").create().nextInt();
	}

	public static void main(String[] args) {
		SpringApplication.run(LscfApplication.class, args);
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		context.registerBean("generateRandomNumber", FunctionRegistration.class,
				() -> new FunctionRegistration<>(generateRandomNumber()).type(FunctionTypeUtils.supplierType(String.class)));
	}
}
