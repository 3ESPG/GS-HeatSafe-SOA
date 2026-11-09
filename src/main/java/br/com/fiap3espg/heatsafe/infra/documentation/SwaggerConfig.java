package br.com.fiap3espg.heatsafe.infra.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HeatSafe Clima API")
                        .version("1.0.0")
                        .description("""
                                Serviço de dados climáticos e previsão de dias de calor perigoso do **HeatSafe** \
                                (FIAP - SOA e Web Services - Global Solution 2026.2 - CitizenTech).

                                Guarda as observações diárias reais do INMET (estação A701 - São Paulo/Mirante, 2015-2025), \
                                calcula o índice de calor (NOAA) e prevê se o dia seguinte será perigoso (índice ≥ 32 °C). \
                                A API C# do HeatSafe consulta `GET /previsao/amanha` para ligar o "modo vigilância".""")
                        .contact(new Contact()
                                .name("Equipe HeatSafe - 3ESPG")
                                .url("https://github.com/3ESPG"))
                        .license(new License()
                                .name("Dados climáticos: INMET (dados públicos)")
                                .url("https://portal.inmet.gov.br/dadoshistoricos")));
    }
}
