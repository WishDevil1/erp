package de.metas.server.config;

import de.metas.util.web.SwaggerUtil;
import io.swagger.v3.oas.models.OpenAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// import springfox.documentation.builders.PathSelectors;
// import springfox.documentation.spi.DocumentationType;
// import springfox.documentation.spring.web.plugins.Docket;

/*
 * #%L
 * de.metas.adempiere.adempiere.serverRoot.base
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Configuration
@EnableWebMvc
public class SwaggerConfig
{
	// @Bean
	// public Docket api()
	// {
	// 	return new Docket(DocumentationType.OAS_30)
	// 			.globalOperationParameters(ImmutableList.of(SwaggerUtil.SWAGGER_GLOBAL_AUTH_TOKEN_PARAMETER))
	// 			.select()
	// 			.paths(PathSelectors.any())
	// 			.build();
	// }

	@Bean
	public OpenAPI appOpenAPI() {
		return SwaggerUtil.createApiInfo("metasfresh application server REST API", "metasfresh REST API");
	}
}
