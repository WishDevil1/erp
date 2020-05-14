import produce from 'immer';
import { get } from 'lodash';
import { createSelector } from 'reselect';

import * as types from '../constants/ActionTypes';

export const tableState = {
  windowType: null,
  viewId: null,
  docId: null,
  tabId: null,
  selected: [],
  rows: [],
  // row columns
  columns: [],
  activeSort: false,
  headerProperties: {},

  //header columns
  headerElements: {},
  emptyText: null,
  emptyHint: null,
  page: 0,
  firstRow: 0,
  size: 0,
  orderBy: [],
  defaultOrderBys: [],
  pageLength: 0,
  queryLimit: 0,
  queryLimitHit: false,
  dataPending: false,
  dataError: false,
  tabIndex: 0,
  internalName: null,
  queryOnActivate: true,
  supportQuickInput: true,

  // includedTabsInfo
  allowCreateNew: true,
  allowCreateNewReason: null,
  allowDelete: true,
  stale: false,
};

// we store the length of the tables structure for the sake of testing and debugging
export const initialState = { length: 0 };

/**
 * @method getTableId
 * @summary Small helper function to generate the table id depending on the values (if viewId is
 * provided, we'll use only that for grids, and if not - it's a tab table so document id
 * and tab ids are expected ).
 */
export const getTableId = ({ windowType, viewId, docId, tabId }) => {
  return `${windowType}_${viewId ? `${viewId}` : `${docId}_${tabId}`}`;
};

/**
 * @method selectTableHelper
 * @summary selector function for `getTable`
 */
const selectTableHelper = (state, id) => {
  return get(state, ['tables', id], tableState);
};

/**
 * @method getTable
 * @summary Selector for getting table object by id from the state
 */
export const getTable = createSelector(
  [selectTableHelper],
  (table) => table
);

const reducer = produce((draftState, action) => {
  switch (action.type) {
    // CRUD
    case types.CREATE_TABLE: {
      const { id, data } = action.payload;
      const newLength = draftState.length + 1;

      draftState[id] = {
        ...tableState,
        ...data,
      };
      draftState.length = newLength;

      return;
    }
    case types.UPDATE_TABLE: {
      const { id, data } = action.payload;

      draftState[id] = {
        ...draftState[id],
        ...data,
      };

      return;
    }
    case types.DELETE_TABLE: {
      const { id } = action.payload;
      const newLength = draftState.length - 1;

      draftState.length = newLength;
      delete draftState[id];

      return;
    }

    case types.SET_ACTIVE_SORT_NEW: {
      const { id, active } = action.payload;

      draftState[id].activeSort = active;
    }
  }
}, initialState);

export default reducer;
