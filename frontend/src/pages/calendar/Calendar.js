import React, { useEffect } from 'react';
import PropTypes from 'prop-types';

import FullCalendar from '@fullcalendar/react';
import timeGridPlugin from '@fullcalendar/timegrid';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import resourceTimelinePlugin from '@fullcalendar/resource-timeline';

import '@fullcalendar/common/main.css';
import '@fullcalendar/daygrid/main.css';
import '@fullcalendar/timegrid/main.css';
import deLocale from '@fullcalendar/core/locales/de';

import * as api from './api/calendar';
import { normalizeDateTime } from './utils/calendarUtils';

import SimulationsDropDown from './components/SimulationsDropDown';
import { getCurrentActiveLanguage } from '../../utils/locale';
import { useCalendarData } from './hooks/useCalendarData';
import { useCalendarWebsocketEvents } from './hooks/useCalendarWebsocketEvents';

import './calendar.scss';
import ConflictsSummary from './components/ConflictsSummary';
import CalendarResourceLabel from './components/CalendarResourceLabel';

const Calendar = ({
  simulationId: initialSelectedSimulationId,
  onParamsChanged,
}) => {
  const calendarData = useCalendarData({
    simulationId: initialSelectedSimulationId,
    fetchAvailableSimulationsFromAPI: api.fetchAvailableSimulations,
    fetchEntriesFromAPI: api.fetchCalendarEntries,
    fetchConflictsFromAPI: api.fetchConflicts,
  });
  const simulationId = calendarData.getSimulationId();

  useEffect(() => {
    onParamsChanged && onParamsChanged({ simulationId });
  }, [simulationId]);

  useEffect(() => {
    console.log('Loading calendars...');
    api.fetchAvailableCalendars().then(calendarData.setCalendars);
  }, []);

  useCalendarWebsocketEvents({
    simulationId,
    onWSEvents: calendarData.applyWSEvents,
  });

  const fetchCalendarEntries = (fetchInfo, successCallback) => {
    calendarData.loadEntriesWithConflicts({
      startDate: normalizeDateTime(fetchInfo.startStr),
      endDate: normalizeDateTime(fetchInfo.endStr),
      onFetchSuccess: successCallback,
    });
  };

  const handleEventClick = (params) => {
    if (params.event.url) {
      params.jsEvent.preventDefault();
      window.open(params.event.url, '_blank');
    }
  };

  const handleEventDragOrResize = (params) => {
    if (
      params.event.startStr === params.oldEvent.startStr &&
      params.event.endStr === params.oldEvent.endStr &&
      params.event.allDay === params.oldEvent.allDay
    ) {
      console.log('handleEventDragOrResize: no change', { params });
      return;
    }

    api
      .addOrUpdateCalendarEntry({
        id: params.event.id,
        simulationId,
        startDate: params.event.start,
        endDate: params.event.end,
        allDay: params.event.allDay,
      })
      .then(calendarData.addEntriesArray)
      .catch((error) => {
        console.log('Got error', error);
        params.revert();
      });
  };

  return (
    <div className="calendar-container">
      <div className="calendar-top">
        <div className="calendar-top-left">
          <ConflictsSummary conflictsCount={calendarData.getConflictsCount()} />
        </div>
        <div className="calendar-top-center" />
        <div className="calendar-top-right">
          <SimulationsDropDown
            simulations={calendarData.getSimulationsArray()}
            selectedSimulationId={simulationId}
            onOpenDropdown={() => calendarData.loadSimulationsFromAPI()}
            onSelect={(simulation) => {
              calendarData.setSimulationId(simulation?.simulationId);
            }}
            onNew={() => {
              api
                .createSimulation({ copyFromSimulationId: simulationId })
                .then(calendarData.addSimulationAndSelect);
            }}
          />
        </div>
      </div>
      <FullCalendar
        schedulerLicenseKey="GPL-My-Project-Is-Open-Source"
        locales={[deLocale]}
        locale={getCurrentActiveLanguage()}
        views={{
          resourceTimelineYear: {
            slotDuration: { months: 1 },
            slotLabelInterval: { months: 1 },
            slotLabelFormat: [{ month: 'long' }],
          },
        }}
        initialView="resourceTimelineYear"
        plugins={[
          dayGridPlugin,
          timeGridPlugin,
          interactionPlugin,
          resourceTimelinePlugin,
        ]}
        weekends="true"
        editable="true"
        headerToolbar={{
          left: 'prev,today,next',
          center: 'title',
          right:
            'dayGridMonth resourceTimelineDay,resourceTimelineWeek,resourceTimelineMonth,resourceTimelineYear',
        }}
        resourceAreaHeaderContent="Resources"
        resources={calendarData.getResourcesArray()}
        resourceLabelContent={(params) => {
          //console.log('resourceLabelContent', { params });
          return (
            <CalendarResourceLabel
              title={params.resource.title}
              conflictsCount={params.resource.extendedProps.conflictsCount}
            />
          );
        }}
        eventSources={[{ events: fetchCalendarEntries }]}
        //dateClick={handleDateClick}
        eventClick={handleEventClick}
        eventClassNames={(params) => {
          if (params.event.extendedProps.conflict) {
            return ['has-conflict'];
          }
        }}
        eventContent={(params) => {
          //console.log('eventContent', { params });
          return <div>{params.event.title}</div>;
        }}
        eventDragStart={(event) => {
          console.log('eventDragStart', { event });
        }}
        eventDragStop={(event) => {
          console.log('eventDragStop', { event });
        }}
        eventDrop={(params) => {
          console.log('eventDrop', { params });

          if (params.oldResource?.id !== params.newResource?.id) {
            console.log('moving event to another resource is not allowed');
            params.revert();
            return;
          }

          handleEventDragOrResize(params);
        }}
        drop={(event) => {
          console.log('drop', { event });
        }}
        eventResizeStart={(event) => {
          console.log('eventResizeStart', { event });
        }}
        eventResizeStop={(event) => {
          console.log('eventResizeStop', { event });
        }}
        eventResize={(params) => {
          console.log('eventResize', { params });
          handleEventDragOrResize(params);
        }}
        eventReceive={(event) => {
          console.log('eventReceive', { event });
          event.revert();
        }}
        eventLeave={(event) => {
          console.log('eventLeave', { event });
          event.revert();
        }}
      />
    </div>
  );
};

Calendar.propTypes = {
  simulationId: PropTypes.string,
  onParamsChanged: PropTypes.func,
};

export default Calendar;
