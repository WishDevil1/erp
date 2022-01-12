import React, { PureComponent } from 'react';
import { connect } from 'react-redux';
import { goBack, push } from 'connected-react-router';
import PropTypes from 'prop-types';
import counterpart from 'counterpart';
import { toastError } from '../../../utils/toast';
import { disposeHU, getDisposalReasonsArray } from '../api';

import ButtonWithIndicator from '../../../components/ButtonWithIndicator';
import QtyReasonsRadioGroup from '../../../components/QtyReasonsRadioGroup';
import { getHandlingUnitInfoFromGlobalState } from '../reducers';
import { HUInfoComponent } from '../components/HUInfoComponent';

class HUDisposalScreen extends PureComponent {
  constructor(props) {
    super(props);

    this.state = {
      disposalReasons: [],
      selectedDisposalReasonKey: null,
    };
  }

  componentDidMount() {
    const { handlingUnitInfo, dispatch } = this.props;
    console.log('handlingUnitInfo=', handlingUnitInfo);
    if (!handlingUnitInfo) {
      dispatch(goBack());
      return;
    }

    this.loadDisposalReasons();
  }

  loadDisposalReasons = () => {
    getDisposalReasonsArray()
      .then((disposalReasons) => {
        this.setState({
          ...this.state,
          disposalReasons,
          selectedDisposalReasonKey: null,
        });
      })
      .catch((axiosError) => {
        //toastError({ axiosError });
        console.trace('Failed loading disposal reasons', axiosError);
      });
  };

  onDisposalReasonSelected = (disposalReasonKey) => {
    this.setState({
      ...this.state,
      selectedDisposalReasonKey: disposalReasonKey,
    });
  };

  onDisposeClick = () => {
    const { handlingUnitInfo, dispatch } = this.props;
    const { selectedDisposalReasonKey } = this.state;
    disposeHU({
      huId: handlingUnitInfo.id,
      reasonCode: selectedDisposalReasonKey,
    })
      .then(() => {
        dispatch(push('/'));
      })
      .catch((axiosError) => toastError({ axiosError }));
  };

  render() {
    const { handlingUnitInfo } = this.props;
    if (!handlingUnitInfo) return null;

    const { disposalReasons, selectedDisposalReasonKey } = this.state;

    return (
      <>
        <HUInfoComponent handlingUnitInfo={handlingUnitInfo} />

        <div className="pt-3 section">
          <div className="centered-text is-size-5">
            <QtyReasonsRadioGroup reasons={disposalReasons} onReasonSelected={this.onDisposalReasonSelected} />
          </div>
        </div>

        <div className="pt-3 section">
          <div className="centered-text is-size-5">
            <div className="mt-0">
              <button
                className="button is-outlined complete-btn"
                onClick={this.onDisposeClick}
                disabled={!selectedDisposalReasonKey}
              >
                <ButtonWithIndicator caption={counterpart.translate('huManager.action.dispose.buttonCaption')} />
              </button>
            </div>
          </div>
        </div>
      </>
    );
  }
}

const mapStateToProps = (globalState) => {
  return {
    handlingUnitInfo: getHandlingUnitInfoFromGlobalState(globalState),
  };
};

HUDisposalScreen.propTypes = {
  handlingUnitInfo: PropTypes.object.isRequired,
  dispatch: PropTypes.func.isRequired,
};

export default connect(mapStateToProps)(HUDisposalScreen);
