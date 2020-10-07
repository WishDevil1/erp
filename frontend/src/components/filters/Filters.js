import counterpart from 'counterpart';
import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import { connect } from 'react-redux';
import {
  updateFilterWidgetShown,
  setNewFiltersActive,
  updateActiveFilter,
  clearAllFilters,
  annotateFilters,
  isFilterValid,
  updateNotValidFields,
  parseToPatch,
  getCachedFilter,
} from '../../actions/FiltersActions';

import FiltersNotIcluded from './FiltersNotIncluded';
import FiltersIncluded from './FiltersIncluded';
import deepUnfreeze from 'deep-unfreeze';
import { getEntityRelatedId } from '../../reducers/filters';

/**
 * @file Class based component.
 * @module Filters
 * @extends PureComponent
 */
class Filters extends PureComponent {
  /**
   * @method applyFilters
   * @summary This method should update filters in the store and DL reacts automatically to the changes
   * @param {object} filter
   * @param {function} cb - executed if filter is valid and after it was applied
   */
  applyFilters = ({ ...filter }, cb) => {
    const valid = isFilterValid(filter);
    const { updateNotValidFields, filterId } = this.props;

    updateNotValidFields({ filterId, data: !valid });
    if (valid) {
      const parsedFilter = filter.parameters
        ? {
            ...filter,
            parameters: parseToPatch(filter.parameters),
          }
        : filter;

      this.setFilterActive(parsedFilter);

      cb && cb();
    }
  };

  /**
   * @method setFilterActive
   * @summary This function updates the active filters we set and then triggers the pre-existing
   *          logic from DocList that will fetch the filtered data
   * @param {object} filterToAdd
   */
  setFilterActive = (filterToAdd) => {
    const { updateDocList, filterId, updateActiveFilter } = this.props;
    const { filtersActive: storeActiveFilters } = this.props.filters;

    // updating the active filters from the redux store with the filter passed as param
    const newFiltersActive = setNewFiltersActive({
      storeActiveFilters,
      filterToAdd,
    });

    updateActiveFilter({ filterId, data: newFiltersActive }); // update in the store the filters
    updateDocList(newFiltersActive); // move on and update the page with the new filters via DocList
  };

  /**
   * @method handleShow
   * @summary Method to lock backdrop, to do not close on click onClickOutside
   *          widgets that are bigger than filter wrapper
   * @param {*} value
   */
  handleShow = (value) => {
    const { filterId, updateFilterWidgetShown } = this.props;
    updateFilterWidgetShown({ filterId, data: value });
  };

  /**
   * @method clearFilters
   * @summary Clears all the filters for a specified filter group
   * @param {object} filterToClear - object containing the filters
   */
  clearFilters = (filterToClear) => {
    const { filterId, clearAllFilters, filters, updateDocList } = this.props;
    clearAllFilters({ filterId, data: filterToClear });
    // fetch again the doc content after filters were updated into the store
    updateDocList(filters.filtersActive);
  };

  /**
   * @method dropdownToggled
   * @summary Resets notValidFields flag to false
   */
  dropdownToggled = () => {
    const { updateNotValidFields, filterId } = this.props;
    updateNotValidFields({ filterId, data: false });
  };

