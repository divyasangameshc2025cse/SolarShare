package PROJECT.SOLARSHARE;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class SolarshareApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarshareApplication.class, args);
	}

	@Bean
	public CommandLineRunner cleanLegacyColumns(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE households DROP COLUMN allocated_kwh");
			} catch (Exception ignored) {
				// Column already dropped or doesn't exist
			}
		};
	}
}
