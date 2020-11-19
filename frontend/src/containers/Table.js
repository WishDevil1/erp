import update from 'immutability-helper';
import React, { PureComponent } from 'react';
import { connect } from 'react-redux';

import { getZoomIntoWindow, deleteRequest } from '../api';
import { getTableId, getTable } from '../reducers/tables';

import {
  updateTableSelection,
  deselectTableRows,
  collapseTableRow,
  setActiveSort,
} from '../actions/TableActions';
import { showIncludedView } from '../actions/ViewActions';
import { openModal, updatePropertyValue } from '../actions/WindowActions';

import { containerPropTypes } from '../utils/tableHelpers';
import { mapIncluded } from '../utils/documentListHelper';

import Table from '../components/table/TableWrapper';

class TableContainer extends PureComponent {
  /**
   * @method getAllLeaves
   * @summary select parent and all it's leaves
   */
  getAllLeaves = () => {
    const { rows, selected } = this.props;
    let leafs = [];
    let leafsIds = [];

    rows.map((item) => {
      if (item.id === selected[0]) {
        leafs = mapIncluded(item);
      }
    });

    leafs.map((item) => {
      leafsIds = leafsIds.concat(item.id);
    });

    this.handleSelect(leafsIds);
  };

  handleSelect = (ids, cb) => {
    const {
      updateTableSelection,
      windowId,
      viewId,
      docId,
      tabId,
      keyProperty,
    } = this.props;
    let newSelected = [];

    if (ids) {
      if (!ids.splice) {
        newSelected = [ids];
      } else {
        newSelected = ids;
      }
    }

    updateTableSelection({
      id: getTableId({ windowId, viewId, docId, tabId }),
      selection: newSelected,
      keyProperty,
      windowId,
      viewId,
    }).then(() => {
      cb && cb();
    });

    return newSelected;
  };

  handleSelectAll = () => {
    const { keyProperty, rows } = this.props;
    const property = keyProperty ? keyProperty : 'rowId';
    const toSelect = rows.map((item) => item[property]);

    this.handleSelect(toSelect);
  };

  handleDeselect = (id) => {
    const { deselectTableRows, windowId, viewId, selected } = this.props;
    const tableId = getTableId({ windowId, viewId });
    const index = selected.indexOf(id);

    // TODO: Do we need this returned value ? Maybe we can handle
    // this in redux only?
    const newSelected = update(selected, { $splice: [[index, 1]] });

    if (!newSelected.length) {
      deselectTableRows({
        id: tableId,
        selection: [id],
        windowId,
        viewId,
      });
    }

    return newSelected;
  };

  handleDeselectAll = (callback) => {
    const { deselectTableRows, windowId, viewId, docId, tabId } = this.props;

    callback && callback();

    deselectTableRows({
      id: getTableId({ windowId, viewId, docId, tabId }),
      selection: [],
      windowId,
      viewId,
    });
  };

  // TODO: This re-fetches quick actions on editing row. Can be cemoved once
  // we'll properly handle quickactions in the redux store
  handleItemChange = () => {
    const { onRowEdited } = this.props;

    onRowEdited && onRowEdited(true);
  };

  /**
   * @method openTableModal
   * @summary Open `Add new` modal
   */
  openTableModal = () => {
    const { openModal, windowId, tabId } = this.props;

    openModal('Add new', windowId, 'window', tabId, 'NEW');
  };

  /**
   * @method handleAdvancedEdit
   * @summary Handles advanced edit - i.e case when ALT+E key combinations are being used
   *          Active only on subtables
   */
  handleAdvancedEdit = () => {
    const { openModal, windowId, tabId, docId, selected } = this.props;

    if (docId) {
      openModal('Advanced edit', windowId, 'window', tabId, selected[0], true);
    }
  };

