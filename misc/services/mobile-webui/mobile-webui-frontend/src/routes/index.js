import React from 'react';
import { ConnectedRouter } from 'connected-react-router';
import { Route, Switch } from 'react-router';

import Main from '../components/Main';
import Header from '../components/Header';
import ViewHeader from '../containers/ViewHeader';
import LoginView from '../components/LoginView';

import PrivateRoute from './PrivateRoute';
import { history } from '../store/store';
import ScreenToaster from '../components/ScreenToaster';

import { commonRoutes } from './common';
import { launchersRoutes } from './launchers';
import { workflowRoutes } from './workflow';
import { manufacturingRoutes } from './manufacturing';
import { distributionRoutes } from './distribution';
import { getApplicationRoutes } from '../apps';

const routesArray = [
  ...commonRoutes,
  ...launchersRoutes,
  ...workflowRoutes,
  ...distributionRoutes,
  ...manufacturingRoutes,
  ...getApplicationRoutes(),
];

// console.log('routes: ', routesArray);

const Routes = () => {
  return (
    <ConnectedRouter history={history} basename="./">
      <Main>
        <Switch>
          <Route exact path="/login" component={LoginView} />
          <PrivateRoute path="/">
            <div>
              {routesArray.map(({ path, Component }) => (
                <Route key={path} exact path={path}>
                  <Header appName="metasfresh mobile" hidden />
                  <ViewHeader />
                  <Component />
                  <ScreenToaster />
                </Route>
              ))}
            </div>
          </PrivateRoute>
        </Switch>
      </Main>
    </ConnectedRouter>
  );
};

export default Routes;
