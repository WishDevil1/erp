import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import counterpart from 'counterpart';

import { pushHeaderEntry } from '../../../actions/HeaderActions';
import StepButton from '../common/StepButton';
import Indicator from '../../../components/Indicator';
import * as CompleteStatus from '../../../constants/CompleteStatus';

class DistributionStepButton extends PureComponent {
  handleClick = () => {
    const { pickFromLocator } = this.props;
    const { dispatch, onHandleClick } = this.props;

    console.log('DistributionStepButton: ', this.props);

    onHandleClick();
    dispatch(
      pushHeaderEntry({
        location,
        values: [
          {
            caption: counterpart.translate('general.Locator'),
            value: pickFromLocator.caption,
          },
        ],
      })
    );
  };

  render() {
    const { lineId, pickFromLocator, uom, qtyPicked, completeStatus, qtyToMove } = this.props;

    return (
      <div className="mt-3">
        <button
          key={lineId}
          className="button is-outlined complete-btn pick-higher-btn"
          onClick={() => this.handleClick()}
        >
          <div className="full-size-btn">
            <div className="left-btn-side" />

            <div className="caption-btn">
              <div className="rows">
                <div className="row is-full pl-5">{pickFromLocator.caption}</div>
                <div className="row is-full is-size-7">
                  <div className="picking-row-info">
                    <div className="picking-to-pick">{counterpart.translate('activities.distribution.target')}:</div>
                    <div className="picking-row-qty">
                      {qtyToMove} {uom}
                    </div>
                    <div className="picking-row-picking">
                      {counterpart.translate('activities.distribution.picked')}:
                    </div>
                    <div className="picking-row-picked">
                      {qtyPicked} {uom}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="right-btn-side pt-4">
              <Indicator completeStatus={completeStatus || CompleteStatus.NOT_STARTED} />
            </div>
          </div>
        </button>
      </div>
    );
  }
}

DistributionStepButton.propTypes = {
  //
  // Props
  wfProcessId: PropTypes.string.isRequired,
  activityId: PropTypes.string.isRequired,
  lineId: PropTypes.string.isRequired,
  stepId: PropTypes.string.isRequired,
  productName: PropTypes.string.isRequired,
  pickFromLocator: PropTypes.object.isRequired,
  pickFromHU: PropTypes.object,
  uom: PropTypes.string,
  qtyPicked: PropTypes.number,
  qtyToMove: PropTypes.number.isRequired,
  completeStatus: PropTypes.string.isRequired,
  //
  // Actions
  onHandleClick: PropTypes.func.isRequired,
  dispatch: PropTypes.func.isRequired,
};

export default StepButton(DistributionStepButton);
