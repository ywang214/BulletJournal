import { actions } from './reducer';

export const updateMyself = () => actions.myselfUpdate({});
export const updateExpandedMyself = (updateSettings: boolean) =>
  actions.expandedMyselfUpdate({ updateSettings: updateSettings });
export const updateTimezone = (timezone: string) =>
  actions.myselfDataReceived({
    timezone: timezone
  });
export const updateBefore = (before: number) =>
  actions.myselfDataReceived({
    before: before
  });
export const patchMyself = (timezone?: string, before?: number) =>
  actions.patchMyself({ timezone, before });