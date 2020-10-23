import React from 'react';
import { mount, shallow, render } from 'enzyme';
import { repeat } from 'lodash';

import RawWidget from '../../../components/widget/RawWidget';
import MasterWidget from '../../../components/widget/MasterWidget';
import fixtures from '../../../../test_setup/fixtures/raw_widget.json';
import rawWidgetFixtures from '../../../../test_setup/fixtures/widget/raw_widget.json';

const createDummyProps = function(props) {
  return {
    allowShortcut: jest.fn(),
    disableShortcut: jest.fn(),
    openModal: jest.fn(),
    patch: jest.fn(),
    updatePropertyValue: jest.fn(),
    modalVisible: false,
    timeZone: 'Europe/Berlin',
    entity: "window",
    dataId: "1001282",
    ...props,
  };
};

describe('MasterWidget component', () => {
  it('renders widget without errors', () => {
    const localFixtures = rawWidgetFixtures.text;
    const props = createDummyProps(
      {
        ...localFixtures.data1,
        ...localFixtures.props1,
      },
    );
    const wrapper = shallow(<MasterWidget {...props} />);

    expect(wrapper.find(RawWidget).length).toBe(1);
  });

  it('clears stored value, when `clearValue` prop is present', () => {
    const localFixtures = rawWidgetFixtures.text;
    const props = createDummyProps(
      {
        ...localFixtures.data1,
        ...localFixtures.props1,
        widgetData: [{ ...fixtures.text.data1 }],
        clearValue: true,
      },
    );
    const widgetData = props.widgetData[0];
    const wrapper = shallow(<MasterWidget {...props} />);
    const html = wrapper.html();

    expect(html.includes(widgetData.value)).toBeFalsy();
    expect(wrapper.state('value')).toBe('');
  });

  it('sets and clears the updated state value when widget value has changed', () => {
    jest.useFakeTimers();

    const localFixtures = rawWidgetFixtures.text;
    const props = createDummyProps(
      {
        ...localFixtures.data1,
        ...localFixtures.props1,
        widgetData: [{ ...fixtures.text.data1 }],
      },
    );
    const widgetData = props.widgetData[0];
    const wrapper = shallow(<MasterWidget {...props} />);
    const html = wrapper.html();

    expect(html.includes(widgetData.value)).toBeTruthy();

    const newValue = repeat(widgetData.value, 2);
    const newWidgetData = [{ ...widgetData, value: newValue }]
    wrapper.setProps({ widgetData: newWidgetData})

    wrapper.update();

    expect(wrapper.state('updated')).toBe(true);

    jest.runOnlyPendingTimers();

    expect(wrapper.state('updated')).toBe(false);
    expect(wrapper.state('value')).toBe(newValue);
  });
});
