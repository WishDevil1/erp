import React, { useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';

import { trl } from '../../../../utils/translations';
import { pushHeaderEntry } from '../../../../actions/HeaderActions';
import { getLineById, getStepsArrayFromLine } from '../../../../reducers/wfProcesses';

import { manufacturingStepScreenLocation } from '../../../../routes/manufacturing_issue';
import ButtonWithIndicator from '../../../../components/buttons/ButtonWithIndicator';
import ButtonQuantityProp from '../../../../components/buttons/ButtonQuantityProp';

const RawMaterialIssueLineScreen = () => {
  const {
    url,
    params: { applicationId, workflowId: wfProcessId, activityId, lineId },
  } = useRouteMatch();

  const { productName, steps } = useSelector(
    (state) => getPropsFromState({ state, wfProcessId, activityId, lineId }),
    shallowEqual
  );

  const dispatch = useDispatch();
  useEffect(() => {
    dispatch(
      pushHeaderEntry({
        location: url,
        caption: 'Issue', // TODO: trl
        values: [
          {
            caption: trl('general.Product'),
            value: productName,
          },
        ],
      })
    );
  }, []);

  const history = useHistory();
  const onButtonClick = ({ stepId }) => {
    history.push(manufacturingStepScreenLocation({ applicationId, wfProcessId, activityId, lineId, stepId }));
  };

  return (
    <div className="section pt-2">
      {steps.length > 0 &&
        steps.map((stepItem) => {
          return (
            <ButtonWithIndicator
              key={stepItem.id}
              caption={stepItem.locatorName}
              completeStatus={stepItem.completeStatus}
              onClick={() => onButtonClick({ stepId: stepItem.id })}
            >
              <ButtonQuantityProp
                qtyCurrent={stepItem.qtyIssued ?? 0}
                qtyTarget={stepItem.qtyToIssue}
                uom={stepItem.uom}
                applicationId={applicationId}
                subtypeId="issues"
              />
            </ButtonWithIndicator>
          );
        })}
    </div>
  );
};

const getPropsFromState = ({ state, wfProcessId, activityId, lineId }) => {
  const line = getLineById(state, wfProcessId, activityId, lineId);
  return {
    productName: line?.productName,
    steps: getStepsArrayFromLine(line),
  };
};

export default RawMaterialIssueLineScreen;
