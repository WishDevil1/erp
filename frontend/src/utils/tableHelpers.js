import React from 'react';
import currentDevice from 'current-device';
import PropTypes from 'prop-types';
import numeral from 'numeral';
import Moment from 'moment-timezone';
import {
  AMOUNT_FIELD_FORMATS_BY_PRECISION,
  AMOUNT_FIELD_TYPES,
  SPECIAL_FIELD_TYPES,
  DATE_FIELD_FORMATS,
  DATE_FIELD_TYPES,
  TIME_FIELD_TYPES,
  TIME_REGEX_TEST,
  TIME_FORMAT,
} from '../constants/Constants';

export const containerPropTypes = {
  // from <DocumentList>
  // TODO: This needs to be fixed in all child components
  // windowId: PropTypes.number,
  // docId: PropTypes.number,
  viewId: PropTypes.string,
  tabId: PropTypes.string,
  autofocus: PropTypes.bool,
  rowEdited: PropTypes.bool,
  onSelectionChanged: PropTypes.func,
  onRowEdited: PropTypes.func,
  defaultSelected: PropTypes.array,
  limitOnClickOutside: PropTypes.bool,
  supportOpenRecord: PropTypes.bool,

  // from redux
  rows: PropTypes.array.isRequired,
  columns: PropTypes.array.isRequired,
  selected: PropTypes.array.isRequired,
  collapsedParentRows: PropTypes.array.isRequired,
  collapsedRows: PropTypes.array.isRequired,
  collapsedArrayMap: PropTypes.array.isRequired,
  allowShortcut: PropTypes.bool,
  allowOutsideClick: PropTypes.bool,
  modalVisible: PropTypes.bool,
  isGerman: PropTypes.bool,
  activeSort: PropTypes.bool,

  // action creators
  collapseTableRow: PropTypes.func.isRequired,
  deselectTableItems: PropTypes.func.isRequired,
  openModal: PropTypes.func.isRequired,
  updateTableSelection: PropTypes.func.isRequired,
};

export const componentPropTypes = {
  ...containerPropTypes,
  onSelect: PropTypes.func.isRequired,
  onGetAllLeaves: PropTypes.func.isRequired,
  onSelectAll: PropTypes.func.isRequired,
  onDeselectAll: PropTypes.func.isRequired,
  onDeselect: PropTypes.func.isRequired,
};

export function constructorFn() {
  this.state = {
    listenOnKeys: true,
    contextMenu: {
      open: false,
      x: 0,
      y: 0,
      fieldName: null,
      supportZoomInto: false,
      supportFieldEdit: false,
    },
    promptOpen: false,
    isBatchEntry: false,
    tableRefreshToggle: false,
  };
}

/**
 * @method getAmountFormatByPrecisiont
 * @param {string} precision
 **/
export function getAmountFormatByPrecision(precision) {
  return precision &&
    precision >= 0 &&
    precision < AMOUNT_FIELD_FORMATS_BY_PRECISION.length
    ? AMOUNT_FIELD_FORMATS_BY_PRECISION[precision]
    : null;
}

/**
 * @method getDateFormat
 * @param {string} fieldType
 * @summary get the date format according to the given fieldType provided parameter
 *   <FieldType> =====> <stringFormat> correspondence:
 *   Date: 'L',
 *   ZonedDateTime: 'L LTS',
 *   DateTime: 'L LTS',
 *   Time: 'LT',
 *   Timestamp: 'L HH:mm:ss',
 */
export function getDateFormat(fieldType) {
  return DATE_FIELD_FORMATS[fieldType];
}

/**
 * @method getSizeClass
 * @param {object} col
 * @summary get the class size dinamically (for Bootstrap ) for the col obj given as param
 */
export function getSizeClass(col) {
  const { widgetType, size } = col;
  const lg = ['List', 'Lookup', 'LongText', 'Date', 'DateTime', 'Time'];
  const md = ['Text', 'Address', 'ProductAttributes'];

  if (size) {
    switch (size) {
      case 'S':
        return 'td-sm';
      case 'M':
        return 'td-md';
      case 'L':
        return 'td-lg';
      case 'XL':
        return 'td-xl';
      case 'XXL':
        return 'td-xxl';
    }
  } else {
    if (lg.indexOf(widgetType) > -1) {
      return 'td-lg';
    } else if (md.indexOf(widgetType) > -1) {
      return 'td-md';
    } else {
      return 'td-sm';
    }
  }
}

/**
 * @method createDate
 * @param {object} containing the fieldValue, fieldType and the active locale
 * @summary creates a Date using Moment lib formatting it with the locale passed as param
 */
