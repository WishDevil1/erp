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

package de.metas.externalsystem;

import com.google.common.collect.ImmutableList;
import de.metas.externalsystem.alberta.ExternalSystemAlbertaConfigId;
import de.metas.externalsystem.model.I_ExternalSystem_Config;
import de.metas.externalsystem.model.I_ExternalSystem_Config_Alberta;
import de.metas.externalsystem.model.I_ExternalSystem_Config_RabbitMQ_HTTP;
import de.metas.externalsystem.model.I_ExternalSystem_Config_Shopware6;
import de.metas.externalsystem.model.I_ExternalSystem_Config_Shopware6Mapping;
import de.metas.externalsystem.model.I_ExternalSystem_Config_WooCommerce;
import de.metas.externalsystem.model.X_ExternalSystem_Config;
import de.metas.externalsystem.other.ExternalSystemOtherConfigId;
import de.metas.externalsystem.other.ExternalSystemOtherConfigRepository;
import de.metas.externalsystem.rabbitmqhttp.ExternalSystemRabbitMQConfigId;
import de.metas.externalsystem.shopware6.ExternalSystemShopware6Config;
import de.metas.externalsystem.shopware6.ExternalSystemShopware6ConfigId;
import de.metas.externalsystem.shopware6.ProductLookup;
import de.metas.externalsystem.woocommerce.ExternalSystemWooCommerceConfigId;
import org.adempiere.test.AdempiereTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.metas.externalsystem.model.X_ExternalSystem_Config_Shopware6Mapping.ISINVOICEEMAILENABLED_Yes;
import static de.metas.externalsystem.other.ExternalSystemOtherConfigRepositoryTest.createExternalConfigParameterRecord;
import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.*;

class ExternalSystemConfigRepoTest
{

	private ExternalSystemConfigRepo externalSystemConfigRepo;

	@BeforeEach
	void beforeEach()
	{
		AdempiereTestHelper.get().init();
		externalSystemConfigRepo = new ExternalSystemConfigRepo(new ExternalSystemOtherConfigRepository());
	}

	@BeforeAll
	static void initStatic()
	{
		start(AdempiereTestHelper.SNAPSHOT_CONFIG);
	}

	@AfterAll
	static void afterAll()
	{
		validateSnapshots();
	}

