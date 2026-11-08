package br.com.fiap.transitoclaro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Relogio injetavel (facilita testes que dependem do tempo). */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("TransitoClaro API")
                .version("1.0.0")
                .description("CitizenTech: informacao em tempo real sobre atrasos e lotacao de metro, trem e BRT, "
                        + "feita pelos proprios passageiros. O passageiro tambem cadastra seus trajetos e "
                        + "recebe um alerta antes de sair de casa."));
    }
}
