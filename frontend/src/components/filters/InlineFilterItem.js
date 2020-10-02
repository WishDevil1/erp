/**
 * Filter element displayed inline for frequent filters
 * To see how this should behave look at https://github.com/metasfresh/metasfresh-webui-frontend-legacy/issues/1387
 * It seems this is not in use any more (checked that at the time of refactoring the filters Sep, 2020)
 **/
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { allowShortcut, disableShortcut } from '../../actions/WindowActions';
import RawWidget from '../widget/RawWidget';
import { convertDateToReadable } from '../../utils/dateHelpers';
import { updateInlineFilter } from '../../actions/FiltersActions';

class InlineFilterItem extends Component {
  state = { filter: this.props.parentFilter, searchString: '' };

  static getDerivedStateFromProps(props) {
    const { active } = props;
    if (active.length && active[0].parameters) {
      return { searchString: active[0].parameters[0].value };
    }
    return null;
  }

  setValue = (property, value, id, valueTo) => {
    const { filterId, updateInlineFilter } = this.props;
    updateInlineFilter({ filterId, value });
    this.setState({ searchString: value });
    //TODO: LOOKUPS GENERATE DIFFERENT TYPE OF PROPERTY parameters
    // IT HAS TO BE UNIFIED
    //
    // OVERWORKED WORKAROUND
    if (Array.isArray(property)) {
      property.map((item) => {
        this.mergeData(item.parameterName, value, valueTo);
      });
    } else {
      this.mergeData(property, value, valueTo);
    }
  };

  mergeData = (property, value, valueTo) => {
    this.setState((prevState) => ({
      filter: Object.assign({}, prevState.filter, {
        parameters: prevState.filter.parameters.map((param) => {
          if (param.parameterName === property) {
            return Object.assign({}, param, {
              value: convertDateToReadable(param.widgetType, value),
              valueTo: valueTo
                ? convertDateToReadable(param.widgetType, valueTo)
                : null, // added safety check as deepUnfreeze crashes when valueTo is undefined
            });
          } else {
            return param;
          }
        }),
      }),
    }));
  };

  handleApply = () => {
    const { applyFilters, clearFilters } = this.props;
    const { filter } = this.state;
    clearFilters(filter);
    if (filter && !filter.parameters[0].value) {
      return this.handleClear();
    }
    applyFilters(filter);
  };

  handleClear = () => {
    const { clearFilters } = this.props;
    const { filter } = this.state;

    clearFilters(filter);
  };

  render() {
    const {
      data,
      id,
      windowType,
      onShow,
      onHide,
      viewId,
      modalVisible,
      timeZone,
      allowShortcut,
      disableShortcut,
    } = this.props;
    const { filter, searchString } = this.state;

    const dataClone = { ...data };
    dataClone.value = searchString;

    return (
      <RawWidget
        entity="documentView"
        subentity="filter"
        subentityId={filter.filterId}
        handlePatch={this.handleApply}
        handleChange={this.setValue}
        widgetType={data.widgetType}
        fields={[{ ...data, emptyText: data.caption }]}
        type={data.type}
        widgetData={[dataClone]}
        range={data.range}
        caption={data.caption}
        noLabel={true}
        filterWidget={true}
        {...{
          id,
          viewId,
          windowType,
          onShow,
          onHide,
          modalVisible,
          timeZone,
          allowShortcut,
          disableShortcut,
        }}
      />
    );
  }
}

const mapStateToProps = (state) => {
  const { appHandler, windowHandler } = state;

  return {
    modalVisible: windowHandler.modal.visible,
    timeZone: appHandler.me.timeZone,
  };
};

InlineFilterItem.propTypes = {
  active: PropTypes.array,
  data: PropTypes.object,
  parentFilter: PropTypes.object,
  onShow: PropTypes.func,
  onHide: PropTypes.func,
  viewId: PropTypes.string,
  id: PropTypes.number,
  applyFilters: PropTypes.func,
  clearFilters: PropTypes.func,
  windowType: PropTypes.string,
  allowShortcut: PropTypes.func.isRequired,
  disableShortcut: PropTypes.func.isRequired,
  modalVisible: PropTypes.bool.isRequired,
  timeZone: PropTypes.string.isRequired,
  filterId: PropTypes.string,
  updateInlineFilter: PropTypes.func,
};

export default connect(
  mapStateToProps,
  {
    allowShortcut,
    disableShortcut,
    updateInlineFilter,
  }
)(InlineFilterItem);