// TODO !!!! use the utils/locale.js after it is present in the DEV branch !!!!
// TODO: (activeLocale={key, caption}, just sending the language name would be enough)
export function createDate({ fieldValue, fieldType, activeLocale }) {
  const languageKey = activeLocale ? activeLocale.key : null;
  if (fieldValue) {
    return !Moment.isMoment(fieldValue) && fieldValue.match(TIME_REGEX_TEST)
      ? Moment.utc(Moment.duration(fieldValue).asMilliseconds())
          .locale(languageKey)
          .format(TIME_FORMAT)
      : Moment(fieldValue)
          .locale(languageKey)
          .format(getDateFormat(fieldType));
  }

  return '';
}

/**
 * @method createAmount
 * @param {string} fieldValue
 * @param {string} precision
 * @param {boolean} isGerman
 * @summary creates an amount for a given value with the desired precision and it provides special formatting
 *          for the case when german language is set
 */
export function createAmount(fieldValue, precision, isGerman) {
  if (fieldValue) {
    const fieldValueAsNum = numeral(parseFloat(fieldValue));
    const numberFormat = getAmountFormatByPrecision(precision);
    const returnValue = numberFormat
      ? fieldValueAsNum.format(numberFormat)
      : fieldValueAsNum.format();

    // For German natives we want to show numbers with comma as a value separator
    // https://github.com/metasfresh/me03/issues/1822
    if (isGerman && parseFloat(returnValue) != null) {
      const commaRegexp = /,/g;
      commaRegexp.test(returnValue);
      const lastIdx = commaRegexp.lastIndex;

      if (lastIdx) {
        return returnValue;
      }

      return `${returnValue}`.replace('.', ',');
    }

    return returnValue;
  }

  return '';
}

/**
 * @method createSpecialField
 * @param {string}  fieldType
 * @param {string}  fieldValue
 * @summary For the special case of fieldType being of type 'Color' it will show a circle in the TableCell
 *          with the hex value given - fieldValue
 *          More details on : https://github.com/metasfresh/metasfresh-webui-frontend-legacy/issues/1603
 */
export function createSpecialField(fieldType, fieldValue) {
  switch (fieldType) {
    case 'Color': {
      const style = {
        backgroundColor: fieldValue,
      };
      return <span className="widget-color-display" style={style} />;
    }
    default:
      return fieldValue;
  }
}

/**
 * @method fieldValueToString
 * @param {string} fieldValue
 * @param {string} fieldType
 * @param {string} precision
 * @param {boolean} isGerman
 * @param {string} activeLocale
 * @summary This is a patch function to mangle the desired output used at table level within TableCell, Filters components
 */
export function fieldValueToString({
  fieldValue,
  fieldType = 'Text',
  precision = null,
  isGerman,
  activeLocale,
}) {
  if (fieldValue === null) {
    return '';
  }

  switch (typeof fieldValue) {
    /**
     * Case when fieldValue is passed as an array - this is used to show date intervals within filters
     * as dd.mm.yyyy - dd.mm.yyyy for example
     */
    case 'object': {
      if (Array.isArray(fieldValue)) {
        return fieldValue
          .map((value) => fieldValueToString(value, fieldType))
          .join(' - ');
      }

      return DATE_FIELD_TYPES.includes(fieldType) ||
        TIME_FIELD_TYPES.includes(fieldType)
        ? createDate({ fieldValue, fieldType, activeLocale })
        : fieldValue.caption;
    }
    case 'boolean': {
      return fieldValue ? (
        <i className="meta-icon-checkbox-1" />
      ) : (
        <i className="meta-icon-checkbox" />
      );
    }
    case 'string': {
      if (
        DATE_FIELD_TYPES.includes(fieldType) ||
        TIME_FIELD_TYPES.includes(fieldType)
      ) {
        return createDate({ fieldValue, fieldType, activeLocale });
      } else if (AMOUNT_FIELD_TYPES.includes(fieldType)) {
        return createAmount(fieldValue, precision, isGerman);
      } else if (SPECIAL_FIELD_TYPES.includes(fieldType)) {
        return createSpecialField(fieldType, fieldValue);
      }
      return fieldValue;
    }
    default: {
      return fieldValue;
    }
  }
}

export function handleCopy(e) {
  e.preventDefault();

  const cell = e.target;
  const textValue = cell.value || cell.textContent;

  e.clipboardData.setData('text/plain', textValue);
}

export function handleOpenNewTab({ windowId, rowIds }) {
  if (!rowIds) {
    return;
  }

  rowIds.forEach((rowId) => {
    window.open(`/window/${windowId}/${rowId}`, '_blank');
  });
}

export function shouldRenderColumn(column) {
  if (
    !column.restrictToMediaTypes ||
    column.restrictToMediaTypes.length === 0
  ) {
    return true;
  }

  const deviceType = currentDevice.type;
  let mediaType = 'tablet';

  if (deviceType === 'mobile') {
    mediaType = 'phone';
  } else if (deviceType === 'desktop') {
    mediaType = 'screen';
  }

  return column.restrictToMediaTypes.indexOf(mediaType) !== -1;
}