  /**
   * @method handlePromptSubmit
   * @summary delete selected items
   */
  handlePromptSubmit = (selected) => {
    const { windowId, docId, updateDocList, tabId } = this.props;

    this.handleSelect();

    // TODO: This should be an action creator
    deleteRequest(
      'window',
      windowId,
      docId ? docId : null,
      docId ? tabId : null,
      selected
    )
      .then((response) => {
        // TODO: In the future we probably shouldn't refresh the whole list
        if (response.data[0] && response.data[0].includedTabsInfo) {
          Object.keys(response.data[0].includedTabsInfo).includes(tabId) &&
            updateDocList();
        }

        // when something is removed.
        if (!docId) {
          updateDocList();
        }
      })
      // for instance removing a newly added tab row without filling any fields
      .catch(() => {
        if (docId) {
          updateDocList();
        }
      });
  };

  /**
   * @method handleZoomInto
   * @summary open new window with details view of the selected row
   */
  handleZoomInto = (fieldName) => {
    const { entity, windowId, docId, tabId, viewId, selected } = this.props;

    getZoomIntoWindow(
      entity,
      windowId,
      docId,
      entity === 'window' ? tabId : viewId,
      selected[0],
      fieldName
    ).then((res) => {
      res &&
        res.data &&
        window.open(
          `/window/${res.data.documentPath.windowId}/${
            res.data.documentPath.documentId
          }`,
          '_blank'
        );
    });
  };

  /**
   * @method handleRowCollapse
   * @summary toggle table rows
   */
  handleRowCollapse = (node, collapse) => {
    const { collapseTableRow, windowId, viewId, tabId, docId } = this.props;
    const tableId = getTableId({ windowId, viewId, docId, tabId });

    collapseTableRow({ tableId, collapse, node });
  };

  render() {
    return (
      <Table
        {...this.props}
        onHandleZoomInto={this.handleZoomInto}
        onPromptSubmit={this.handlePromptSubmit}
        onItemChange={this.handleItemChange}
        onSelect={this.handleSelect}
        onSelectAll={this.handleSelectAll}
        onDeselectAll={this.handleDeselectAll}
        onDeselect={this.handleDeselect}
        onRowCollapse={this.handleRowCollapse}
        onGetAllLeaves={this.getAllLeaves}
        onHandleAdvancedEdit={this.handleAdvancedEdit}
        onOpenTableModal={this.openTableModal}
      />
    );
  }
}

TableContainer.propTypes = containerPropTypes;

const mapStateToProps = (state, props) => {
  const { windowId, docId, tabId, viewId } = props;
  const tableId = getTableId({ windowId, viewId, docId, tabId });
  const table = getTable(state, tableId);
  const modalVisible = state.windowHandler.modal.visible;
  let handleShortcuts = state.windowHandler.allowShortcut;

  // we don't have to worry about shortcuts if table is behind a modal
  if (modalVisible && !props.isModal) {
    handleShortcuts = false;
  }

  return {
    tableId,
    rows: table.rows,
    columns: table.columns,
    selected: table.selected,
    collapsedParentRows: table.collapsedParentRows,
    collapsedRows: table.collapsedRows,
    collapsedArrayMap: table.collapsedArrayMap,
    activeSort: table.activeSort,
    emptyText: table.emptyText,
    emptyHint: table.emptyHint,
    indentSupported: table.indentSupported,
    collapsible: table.collapsible,
    keyProperty: table.keyProperty,
    size: table.size,
    allowShortcut: handleShortcuts,
    allowOutsideClick: state.windowHandler.allowOutsideClick,
    modalVisible,
    isGerman:
      state.appHandler.me.language && state.appHandler.me.language.key
        ? state.appHandler.me.language.key.includes('de')
        : false,
  };
};

export { TableContainer };
export default connect(
  mapStateToProps,
  {
    collapseTableRow,
    deselectTableRows,
    openModal,
    updateTableSelection,
    updatePropertyValue,
    showIncludedView,
    setActiveSort,
  },
  false,
  { forwardRef: true }
)(TableContainer);
