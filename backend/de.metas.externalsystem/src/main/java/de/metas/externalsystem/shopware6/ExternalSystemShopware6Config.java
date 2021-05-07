/*
 * #%L
 * de.metas.externalsystem
 * %%
 * Copyright (C) 2021 metas GmbH
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

package de.metas.externalsystem.shopware6;

import de.metas.externalsystem.ExternalSystemParentConfigId;
import de.metas.externalsystem.IExternalSystemChildConfig;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;

@Value
public class ExternalSystemShopware6Config implements IExternalSystemChildConfig
{
	@NonNull
	ExternalSystemShopware6ConfigId id;
	@NonNull
	ExternalSystemParentConfigId parentId;
	@NonNull
	String baseUrl;
	@NonNull
	String clientId;
	@NonNull
	String clientSecret;
	@NonNull
	List<ExternalSystemShopware6ConfigMapping> externalSystemShopware6ConfigMappingList;
	@Nullable
	String bPartnerIdJSONPath;
	@Nullable
	String bPartnerLocationIdJSONPath;
	@Nullable
	String salesRepJSONPath;

	@Builder
	public ExternalSystemShopware6Config(final @NonNull ExternalSystemShopware6ConfigId id,
			final @NonNull ExternalSystemParentConfigId parentId,
			final @NonNull String baseUrl,
			final @NonNull String clientId,
			final @NonNull String clientSecret,
			final @NonNull List<ExternalSystemShopware6ConfigMapping> externalSystemShopware6ConfigMappingList,
			final @Nullable String bPartnerIdJSONPath,
			final @Nullable String bPartnerLocationIdJSONPath,
			final @Nullable String salesRepJSONPath)
	{
		this.id = id;
		this.parentId = parentId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.externalSystemShopware6ConfigMappingList = externalSystemShopware6ConfigMappingList;
		this.baseUrl = baseUrl;
		this.bPartnerIdJSONPath = bPartnerIdJSONPath;
		this.bPartnerLocationIdJSONPath = bPartnerLocationIdJSONPath;
		this.salesRepJSONPath = salesRepJSONPath;
	}

	public static ExternalSystemShopware6Config cast(@NonNull final IExternalSystemChildConfig childConfig)
	{
		return (ExternalSystemShopware6Config)childConfig;
	}
}
