package de.metas.handlingunits.attributes.sscc18.impl;

import org.adempiere.service.ClientId;
import org.adempiere.service.ISysConfigBL;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.adempiere.util.StringUtils;
import org.compiere.util.Util;

import de.metas.document.documentNo.IDocumentNoBuilderFactory;
import de.metas.handlingunits.attributes.sscc18.ISSCC18CodeBL;
import de.metas.handlingunits.attributes.sscc18.SSCC18;
import de.metas.organization.OrgId;
import lombok.NonNull;

public class SSCC18CodeBL implements ISSCC18CodeBL
{
	/**
	 * Manufacturer code consists of 7 or 8 digits. For the system default it is 0000000 (7 zeros)
	 */
	public static final String SYSCONFIG_ManufacturerCode = "de.metas.handlingunit.GS1ManufacturerCode";

	private final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);

	private final NextSerialNumberProvider nextSerialNumberProvider;
	/** for debugging */
	private boolean hasCustomNextSerialNumberProvider;

	/**
	 * The extended digit in SSCC18. Usually 0 (the package type - a carton)
	 */
	private final int EXTENDED_DIGIT = 0;

	public SSCC18CodeBL()
	{
		this.hasCustomNextSerialNumberProvider = false;

		this.nextSerialNumberProvider = orgId -> {
			final String sscc18SerialNumberStr = Services.get(IDocumentNoBuilderFactory.class)
					.forTableName(SSCC18_SERIALNUMBER_SEQUENCENAME, ClientId.METASFRESH.getRepoId(), orgId.getRepoId())
					.build();
			return Integer.parseInt(sscc18SerialNumberStr);
		};
	}

	private String getManufacturerCode(@NonNull final OrgId orgId)
	{
		final String manufacturerCode_SysConfig = sysConfigBL.getValue(SYSCONFIG_ManufacturerCode, null,
				ClientId.METASFRESH.getRepoId(),
				orgId.getRepoId());

		return manufacturerCode_SysConfig;
	}

	@Override
	public boolean isCheckDigitValid(final SSCC18 sscc18)
	{

		final int extentionDigit = sscc18.getExtensionDigit();
		final String manufacturerCode = sscc18.getManufacturerCode().trim();
		final String serialNumber = sscc18.getSerialNumber().trim();
		final int checkDigit = sscc18.getCheckDigit();

		final String stringSSCC18ToVerify = extentionDigit
				+ manufacturerCode
				+ serialNumber;

		final int result = computeCheckDigit(stringSSCC18ToVerify);

		if (checkDigit == result % 10)
		{
			return true;
		}

		return false;
	}

	@Override
	public int computeCheckDigit(final String stringSSCC18ToVerify)
	{
		Check.assume(stringSSCC18ToVerify.length() == 17, "Incorrect SSCC18");

		int sumOdd = 0;
		int sumEven = 0;

		for (int i = 0; i < stringSSCC18ToVerify.length(); i++)
		{
			// odd
			if (i % 2 != 0)
			{
				sumOdd += Integer.parseInt(Character.toString(stringSSCC18ToVerify.charAt(i)));
			}

			else
			{
				sumEven += Integer.parseInt(Character.toString(stringSSCC18ToVerify.charAt(i)));
			}
		}

		int result = 3 * sumOdd + sumEven;

		result /= 10;
		return result % 10;
	}

	@Override
	public SSCC18 generate(@NonNull final OrgId orgId)
	{
		return generate(orgId, nextSerialNumberProvider.provideNextSerialNumber(orgId));
	}

	@Override
	public SSCC18 generate(@NonNull final OrgId orgId, final int serialNumber)
	{
		Check.assume(serialNumber > 0, "serialNumber > 0");

		//
		// Retrieve and validate ManufacturerCode
		final String manufacturerCode_SysConfig = getManufacturerCode(orgId);
		Check.assume(StringUtils.isNumber(manufacturerCode_SysConfig), "Manufacturer code {} is not a number", manufacturerCode_SysConfig);
		final int manufacturerCodeSize = manufacturerCode_SysConfig.length();
		Check.assume(manufacturerCodeSize <= 8, "Manufacturer code too long: {}", manufacturerCode_SysConfig);

		//
		// Validate serialNumber and adjust serialNumber and manufacturerCode paddings
		final String serialNumberStr = String.valueOf(serialNumber);
		final int serialNumberSize = serialNumberStr.length();
		Check.assume(serialNumberSize <= 9, "Serial number too long: {}", serialNumberStr);
		final String finalManufacturerCode;
		final String finalSerialNumber;
		if (manufacturerCodeSize == 8)
		{
			Check.assume(serialNumberSize <= 8, "Serial number too long: {}", serialNumberStr);
			finalSerialNumber = Util.lpadZero(serialNumberStr, 8, "Manufacturer code size shoult be 8");

			finalManufacturerCode = manufacturerCode_SysConfig;
		}
		else if (manufacturerCodeSize == 7)
		{
			finalSerialNumber = Util.lpadZero(serialNumberStr, 9, "Manufacturer code size shoult be 9");

			finalManufacturerCode = manufacturerCode_SysConfig;
		}
		// manufacturer code smaller than 7
		else
		{
			finalSerialNumber = Util.lpadZero(serialNumberStr, 9, "Manufacturer code size shoult be " + 9);

			finalManufacturerCode = Util.lpadZero(manufacturerCode_SysConfig, 7, "Manufacturer code size shoult be " + 7);
		}

		final int checkDigit = computeCheckDigit(EXTENDED_DIGIT + finalManufacturerCode + finalSerialNumber);

		return new SSCC18(EXTENDED_DIGIT, finalManufacturerCode, finalSerialNumber, checkDigit);
	}

	@Override
	public void validate(final SSCC18 sscc18ToValidate)
	{
		final String manufactCode = sscc18ToValidate.getManufacturerCode().trim();
		final String serialNumber = sscc18ToValidate.getSerialNumber().trim();

		Check.assume(StringUtils.isNumber(manufactCode), "The manufacturer code " + manufactCode + " is not a number");
		Check.assume(manufactCode.length() <= 8, "The manufacturer code " + manufactCode + "is too long");

		Check.assume(StringUtils.isNumber(serialNumber), "The serial number " + serialNumber + " is not a number");
		Check.assume(serialNumber.length() <= 9, "The serial number " + serialNumber + "is too long");

		Check.assume((serialNumber + manufactCode).length() == 16, "Manufacturer code + serial number must be 16");
		Check.assume(isCheckDigitValid(sscc18ToValidate), "Check digit is not valid");
	}

	@Override
	public String toString(final SSCC18 sscc18, final boolean humanReadable)
	{
		Check.assumeNotNull(sscc18, "sscc18 not null");

		if (!humanReadable)
		{
			return sscc18.getExtensionDigit()
					+ sscc18.getManufacturerCode()
					+ sscc18.getSerialNumber()
					+ sscc18.getCheckDigit();
		}
		else
		{
			throw new IllegalStateException("Not implemented");
		}
	}

	@FunctionalInterface
	public interface NextSerialNumberProvider
	{
		int provideNextSerialNumber(OrgId orgId);
	}
}
