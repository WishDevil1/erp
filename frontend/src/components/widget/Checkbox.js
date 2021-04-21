import React, { useRef, useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

/**
 * @file Function based component.
 * @module Checkbox
 */
const Checkbox = (props) => {
  const rawWidget = useRef(null);
  const {
    widgetData,
    disabled,
    fullScreen,
    tabIndex,
    handlePatch,
    widgetField,
    id,
    filterWidget,
    isFilterActive,
  } = props;
  let { value, defaultValue } = props.widgetData[0];

  value =
    typeof value === 'string' || typeof value === 'undefined' ? false : value;
  const initialChecked = defaultValue && !isFilterActive ? defaultValue : value;
  const [checkedState, setCheckedState] = useState(initialChecked);

  useEffect(() => {
    defaultValue &&
      !isFilterActive &&
      handlePatch(widgetField, defaultValue, id);
  }, []);

  /**
   * @method handleClear
   * @summary clears the widget field by calling the method handlePatch from its parent with an empty value
   */
  const handleClear = () => {
    const { handlePatch, widgetField, id } = props;
    setCheckedState(false);
    handlePatch(widgetField, '', id);
  };

  /**
   * @method updateCheckedState
   * @summary toggles local checked state and in the same time it performs a patch to update
   *          the widgetField with the current checked value of the element
   * @param {object} e
   */
  const updateCheckedState = (e) => {
    setCheckedState(!checkedState);
    handlePatch(widgetField, e.target.checked, id);
  };

  return (
    <div>
      <label
        className={
          'input-checkbox ' +
          (widgetData[0].readonly || disabled ? 'input-disabled ' : '')
        }
        tabIndex={fullScreen ? -1 : tabIndex}
        onKeyDown={(e) => {
          if (e.key === ' ') {
            e.preventDefault();
            rawWidget.current && rawWidget.current.click();
          }
        }}
      >
        <input
          ref={rawWidget}
          type="checkbox"
          checked={checkedState}
          disabled={widgetData[0].readonly || disabled}
          onChange={updateCheckedState}
          tabIndex="-1"
        />
        <div
          className={classnames('input-checkbox-tick', {
            'input-state-false': widgetData[0].value === false && filterWidget,
            checked: widgetData[0].value,
          })}
        />
      </label>
      {filterWidget &&
      !disabled &&
      !widgetData[0].readonly &&
      (widgetData[0].value != null && widgetData[0].value !== '') ? (
        <small className="input-side" onClick={handleClear}>
          (clear)
        </small>
      ) : (
        ''
      )}
    </div>
  );
};

/**
 * @typedef {object} Props Component props
 * @prop {array} widgetData
 * @prop {bool} [disabled]
 * @prop {bool} [fullScreen]
 * @prop {number} [tabIndex]
 * @prop {bool} [filterWidget]
 * @prop {func} [handlePatch]
 * @prop {string} [widgetField]
 * @prop {string|number} [id]
 * @todo Check props. Which proptype? Required or optional?
 */
Checkbox.propTypes = {
  widgetData: PropTypes.array.isRequired,
  disabled: PropTypes.bool,
  fullScreen: PropTypes.bool,
  tabIndex: PropTypes.number,
  filterWidget: PropTypes.bool,
  handlePatch: PropTypes.func,
  widgetField: PropTypes.string,
  isFilterActive: PropTypes.bool,
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

export default Checkbox;