  /**
   * @method render
   * @summary Main render function - renders the filters
   */
  render() {
    const {
      windowType,
      viewId,
      resetInitialValues,
      allowOutsideClick,
      modalVisible,
      filters,
      filterId,
      allFilters,
      flatActiveFilterIds,
    } = this.props;

    const widgetShown = filters ? filters.widgetShown : false;
    const notValidFields = filters ? filters.notValidFields : false;

    if (!filters || !viewId || !filters.filterData || !allFilters.length)
      return false;
    const { filtersActive, filtersCaptions: activeFiltersCaptions } = filters;

    return (
      <div
        className="filter-wrapper js-not-unselect"
        ref={(c) => (this.filtersWrapper = c)}
      >
        <span className="filter-caption">
          {`${counterpart.translate('window.filters.caption')}: `}
        </span>
        <div className="filter-wrapper">
          {allFilters.map((item) => {
            // iterate among the existing filters
            if (item.includedFilters) {
              let dropdownFilters = item.includedFilters;
              dropdownFilters = deepUnfreeze(dropdownFilters);
              dropdownFilters.map((dropdownFilterItem) => {
                dropdownFilterItem.isActive = flatActiveFilterIds.includes(
                  dropdownFilterItem.filterId
                );
                return dropdownFilterItem;
              });

              // we render the FiltersNotFrequent for normal filters
              // and for those entries that do have includedFilters (we have subfilters)
              // we are rendering FiltersFrequent layout. Note: this is adaptation over previous
              // funtionality that used the 'frequent' flag (was ditched in favour of this one).
              return (
                <FiltersIncluded
                  key={item.caption}
                  {...{
                    activeFiltersCaptions,
                    windowType,
                    notValidFields,
                    viewId,
                    widgetShown,
                    resetInitialValues,
                    allowOutsideClick,
                    modalVisible,
                  }}
                  data={dropdownFilters}
                  handleShow={this.handleShow}
                  applyFilters={this.applyFilters}
                  clearFilters={this.clearFilters}
                  active={filtersActive}
                  dropdownToggled={this.dropdownToggled}
                  filtersWrapper={this.filtersWrapper}
                />
              );
            }
            if (!item.includedFilters) {
              return (
                <FiltersNotIcluded
                  {...{
                    activeFiltersCaptions,
                    windowType,
                    notValidFields,
                    viewId,
                    widgetShown,
                    allowOutsideClick,
                    modalVisible,
                  }}
                  data={[item]}
                  handleShow={this.handleShow}
                  applyFilters={this.applyFilters}
                  clearFilters={this.clearFilters}
                  active={filtersActive}
                  dropdownToggled={this.dropdownToggled}
                  filtersWrapper={this.filtersWrapper}
                  key={item.filterId}
                  filterId={filterId}
                />
              );
            }
          })}
        </div>
      </div>
    );
  }
}

Filters.propTypes = {
  windowType: PropTypes.string.isRequired,
  resetInitialValues: PropTypes.func.isRequired,
  viewId: PropTypes.string,
  updateDocList: PropTypes.any,
  filtersActive: PropTypes.any,
  filterData: PropTypes.any,
  initialValuesNulled: PropTypes.any,
  allowOutsideClick: PropTypes.bool,
  modalVisible: PropTypes.bool,
  filterId: PropTypes.string,
  updateFilterWidgetShown: PropTypes.func,
  updateActiveFilter: PropTypes.func,
  filters: PropTypes.object,
  clearAllFilters: PropTypes.func,
  updateNotValidFields: PropTypes.func,
  allFilters: PropTypes.array,
  flatActiveFilterIds: PropTypes.array,
};

const mapStateToProps = (state, ownProps) => {
  const { allowOutsideClick, modal } = state.windowHandler;
  const { viewId, windowType: windowId } = ownProps;
  const filterId = getEntityRelatedId({ windowId, viewId });

  const stateFilter =
    windowId && viewId ? getCachedFilter(state, filterId) : null;

  const allFilters =
    stateFilter && stateFilter.filterData
      ? annotateFilters({
          unannotatedFilters: stateFilter.filterData,
          filtersActive: stateFilter.filtersActive,
        })
      : [];

  const flatActiveFilterIds =
    stateFilter && stateFilter.filtersActive
      ? stateFilter.filtersActive.map((item) => item.filterId)
      : [];

  return {
    allowOutsideClick,
    modalVisible: modal.visible,
    filters: stateFilter,
    filterId,
    allFilters,
    flatActiveFilterIds,
  };
};

export default connect(
  mapStateToProps,
  {
    updateFilterWidgetShown,
    updateActiveFilter,
    clearAllFilters,
    updateNotValidFields,
  }
)(Filters);
