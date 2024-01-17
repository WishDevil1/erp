import React from 'react';
import PropTypes from 'prop-types';

import ButtonWithIndicator from '../../../components/buttons/ButtonWithIndicator';
import { isNullOrUndefined } from '../../../utils';

const ATTRIBUTE_CODES_TO_DISPLAY = ['HU_BestBeforeDate', 'Lot-Nummer'];
const ATTRIBUTE_CODES_TO_MAX_LENGTH = {
  HU_BestBeforeDate: 10,
};

const HUButton = ({ handlingUnitInfo, onClick }) => {
  const getProductCaption = () => {
    if (!handlingUnitInfo.products || !handlingUnitInfo.products.length) {
      return '';
    }

    return `${handlingUnitInfo.products[0].productValue} - ${handlingUnitInfo.products[0].productName}`;
  };
  const getAttributes = () => {
    if (
      !handlingUnitInfo.attributes2 ||
      !handlingUnitInfo.attributes2.list ||
      !handlingUnitInfo.attributes2.list.length
    ) {
      return '';
    }

    return handlingUnitInfo.attributes2.list.filter((attribute) => ATTRIBUTE_CODES_TO_DISPLAY.includes(attribute.code));
  };
  const getAttributeDisplayValue = (attribute) => {
    if (isNullOrUndefined(attribute.value)) {
      return '-';
    }
    const stringValue = String(attribute.value);
    const maxLength = ATTRIBUTE_CODES_TO_MAX_LENGTH[attribute.code] || stringValue.length;
    return stringValue.substring(0, maxLength);
  };

  return (
    <ButtonWithIndicator caption={getProductCaption()} onClick={onClick} size={'large'}>
      {getAttributes().map((attribute, index) => (
        <div key={index} className="hu-row-info">
          <div className="hu-row-label">
            <span>{attribute.caption}:</span>
          </div>
          <div className="hu-row-value">
            <span>{getAttributeDisplayValue(attribute)}</span>
          </div>
        </div>
      ))}
    </ButtonWithIndicator>
  );
};

HUButton.propTypes = {
  handlingUnitInfo: PropTypes.object.isRequired,
  onClick: PropTypes.func.isRequired,
};

export default HUButton;
