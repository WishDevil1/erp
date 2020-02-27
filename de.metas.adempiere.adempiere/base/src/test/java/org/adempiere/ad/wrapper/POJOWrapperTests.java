package org.adempiere.ad.wrapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Properties;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
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

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.model.POWrapper;
import org.adempiere.model.PlainContextAware;
import org.adempiere.test.AdempiereTestHelper;
import org.compiere.model.I_C_BPartner;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.metas.user.UserId;
import de.metas.util.time.FixedTimeSource;
import de.metas.util.time.SystemTime;

public class POJOWrapperTests
{
	private PlainContextAware contextProvider;

	public interface ITable1
	{
		// @formatter:off
		String Table_Name = "Table1";
		String COLUMNNNAME_Table1_ID = "Table1_ID";
		void setTable1_ID(int value);
		int getTable1_ID();

		String COLUMNNNAME_Name = "Name";
		String getName();
		void setName(String name);

		String COLUMNNNAME_ValueInt = "ValueInt";
		int getValueInt();
		void setValueInt(int value);
		// @formatter:on
	}

	public interface ITable2
	{
		// @formatter:off
		String Table_Name = "Table2";
		String COLUMNNNAME_Table2_ID = "Table2_ID";
		int getTable2_ID();

		String COLUMNNNAME_Table1_ID = "Table1_ID";
		int getTable1_ID();
		void setTable1_ID(int Table1_ID);
		ITable1 getTable1();
		void setTable1(ITable1 table1);
		// @formatter:on
	}

	@BeforeEach
	public void init()
	{
		AdempiereTestHelper.get().init();
		POJOLookupMap.get().setCopyOnSave(true);

		// NOTE: By Default we work with strict values
		// Tests will un-set this setting if necessary
		POJOWrapper.setDefaultStrictValues(true);

		contextProvider = PlainContextAware.newOutOfTrx();
	}

	private void assertEquals(final String message, final int expected, final int actual)
	{
		assertThat(actual).as(message).isEqualTo(expected);
	}

	private void assertEquals(final String message, final Object expected, final ITable1 actual)
	{
		assertThat(actual).as(message).isEqualTo(expected);
	}

	private void assertSame(final String message, final ITable1 expected, final ITable1 actual)
	{
		assertThat(actual).as(message).isSameAs(expected);
	}

	@Test
	public void test_DefaultValues()
	{
		assertThat(POJOWrapper.DEFAULT_VALUE_int).isEqualTo(0);
		assertThat(POJOWrapper.DEFAULT_VALUE_BigDecimal).isEqualTo(BigDecimal.ZERO);
	}

	@Nested
	public class created_and_updated_handling
	{
		@Test
		public void presetID()
		{
			final UserId userId = UserId.ofRepoId(111);
			SystemTime.setTimeSource(new FixedTimeSource(LocalDate.of(2020, Month.FEBRUARY, 27).atStartOfDay()));

			final Properties ctx = contextProvider.getCtx();
			Env.setLoggedUserId(ctx, userId);

			final ITable1 record = POJOWrapper.create(ctx, ITable1.class);
			record.setTable1_ID(12345);
			POJOWrapper.save(record);

			final POJOWrapper wrapper = POJOWrapper.getWrapper(record);
			assertThat(wrapper.getValue(POJOWrapper.COLUMNNAME_CreatedBy, Object.class)).isEqualTo(userId.getRepoId());
			assertThat(wrapper.getValue(POJOWrapper.COLUMNNAME_Created, Object.class)).isEqualTo(TimeUtil.getDay(2020, 02, 27));
			assertThat(wrapper.getValue(POJOWrapper.COLUMNNAME_UpdatedBy, Object.class)).isEqualTo(userId.getRepoId());
			assertThat(wrapper.getValue(POJOWrapper.COLUMNNAME_Updated, Object.class)).isEqualTo(TimeUtil.getDay(2020, 02, 27));
		}
	}

