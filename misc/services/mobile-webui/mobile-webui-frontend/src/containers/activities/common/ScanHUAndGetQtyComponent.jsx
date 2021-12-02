import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { toastError } from '../../../utils/toast';
import CodeScanner from '../scan/CodeScanner';
import PickQuantityPrompt from '../PickQuantityPrompt';
import QtyReasonsView from '../QtyReasonsView';

class ScanHUAndGetQtyComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      qtyInputPromptVisible: false,
      reasonsPanelVisible: false,
      newQuantity: 0,
      scannedBarcode: null,
      qtyRejected: 0,
    };
  }

  hidePrompt = () => {
    this.setState({ promptVisible: false });
  };

  onBarcodeScanned = ({ scannedBarcode }) => {
    const { qtyTarget, onResult, invalidBarcodeMessageKey } = this.props;

    if (this.isEligibleBarcode(scannedBarcode)) {
      // in some cases we don't need store quantity (ie manufacturing receipts)
      if (qtyTarget) {
        this.setState({ promptVisible: true });
      } else {
        onResult({ qty: 0, reason: null });
      }
    } else {
      // show an error to user but keep scanning...
      toastError({ messageKey: invalidBarcodeMessageKey || 'activities.picking.notEligibleHUBarcode' });
    }
  };

  onQtyEntered = (qty) => {
    const { qtyTarget, onResult, invalidQtyMessageKey } = this.props;

    const inputQty = parseFloat(qty);
    if (isNaN(inputQty)) {
      return;
    }

    const isValidQty = this.validateQtyInput(inputQty);
    if (isValidQty) {
      this.setState({ newQuantity: inputQty });

      if (inputQty !== qtyTarget) {
        const qtyRejected = qtyTarget - inputQty;
        this.setState({ reasonsPanelVisible: true, qtyRejected });
      } else {
        onResult({ qty: inputQty });
      }

      this.setState({ promptVisible: false });
    } else {
      toastError({ messageKey: invalidQtyMessageKey || 'activities.picking.invalidQtyPicked' });
    }
  };

  hideReasonsPanel = (reason) => {
    const { onResult } = this.props;
    this.setState({ reasonsPanelVisible: false });

    onResult({ qty: this.state.newQuantity, reason });
  };

  validateQtyInput = (numberInput) => {
    const { qtyTarget } = this.props;

    return numberInput >= 0 && numberInput <= qtyTarget;
  };

  isEligibleBarcode = (scannedBarcode) => {
    const { eligibleBarcode } = this.props;

    console.log(`checking eligibleBarcode=${eligibleBarcode} vs scannedBarcode=${scannedBarcode}`);
    // in some cases we accept whatever code user scans and we're not constraining it
    return eligibleBarcode ? scannedBarcode === eligibleBarcode : true;
  };

  render() {
    const { uom, qtyCaption, qtyInitial, qtyTarget } = this.props;
    const { promptVisible, reasonsPanelVisible, qtyRejected } = this.state;

    return (
      <div className="mt-0">
        {reasonsPanelVisible ? (
          <QtyReasonsView onHide={this.hideReasonsPanel} uom={uom} qtyRejected={qtyRejected} />
        ) : (
          <>
            {promptVisible ? (
              <PickQuantityPrompt
                qtyInitial={qtyInitial}
                qtyTarget={qtyTarget}
                qtyCaption={qtyCaption}
                onQtyChange={this.onQtyEntered}
                onCloseDialog={this.hidePrompt}
              />
            ) : (
              <CodeScanner onBarcodeScanned={this.onBarcodeScanned} />
            )}
          </>
        )}
      </div>
    );
  }
}

ScanHUAndGetQtyComponent.propTypes = {
  //
  // Props
  eligibleBarcode: PropTypes.string,
  qtyCaption: PropTypes.string,
  qtyTarget: PropTypes.number,
  qtyInitial: PropTypes.number,
  uom: PropTypes.string,
  // Error messages:
  invalidBarcodeMessageKey: PropTypes.string,
  invalidQtyMessageKey: PropTypes.string,
  //
  // Functions
  onResult: PropTypes.func,
};

export default ScanHUAndGetQtyComponent;
