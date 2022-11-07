import React from 'react';
import { useHistory } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';

import { toastError } from '../../../../../utils/toast';
import { updateManufacturingIssue } from '../../../../../actions/ManufacturingActions';

import ScanHUAndGetQtyComponent from '../../../../../components/ScanHUAndGetQtyComponent';
import { toQRCodeString } from '../../../../../utils/huQRCodes';
import { computeStepScanPropsFromActivity } from './computeStepScanPropsFromState';
import { computeStepScanUserInfoQtys } from './computeStepScanUserInfoQtys';
import PropTypes from 'prop-types';
import {
  getActivityById,
  getStepByIdFromActivity,
  getStepByQRCodeFromActivity,
} from '../../../../../reducers/wfProcesses';
import { trl } from '../../../../../utils/translations';

const RawMaterialIssueStepScanComponent = ({ wfProcessId, activityId, lineId, stepId }) => {
  console.log('RawMaterialIssueStepScanComponent', { wfProcessId, activityId, lineId, stepId });

  const activity = useSelector((state) => getActivityById(state, wfProcessId, activityId));

  const eligibleBarcode =
    stepId != null ? toQRCodeString(getStepByIdFromActivity(activity, lineId, stepId).huQRCode) : null;

  const resolveScannedBarcode = (scannedBarcode) => {
    const step = getStepByQRCodeFromActivity(activity, lineId, scannedBarcode);
    if (!step) {
      throw trl('activities.picking.notEligibleHUBarcode');
    }

    const {
      uom,
      qtyToIssueTarget,
      qtyToIssueMax,
      lineQtyToIssue,
      lineQtyToIssueTolerancePerc,
      lineQtyToIssueRemaining,
      isWeightable,
      isIssueWholeHU,
      qtyRejectedReasons,
      scaleDevice,
    } = computeStepScanPropsFromActivity({ activity, lineId, stepId: step.id });

    return {
      //
      // Props needed for ScanHUAndGetQtyComponent:
      userInfo: computeStepScanUserInfoQtys({
        uom,
        lineQtyToIssue,
        lineQtyToIssueTolerancePerc,
        lineQtyToIssueRemaining,
      }),
      qtyTarget: qtyToIssueTarget,
      qtyMax: qtyToIssueMax,
      uom,
      qtyRejectedReasons,
      scaleDevice,
      //
      // Props which are needed by `onResult` function (see below):
      stepId: step.id,
      isWeightable,
      isIssueWholeHU,
    };
  };

  const dispatch = useDispatch();
  const history = useHistory();
  const onResult = ({ qty = 0, qtyRejected = 0, reason = null, resolvedBarcodeData }) => {
    console.log('onResult', { qty, qtyRejected, reason, resolvedBarcodeData });

    const stepId = resolvedBarcodeData.stepId;
    const isWeightable = !!resolvedBarcodeData.isWeightable;
    const isIssueWholeHU = !!resolvedBarcodeData.isIssueWholeHU;

    dispatch(
      updateManufacturingIssue({
        wfProcessId,
        activityId,
        lineId,
        stepId,
        huWeightGrossBeforeIssue: isWeightable && isIssueWholeHU ? qty : null,
        qtyIssued: qty,
        qtyRejected: isIssueWholeHU ? qtyRejected : 0,
        qtyRejectedReasonCode: isIssueWholeHU ? reason : null,
      })
    )
      .catch((axiosError) => toastError({ axiosError }))
      .finally(() => history.go(-1));
  };

  return (
    <ScanHUAndGetQtyComponent
      eligibleBarcode={eligibleBarcode}
      resolveScannedBarcode={resolveScannedBarcode}
      //
      // userInfo={userInfo}
      // qtyTarget={qtyToIssueTarget}
      // qtyMax={qtyToIssueMax}
      // uom={uom}
      // qtyRejectedReasons={qtyRejectedReasons}
      // scaleDevice={scaleDevice}
      //
      // Callbacks:
      onResult={onResult}
    />
  );
};

RawMaterialIssueStepScanComponent.propTypes = {
  wfProcessId: PropTypes.string.isRequired,
  activityId: PropTypes.string.isRequired,
  lineId: PropTypes.string.isRequired,
  stepId: PropTypes.string,
};

export default RawMaterialIssueStepScanComponent;
