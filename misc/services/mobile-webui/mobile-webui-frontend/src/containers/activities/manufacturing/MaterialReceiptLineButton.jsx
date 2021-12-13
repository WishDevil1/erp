import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { push } from 'connected-react-router';
import { connect } from 'react-redux';

import { manufacturingLineScreenLocation } from '../../../routes/manufacturing';
import ButtonWithIndicator from '../../../components/ButtonWithIndicator';
import ButtonQuantityProp from '../../../components/ButtonQuantityProp';

class MaterialReceiptLineButton extends PureComponent {
  handleClick = () => {
    const { push, wfProcessId, activityId, lineId } = this.props;
    const location = manufacturingLineScreenLocation({ wfProcessId, activityId, lineId });

    push(location);
  };

  render() {
    const { caption, uom, qtyCurrent, qtyTarget, completeStatus, lineId, isUserEditable } = this.props;

    return (
      <button
        key={lineId}
        className="button is-outlined complete-btn"
        disabled={!isUserEditable}
        onClick={this.handleClick}
      >
        <ButtonWithIndicator caption={caption} completeStatus={completeStatus}>
          <ButtonQuantityProp
            qtyCurrent={qtyCurrent}
            qtyTarget={qtyTarget}
            uom={uom}
            appId="mfg"
            subtypeId="receipts"
          />
        </ButtonWithIndicator>
      </button>
    );
  }
}

MaterialReceiptLineButton.propTypes = {
  //
  // Props
  wfProcessId: PropTypes.string.isRequired,
  activityId: PropTypes.string.isRequired,
  lineId: PropTypes.string.isRequired,
  caption: PropTypes.string.isRequired,
  isUserEditable: PropTypes.bool.isRequired,
  completeStatus: PropTypes.string.isRequired,
  uom: PropTypes.string.isRequired,
  qtyCurrent: PropTypes.number.isRequired,
  qtyTarget: PropTypes.number.isRequired,
  //
  // Actions
  push: PropTypes.func.isRequired,
};

export default connect(null, { push })(MaterialReceiptLineButton);
