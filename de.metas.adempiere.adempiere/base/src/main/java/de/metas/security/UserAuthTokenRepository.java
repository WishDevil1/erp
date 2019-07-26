package de.metas.security;

import java.util.List;
import java.util.UUID;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.service.ClientId;
import org.compiere.model.I_AD_User_AuthToken;
import org.springframework.stereotype.Repository;

import de.metas.cache.CCache;
import de.metas.organization.OrgId;
import de.metas.user.UserId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

@Repository
public class UserAuthTokenRepository
{
	private final CCache<String, UserAuthToken> authTokensByToken = CCache.newCache(I_AD_User_AuthToken.Table_Name + "#by#token", 50, CCache.EXPIREMINUTES_Never);

	public UserAuthToken getByToken(@NonNull final String token)
	{
		return authTokensByToken.getOrLoad(token, () -> retrieveByToken(token));
	}

	private UserAuthToken retrieveByToken(final String token)
	{
		final List<I_AD_User_AuthToken> userAuthTokens = Services.get(IQueryBL.class)
				.createQueryBuilder(I_AD_User_AuthToken.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_AD_User_AuthToken.COLUMN_AuthToken, token)
				.setLimit(2)
				.create()
				.list(I_AD_User_AuthToken.class);
		if (userAuthTokens.isEmpty())
		{
			throw new AdempiereException("Invalid token (1)");
		}
		else if (userAuthTokens.size() > 1)
		{
			throw new AdempiereException("Invalid token (2)");
		}

		return toUserAuthToken(userAuthTokens.get(0));
	}

	private static UserAuthToken toUserAuthToken(final I_AD_User_AuthToken userAuthTokenPO)
	{
		return UserAuthToken.builder()
				.userId(UserId.ofRepoId(userAuthTokenPO.getAD_User_ID()))
				.authToken(userAuthTokenPO.getAuthToken())
				.description(userAuthTokenPO.getDescription())
				.clientId(ClientId.ofRepoId(userAuthTokenPO.getAD_Client_ID()))
				.orgId(OrgId.ofRepoId(userAuthTokenPO.getAD_Org_ID()))
				.roleId(RoleId.ofRepoId(userAuthTokenPO.getAD_Role_ID()))
				.build();
	}

	public void beforeSave(final I_AD_User_AuthToken userAuthTokenPO)
	{
		if (Check.isEmpty(userAuthTokenPO.getAuthToken(), true))
		{
			userAuthTokenPO.setAuthToken(generateAuthTokenString());
		}

		toUserAuthToken(userAuthTokenPO); // make sure it's valid
	}

	private static String generateAuthTokenString()
	{
		return UUID.randomUUID().toString().replace("-", "");
	}

	public UserAuthToken retrieveByUserId(@NonNull final UserId userId, @NonNull final RoleId roleId)
	{
		final List<I_AD_User_AuthToken> userAuthTokens = Services.get(IQueryBL.class)
				.createQueryBuilder(I_AD_User_AuthToken.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_AD_User_AuthToken.COLUMN_AD_User_ID, userId)
				.addEqualsFilter(I_AD_User_AuthToken.COLUMN_AD_Role_ID, roleId)
				.setLimit(2)
				.create()
				.list(I_AD_User_AuthToken.class);

		if (userAuthTokens.isEmpty())
		{
			throw new AdempiereException("Invalid token (1)");
		}
		else if (userAuthTokens.size() > 1)
		{
			throw new AdempiereException("Invalid token (2)");
		}

		return toUserAuthToken(userAuthTokens.get(0));
	}
}