	@Test
	public void create_return_instance_with_default_values()
	{
		POJOWrapper.setDefaultStrictValues(false);
		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);
		assertThat(table2.getTable2_ID()).isEqualTo(POJOWrapper.ID_ZERO);
		assertThat(table2.getTable1_ID()).isEqualTo(POJOWrapper.ID_ZERO);
	}

	@Test
	public void test_SetGet_IDColumn()
	{
		ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);
		assertThat(table2.getTable2_ID()).isEqualTo(POJOWrapper.ID_ZERO);

		table2.setTable1_ID(123);
		assertThat(table2.getTable1_ID()).as("Invalid Table1_ID").isEqualTo(123);

		POJOWrapper.save(table2);
		//
		// Reload
		table2 = POJOWrapper.create(contextProvider.getCtx(),
				table2.getTable2_ID(),
				ITable2.class,
				contextProvider.getTrxName());

		//
		// Revalidate
		assertThat(table2.getTable1_ID()).as("Invalid Table1_ID").isEqualTo(123);
	}

	@Test
	public void test_SetGetModel_Unsaved()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1);
		assertThat(table2.getTable1()).isSameAs(table1);
	}

	@Test
	public void test_SetGetModel_Unsaved_ResetID()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1);
		assertThat(table2.getTable1_ID()).as("Invalid Table1_ID").isEqualTo(POJOWrapper.ID_ZERO);
		assertThat(table2.getTable1()).as("Invalid Table1").isSameAs(table1);

		table2.setTable1_ID(POJOWrapper.ID_ZERO);
		assertThat(table2.getTable1_ID()).as("Invalid Table1_ID").isEqualTo(POJOWrapper.ID_ZERO);
		assertThat(table2.getTable1()).as("Invalid Table1").isNull();
	}

	@Test
	public void test_SetGetModel_Saved_ResetID()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.save(table1);

		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1);
		assertThat(table2.getTable1_ID()).isEqualTo(table1.getTable1_ID());
		assertThat(table2.getTable1()).isSameAs(table1);

		table2.setTable1_ID(-1);
		assertThat(table2.getTable1_ID()).isEqualTo(-1);
		assertThat(table2.getTable1()).isNull();
	}

	@Test
	public void test_SetGetModel_Unsaved_ResetModel()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1);
		assertEquals("Invalid Table1_ID", POJOWrapper.ID_ZERO, table2.getTable1_ID());
		assertSame("Invalid Table1", table1, table2.getTable1());

		table2.setTable1(null);
		assertEquals("Invalid Table1_ID", POJOWrapper.ID_ZERO, table2.getTable1_ID());
		assertEquals("Invalid Table1", null, table2.getTable1());
	}

	@Test
	public void test_SetGetModel_Saved_ResetModel()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.save(table1);

		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1);
		assertEquals("Invalid Table1_ID", table1.getTable1_ID(), table2.getTable1_ID());
		assertSame("Invalid Table1", table1, table2.getTable1());

		table2.setTable1(null);
		assertEquals("Invalid Table1_ID", POJOWrapper.ID_ZERO, table2.getTable1_ID());
		assertEquals("Invalid Table1", null, table2.getTable1());
	}

	@Test
	public void test_SetGetModel_Changed()
	{
		final ITable1 table1_1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.save(table1_1);
		final ITable1 table1_2 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.save(table1_2);

		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1_1);
		assertEquals("Invalid Table1_ID", table1_1.getTable1_ID(), table2.getTable1_ID());
		assertSame("Invalid Table1", table1_1, table2.getTable1());

		table2.setTable1_ID(-1);
		assertEquals("Invalid Table1_ID", -1, table2.getTable1_ID());
		assertThat(table2.getTable1()).isNull();
	}

	@Test
	public void test_SetGetModel_ChangedAfterSet()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		// NOTE: we are not saving it

		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);

		table2.setTable1(table1);
		assertEquals("Invalid Table1_ID", POJOWrapper.ID_ZERO, table2.getTable1_ID());
		assertSame("Invalid Table1", table1, table2.getTable1());

		final ITable1 table1_saved = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.save(table1_saved);

		table2.setTable1_ID(table1_saved.getTable1_ID());
		assertEquals("Invalid Table1_ID", table1_saved.getTable1_ID(), table2.getTable1_ID());
		assertEquals("Invalid Table1 value", table1_saved, table2.getTable1());
		assertThat(table2.getTable1()).isNotSameAs(table1_saved);
	}

	@Test
	public void test_SetGet_ValueInt()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);

		table1.setValueInt(123);
		assertEquals("Invalid ValueInt", 123, table1.getValueInt());
	}

	@Test
	public void test_SetGet_ValueInt_Default()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.disableStrictValues(table1);

		assertEquals("Invalid Default ValueInt", POJOWrapper.DEFAULT_VALUE_int, table1.getValueInt());
	}

	@Test
	public void setId_Again()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		POJOWrapper.save(table1);

		assertThat(table1.getTable1_ID()).isGreaterThan(0);

		// Shall throw an exception because we are not allowed to reset ID
		table1.setTable1_ID(11111);
	}

	@Test
	public void test_GetModel_CachedModelShallRefresh()
	{
		final ITable1 table1 = POJOWrapper.create(contextProvider.getCtx(), ITable1.class);
		table1.setValueInt(100);
		POJOWrapper.save(table1);

		final ITable2 table2 = POJOWrapper.create(contextProvider.getCtx(), ITable2.class);
		table2.setTable1(table1);
		POJOWrapper.save(table2);

		// We assume that Table1 has the right ValueInt
		assertEquals("Table1 shall be refreshed before returned",
				100, // expected
				table2.getTable1().getValueInt());

		final ITable1 table1_reloaded = POJOWrapper.create(Env.getCtx(), table1.getTable1_ID(), ITable1.class, ITrx.TRXNAME_None);
		assertEquals("Invalid Table1 ValueInt", 100, table1_reloaded.getValueInt());

		table1_reloaded.setValueInt(123);
		POJOWrapper.save(table1_reloaded);

		// We assume that Table1 is refreshed before retrieved even if it was cached
		assertEquals("Table1 shall be refreshed before returned",
				123, // expected
				table2.getTable1().getValueInt());
	}

	/**
	 * Making sure that if {@link POJOWrapper#refresh(Object, String)} is called, then the pojo's <code>trxName</code> is updated.
	 *
	 * We need this in order to make the {@link POJOWrapper} behave similar to the {@link POWrapper},
	 * when they are called by {@link InterfaceWrapperHelper#refresh(Object, String)}.
	 */
	@Test
	public void testRefreshSetsTrxName()
	{
		final I_C_BPartner bp = POJOWrapper.create(Env.getCtx(), I_C_BPartner.class);
		POJOWrapper.save(bp);
		assertThat(POJOWrapper.getTrxName(bp)).isNull(); // guard

		final boolean discardChanges = false;
		POJOWrapper.refresh(bp, discardChanges, "myTrxName");
		assertThat(POJOWrapper.getTrxName(bp)).isEqualTo("myTrxName");
	}

}