	@Test
	void externalSystem_Config_Alberta_getById()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Alberta);
		saveRecord(parentRecord);

		final I_ExternalSystem_Config_Alberta childRecord = newInstance(I_ExternalSystem_Config_Alberta.class);
		childRecord.setApiKey("apiKey");
		childRecord.setBaseURL("baseUrl");
		childRecord.setTenant("tenant");
		childRecord.setExternalSystemValue("testAlbertaValue");
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		// when
		final ExternalSystemAlbertaConfigId id = ExternalSystemAlbertaConfigId.ofRepoId(childRecord.getExternalSystem_Config_Alberta_ID());
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getById(id);

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Shopware6_getById()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Shopware6);
		saveRecord(parentRecord);

		final I_ExternalSystem_Config_Shopware6 childRecord = newInstance(I_ExternalSystem_Config_Shopware6.class);
		childRecord.setBaseURL("baseUrl");
		childRecord.setClient_Secret("secret");
		childRecord.setClient_Id("id");
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		childRecord.setExternalSystemValue("testShopware6Value");
		childRecord.setFreightCost_NormalVAT_Rates("1,2");
		childRecord.setFreightCost_Reduced_VAT_Rates("3,4");
		childRecord.setJSONPathSalesRepID("/test/salesrep");
		childRecord.setJSONPathConstantBPartnerLocationID("JSONPathConstantBPartnerLocationID");
		childRecord.setJSONPathEmail("JSONPathEmail");
		childRecord.setJSONPathMetasfreshID("JSONPathMetasfreshID");
		childRecord.setJSONPathShopwareID("JSONPathShopwareID");
		childRecord.setM_FreightCost_NormalVAT_Product_ID(20);
		childRecord.setM_FreightCost_ReducedVAT_Product_ID(30);
		childRecord.setM_PriceList_ID(40);
		childRecord.setProductLookup(ProductLookup.ProductNumber.getCode());
		saveRecord(childRecord);

		// when
		final ExternalSystemShopware6ConfigId id = ExternalSystemShopware6ConfigId.ofRepoId(childRecord.getExternalSystem_Config_Shopware6_ID());
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getById(id);

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_RabbitMQ_getById()
	{
		// given
		final I_ExternalSystem_Config parentRecord = ExternalSystemTestUtil.createI_ExternalSystem_ConfigBuilder()
				.type(ExternalSystemType.RabbitMQ.getCode())
				.build();

		final I_ExternalSystem_Config_RabbitMQ_HTTP childRecord = ExternalSystemTestUtil.createRabbitMQConfigBuilder()
				.externalSystemConfigId(parentRecord.getExternalSystem_Config_ID())
				.value("testRabbitMQValue")
				.isSyncBPartnerToRabbitMQ(true)
				.build();

		// when
		final ExternalSystemRabbitMQConfigId id = ExternalSystemRabbitMQConfigId.ofRepoId(childRecord.getExternalSystem_Config_RabbitMQ_HTTP_ID());
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getById(id);

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Shopware6_getTypeAndValue()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Shopware6);
		saveRecord(parentRecord);

		final String value = "testShopware6Value";

		final I_ExternalSystem_Config_Shopware6 childRecord = newInstance(I_ExternalSystem_Config_Shopware6.class);
		childRecord.setBaseURL("baseUrl");
		childRecord.setClient_Secret("secret");
		childRecord.setClient_Id("id");
		childRecord.setJSONPathSalesRepID("/test/salesrep");
		childRecord.setExternalSystemValue(value);
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		childRecord.setProductLookup(ProductLookup.ProductNumber.getCode());
		saveRecord(childRecord);

		// when
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getByTypeAndValue(ExternalSystemType.Shopware6, value)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemParentConfig found!"));

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Alberta_getByTypeAndValue()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Alberta);
		saveRecord(parentRecord);

		final String value = "testAlbertaValue";

		final I_ExternalSystem_Config_Alberta childRecord = newInstance(I_ExternalSystem_Config_Alberta.class);
		childRecord.setApiKey("apiKey");
		childRecord.setBaseURL("baseUrl");
		childRecord.setTenant("tenant");
		childRecord.setExternalSystemValue(value);
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		// when
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getByTypeAndValue(ExternalSystemType.Alberta, value)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemParentConfig found!"));

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_RabbitMQ_getByTypeAndValue()
	{
		// given
		final I_ExternalSystem_Config parentRecord = ExternalSystemTestUtil.createI_ExternalSystem_ConfigBuilder()
				.type(ExternalSystemType.RabbitMQ.getCode())
				.build();

		final String value = "testRabbitMQValue";

		ExternalSystemTestUtil.createRabbitMQConfigBuilder()
				.externalSystemConfigId(parentRecord.getExternalSystem_Config_ID())
				.value(value)
				.isSyncBPartnerToRabbitMQ(true)
				.isAutoSendWhenCreatedByUserGroup(true)
				.subjectCreatedByUserGroupId(1)
				.build();

		// when
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getByTypeAndValue(ExternalSystemType.RabbitMQ, value)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemParentConfig found!"));

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Alberta_getByTypeAndValue_wrongType()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Alberta);
		saveRecord(parentRecord);

		final String value = "testAlbertaValue";

		final I_ExternalSystem_Config_Alberta childRecord = newInstance(I_ExternalSystem_Config_Alberta.class);
		childRecord.setApiKey("apiKey");
		childRecord.setBaseURL("baseUrl");
		childRecord.setTenant("tenant");
		childRecord.setExternalSystemValue(value);
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		// when
		final Optional<ExternalSystemParentConfig> externalSystemParentConfig = externalSystemConfigRepo.getByTypeAndValue(ExternalSystemType.Shopware6, value);

		assertThat(externalSystemParentConfig).isEmpty();
	}

	@Test
	void externalSystem_Config_Alberta_getByTypeAndParent()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Alberta);
		saveRecord(parentRecord);

		final String value = "testAlbertaValue";

		final I_ExternalSystem_Config_Alberta childRecord = newInstance(I_ExternalSystem_Config_Alberta.class);
		childRecord.setApiKey("apiKey");
		childRecord.setBaseURL("baseUrl");
		childRecord.setTenant("tenant");
		childRecord.setExternalSystemValue(value);
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		final ExternalSystemParentConfigId externalSystemParentConfigId = ExternalSystemParentConfigId.ofRepoId(parentRecord.getExternalSystem_Config_ID());
		// when
		final IExternalSystemChildConfig result = externalSystemConfigRepo.getChildByParentIdAndType(externalSystemParentConfigId, ExternalSystemType.Alberta)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemChildConfig found!"));

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId().getRepoId()).isEqualTo(childRecord.getExternalSystem_Config_Alberta_ID());
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Shopware6_getByTypeAndParent()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Shopware6);
		saveRecord(parentRecord);

		final String value = "testShopware6Value";

		final I_ExternalSystem_Config_Shopware6 childRecord = newInstance(I_ExternalSystem_Config_Shopware6.class);
		childRecord.setBaseURL("baseUrl");
		childRecord.setClient_Secret("secret");
		childRecord.setClient_Id("id");
		childRecord.setExternalSystemValue(value);
		childRecord.setJSONPathSalesRepID("/test/salesrep");
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		childRecord.setProductLookup(ProductLookup.ProductNumber.getCode());
		saveRecord(childRecord);

		final I_ExternalSystem_Config_Shopware6Mapping childMappingRecord = newInstance(I_ExternalSystem_Config_Shopware6Mapping.class);
		childMappingRecord.setC_PaymentTerm_ID(10000);
		childMappingRecord.setC_DocTypeOrder_ID(10000);
		childMappingRecord.setPaymentRule("K");
		childMappingRecord.setSeqNo(10);
		childMappingRecord.setSW6_Customer_Group("testWithAnä");
		childMappingRecord.setSW6_Payment_Method("test");
		childMappingRecord.setDescription("test");
		childMappingRecord.setExternalSystem_Config_Shopware6_ID(childRecord.getExternalSystem_Config_Shopware6_ID());
		childMappingRecord.setIsInvoiceEmailEnabled(ISINVOICEEMAILENABLED_Yes);
		childMappingRecord.setBPartner_IfExists("UPDATE_MERGE");
		childMappingRecord.setBPartner_IfNotExists("FAIL");
		childMappingRecord.setBPartnerLocation_IfExists("DONT_UPDATE");
		childMappingRecord.setBPartnerLocation_IfNotExists("CREATE");
		saveRecord(childMappingRecord);

		final ExternalSystemParentConfigId externalSystemParentConfigId = ExternalSystemParentConfigId.ofRepoId(parentRecord.getExternalSystem_Config_ID());
		// when
		final IExternalSystemChildConfig result = externalSystemConfigRepo.getChildByParentIdAndType(externalSystemParentConfigId, ExternalSystemType.Shopware6)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemChildConfig found!"));

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId().getRepoId()).isEqualTo(childRecord.getExternalSystem_Config_Shopware6_ID());
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Other_Config_getById()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Other);
		saveRecord(parentRecord);

		final ExternalSystemParentConfigId externalSystemParentConfigId = ExternalSystemParentConfigId.ofRepoId(parentRecord.getExternalSystem_Config_ID());

		createExternalConfigParameterRecord(externalSystemParentConfigId, "name1", "value1");
		createExternalConfigParameterRecord(externalSystemParentConfigId, "name2", "value2");

		final ExternalSystemOtherConfigId otherConfigId = ExternalSystemOtherConfigId.ofExternalSystemParentConfigId(externalSystemParentConfigId);

		// when
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getById(otherConfigId);

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Woocommerce_getById()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_WooCommerce);
		saveRecord(parentRecord);

		final I_ExternalSystem_Config_WooCommerce childRecord = newInstance(I_ExternalSystem_Config_WooCommerce.class);
		childRecord.setCamelHttpResourceAuthKey("authKey");
		childRecord.setExternalSystemValue("testWoocommerceValue");
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		// when
		final ExternalSystemWooCommerceConfigId id = ExternalSystemWooCommerceConfigId.ofRepoId(childRecord.getExternalSystem_Config_WooCommerce_ID());
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getById(id);

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Woocommerce_getTypeAndValue()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_WooCommerce);
		saveRecord(parentRecord);

		final String value = "testWoocommerceValue";

		final I_ExternalSystem_Config_WooCommerce childRecord = newInstance(I_ExternalSystem_Config_WooCommerce.class);
		childRecord.setExternalSystemValue(value);
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		// when
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getByTypeAndValue(ExternalSystemType.WOO, value)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemParentConfig found!"));

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Woocommerce_getByTypeAndValue_wrongType()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_WooCommerce);
		saveRecord(parentRecord);

		final String value = "testWoocommerceValue";

		final I_ExternalSystem_Config_WooCommerce childRecord = newInstance(I_ExternalSystem_Config_WooCommerce.class);
		childRecord.setCamelHttpResourceAuthKey("apiKey");
		childRecord.setExternalSystemValue(value);
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		saveRecord(childRecord);

		// when
		final Optional<ExternalSystemParentConfig> externalSystemParentConfig = externalSystemConfigRepo.getByTypeAndValue(ExternalSystemType.Shopware6, value);

		//then
		assertThat(externalSystemParentConfig).isEmpty();
	}

	@Test
	void externalSystem_Config_Shopware6_getByQuery()
	{
		// given
		final I_ExternalSystem_Config parentRecord = newInstance(I_ExternalSystem_Config.class);
		parentRecord.setName("name");
		parentRecord.setType(X_ExternalSystem_Config.TYPE_Shopware6);
		parentRecord.setIsActive(false);
		saveRecord(parentRecord);

		final I_ExternalSystem_Config_Shopware6 childRecord = newInstance(I_ExternalSystem_Config_Shopware6.class);
		childRecord.setBaseURL("baseUrl");
		childRecord.setClient_Secret("secret");
		childRecord.setClient_Id("id");
		childRecord.setJSONPathSalesRepID("/test/salesrep");
		childRecord.setExternalSystemValue("testShopware6Value");
		childRecord.setExternalSystem_Config_ID(parentRecord.getExternalSystem_Config_ID());
		childRecord.setIsActive(false);
		childRecord.setProductLookup(ProductLookup.ProductNumber.getCode());
		saveRecord(childRecord);

		final ExternalSystemConfigQuery query = ExternalSystemConfigQuery.builder()
				.parentConfigId(ExternalSystemParentConfigId.ofRepoId(parentRecord.getExternalSystem_Config_ID()))
				.isActive(false)
				.build();

		// when
		final ExternalSystemParentConfig result = externalSystemConfigRepo.getByQuery(ExternalSystemType.Shopware6, query)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemParentConfig found!"));

		// then
		assertThat(result).isNotNull();
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_Shopware6_store()
	{
		// given
		final I_ExternalSystem_Config initialParentRecord = newInstance(I_ExternalSystem_Config.class);
		initialParentRecord.setName("name");
		initialParentRecord.setType(X_ExternalSystem_Config.TYPE_Shopware6);
		initialParentRecord.setIsActive(false);
		saveRecord(initialParentRecord);

		final I_ExternalSystem_Config_Shopware6 initialChildRecord = newInstance(I_ExternalSystem_Config_Shopware6.class);
		initialChildRecord.setBaseURL("baseUrl");
		initialChildRecord.setClient_Secret("secret");
		initialChildRecord.setClient_Id("id");
		initialChildRecord.setJSONPathSalesRepID("/test/salesrep");
		initialChildRecord.setExternalSystemValue("testShopware6Value");
		initialChildRecord.setExternalSystem_Config_ID(initialParentRecord.getExternalSystem_Config_ID());
		initialChildRecord.setProductLookup(ProductLookup.ProductNumber.getCode());
		initialChildRecord.setIsActive(false);
		saveRecord(initialChildRecord);

		final ExternalSystemParentConfig parentConfig = externalSystemConfigRepo.getById(ExternalSystemShopware6ConfigId.ofRepoId(initialChildRecord.getExternalSystem_Config_Shopware6_ID()));

		final String baseURL = "new-baseURL";
		final String clientId = "new-clientId";
		final String clientSecret = "new-clientSecret";

		final ExternalSystemShopware6Config childConfig = ExternalSystemShopware6Config.cast(parentConfig.getChildConfig())
				.toBuilder()
				.baseUrl(baseURL)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.isActive(true)
				.build();

		final ExternalSystemParentConfig updatedParentConfig = parentConfig.toBuilder()
				.isActive(true)
				.childConfig(childConfig)
				.build();
		// when
		externalSystemConfigRepo.saveConfig(updatedParentConfig);

		// then
		final ExternalSystemParentConfig updatedChildConfig = externalSystemConfigRepo.getById(ExternalSystemShopware6ConfigId.ofRepoId(initialChildRecord.getExternalSystem_Config_Shopware6_ID()));
		assertThat(updatedChildConfig).isNotNull();
		expect(updatedChildConfig).toMatchSnapshot();

		assertThat(updatedChildConfig.getIsActive()).isTrue();

		final ExternalSystemShopware6Config shopware6Config = ExternalSystemShopware6Config.cast(updatedChildConfig.getChildConfig());
		assertThat(shopware6Config.getBaseUrl()).isEqualTo(baseURL);
		assertThat(shopware6Config.getClientId()).isEqualTo(clientId);
		assertThat(shopware6Config.getClientSecret()).isEqualTo(clientSecret);
		assertThat(shopware6Config.getIsActive()).isTrue();
	}

	@Test
	void externalSystem_Config_RabbitMQ_getByTypeAndParent()
	{
		// given
		final I_ExternalSystem_Config parentRecord = ExternalSystemTestUtil.createI_ExternalSystem_ConfigBuilder()
				.type(ExternalSystemType.RabbitMQ.getCode())
				.build();

		final I_ExternalSystem_Config_RabbitMQ_HTTP childRecord = ExternalSystemTestUtil.createRabbitMQConfigBuilder()
				.externalSystemConfigId(parentRecord.getExternalSystem_Config_ID())
				.value("testRabbitMQValue")
				.isSyncBPartnerToRabbitMQ(true)
				.build();

		final ExternalSystemParentConfigId externalSystemParentConfigId = ExternalSystemParentConfigId.ofRepoId(parentRecord.getExternalSystem_Config_ID());
		// when
		final IExternalSystemChildConfig result = externalSystemConfigRepo.getChildByParentIdAndType(externalSystemParentConfigId, ExternalSystemType.RabbitMQ)
				.orElseThrow(() -> new RuntimeException("Something went wrong, no ExternalSystemChildConfig found!"));

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId().getRepoId()).isEqualTo(childRecord.getExternalSystem_Config_RabbitMQ_HTTP_ID());
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_getActiveByType_RabbitMQ()
	{
		// given
		final I_ExternalSystem_Config parentRecord = ExternalSystemTestUtil.createI_ExternalSystem_ConfigBuilder()
				.type(ExternalSystemType.RabbitMQ.getCode())
				.build();

		ExternalSystemTestUtil.createRabbitMQConfigBuilder()
				.externalSystemConfigId(parentRecord.getExternalSystem_Config_ID())
				.build();

		// when
		final ImmutableList<ExternalSystemParentConfig> result = externalSystemConfigRepo.getActiveByType(ExternalSystemType.RabbitMQ);

		// then
		assertThat(result).isNotEmpty();
		assertThat(result.size()).isEqualTo(1);
		expect(result).toMatchSnapshot();
	}

	@Test
	void externalSystem_Config_getActiveByType_NoRecord()
	{
		// given
		final I_ExternalSystem_Config parentRecord = ExternalSystemTestUtil.createI_ExternalSystem_ConfigBuilder()
				.type(ExternalSystemType.RabbitMQ.getCode())
				.build();

		ExternalSystemTestUtil.createRabbitMQConfigBuilder()
				.externalSystemConfigId(parentRecord.getExternalSystem_Config_ID())
				.build();

		// when
		final ImmutableList<ExternalSystemParentConfig> result = externalSystemConfigRepo.getActiveByType(ExternalSystemType.Alberta);

		// then
		assertThat(result).isEmpty();
	}
}
