import React from 'react';
import PropTypes from 'prop-types';
import { trl } from '../../../utils/translations';
import { toQRCodeDisplayable } from '../../../utils/huQRCodes';

export const HUInfoComponent = ({ handlingUnitInfo }) => {
  // console.log('HUInfoComponent', { handlingUnitInfo });
  return (
    <table className="table view-header is-size-6">
      <tbody>
        <tr>
          <th>{trl('huManager.HU')}</th>
          <td>{handlingUnitInfo.displayName}</td>
        </tr>
        <tr>
          <th>{trl('huManager.qrCode')}</th>
          <td>{toQRCodeDisplayable(handlingUnitInfo.qrCode)}</td>
        </tr>
        <tr>
          <th>{trl('huManager.locator')}</th>
          <td>{handlingUnitInfo.locatorValue}</td>
        </tr>
        <tr>
          <th>{trl('huManager.HUStatus')}</th>
          <td>{computeHUStatusCaption(handlingUnitInfo)}</td>
        </tr>
        {handlingUnitInfo.products.map((product) => (
          <ProductInfoRows key={product.productValue} product={product} />
        ))}
        {handlingUnitInfo.attributes2 &&
          handlingUnitInfo.attributes2.list &&
          handlingUnitInfo.attributes2.list.map((attribute) => (
            <AttributeRow key={attribute.code} caption={attribute.caption} value={attribute.value} />
          ))}
      </tbody>
    </table>
  );
};

HUInfoComponent.propTypes = {
  handlingUnitInfo: PropTypes.object.isRequired,
};

const computeHUStatusCaption = (handlingUnitInfo) => {
  let result = handlingUnitInfo.huStatusCaption;
  if (handlingUnitInfo.isDisposalPending) {
    result += ' / ' + trl('huManager.disposePendingStatus');
  }
  return result;
};

const ProductInfoRows = ({ product }) => {
  return (
    <>
      <tr>
        <th>{trl('huManager.product')}</th>
        <td>
          {product.productName} ({product.productValue})
        </td>
      </tr>
      <tr>
        <th>{trl('huManager.qty')}</th>
        <td>
          {product.qty} {product.uom}
        </td>
      </tr>
    </>
  );
};

ProductInfoRows.propTypes = {
  product: PropTypes.shape({
    productValue: PropTypes.string.isRequired,
    productName: PropTypes.string.isRequired,
    qty: PropTypes.string.isRequired, // it's string instead of number because it comes preformatted from the backend
    uom: PropTypes.string.isRequired,
  }).isRequired,
};

const AttributeRow = ({ caption, value }) => {
  // hide rows with empty values
  if (value == null) {
    return null;
  }

  return (
    <tr>
      <th>{caption}</th>
      <td>{value}</td>
    </tr>
  );
};

AttributeRow.propTypes = {
  caption: PropTypes.string.isRequired,
  value: PropTypes.any,
};
