import { Store } from '../../lib/store';
import { App } from '../../lib/app';
import { IReactStateComponentConfig } from '../../lib/app/services/plugin-builder';
import { History, HistoryProps } from './HistoryComponent';
import { cache } from '../../store';
import { onHistoryClick } from './history-events';

export default (app: App, store: Store): IReactStateComponentConfig => ({
  name: 'history',
  template: History,
  url: {},
  scope: {
    filter: () => {},
    user: () => {},
    error: () => {},
    onHistoryClicked: () => {},
    loadMore: () => {},
    getUsers: () => {},
  },
  controller: async (scope: HistoryProps, params, { syncUrl, setTitle }) => {
    syncUrl();
    setTitle();

    store.subscribe(
      'history',
      ({ error }) => {
        scope.user = app.getUser();
        scope.loadMore = ({offset, limit, ...rest}: {offset: number; limit: number; rest: object}) => {
          return cache.history.fetch({ offset, limit, ...rest });
        };
        scope.getUsers = () => {
          return cache.users.fetch();
        }
        scope.error = error;
      },
      scope
    );

    scope.onHistoryClicked = onHistoryClick(scope, store, app);
  }
});
