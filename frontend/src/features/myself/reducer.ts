import { createSlice, PayloadAction } from 'redux-starter-kit';

export type MyselfWithAvatar = {
  username?: string;
  avatar?: string;
  timezone?: string;
  before?: number;
  currency?: string;
  theme?: string;
};

export type MyselfApiErrorAction = {
  error: string;
};

export type UpdateMyself = {};

export type UpdateExpandedMyself = {
  updateSettings: boolean;
};

export type PatchMyself = {
  timezone?: string;
  before?: number;
  currency?: string;
  theme?: string;
};

let initialState = {
  username: '',
  avatar: '',
  timezone: '',
  before: 0,
  currency: '',
  theme: 'LIGHT',
};

const slice = createSlice({
  name: 'myself',
  initialState,
  reducers: {
    myselfDataReceived: (state, action: PayloadAction<MyselfWithAvatar>) => {
      const { username, avatar, timezone, before, currency, theme } = action.payload;
      if (username && username.length > 0) state.username = username;
      if (avatar && avatar.length > 0) state.avatar = avatar;
      if (timezone && timezone.length > 0) state.timezone = timezone;
      if (before || before === 0) state.before = before!;
      if (currency && currency.length > 0) state.currency = currency;
      if (theme) state.theme = theme;
    },
    myselfApiErrorReceived: (
      state,
      action: PayloadAction<MyselfApiErrorAction>
    ) => state,
    myselfUpdate: (state, action: PayloadAction<UpdateMyself>) => state,
    patchMyself: (state, action: PayloadAction<PatchMyself>) => state,
    expandedMyselfUpdate: (
      state,
      action: PayloadAction<UpdateExpandedMyself>
    ) => state
  }
});

export const reducer = slice.reducer;
export const actions = slice.actions;
