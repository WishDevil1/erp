import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { forEach } from 'lodash';

import PickLineButton from './PickLineButton';
import * as CompleteStatus from '../../../constants/CompleteStatus';

const computeLineQuantities = (line) => {
  let picked = 0;
  let toPick = 0;
  let uom = '';

  forEach(line.steps, (step) => {
    const { qtyToPick, uom: stepUom } = step;

    picked += computeStepQtyPickedTotal(step);
    toPick += qtyToPick;
    uom = stepUom;
  });

  return { picked, toPick, uom };
};

const computeStepQtyPickedTotal = (step) => {
  let qtyPickedTotal = 0;

  if (step.mainPickFrom.qtyPicked) {
    qtyPickedTotal += step.mainPickFrom.qtyPicked;
  }

  if (step.pickFromAlternatives) {
    const qtyPickedInAltSteps = Object.values(step.pickFromAlternatives).reduce(
      (accum, pickFromAlternative) => accum + pickFromAlternative.qtyPicked,
      0
    );

    qtyPickedTotal += qtyPickedInAltSteps;
  }

  return qtyPickedTotal;
};

class PickProductsActivity extends Component {
  render() {
    const {
      componentProps: { lines },
      activityState,
      wfProcessId,
      activityId,
    } = this.props;
    const dataStored = activityState ? activityState.dataStored : {};
    const { completeStatus, isUserEditable } = dataStored;

    return (
      <div className="pick-products-activity-container mt-5">
        {activityState && lines.length > 0
          ? lines.map((lineItem, lineIndex) => {
              const lineId = '' + lineIndex;
              const { picked, toPick, uom } = computeLineQuantities(dataStored.lines[lineIndex]);

              return (
                <PickLineButton
                  key={lineId}
                  wfProcessId={wfProcessId}
                  activityId={activityId}
                  lineId={lineId}
                  caption={lineItem.caption}
                  isUserEditable={isUserEditable}
                  completeStatus={completeStatus || CompleteStatus.NOT_STARTED}
                  qtyPicked={picked}
                  qtyToPick={toPick}
                  uom={uom}
                />
              );
            })
          : null}
      </div>
    );
  }
}

PickProductsActivity.propTypes = {
  wfProcessId: PropTypes.string,
  activityId: PropTypes.string,
  caption: PropTypes.string,
  componentProps: PropTypes.object,
  activityState: PropTypes.object,
};

export default PickProductsActivity;
