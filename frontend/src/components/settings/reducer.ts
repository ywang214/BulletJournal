import { createSlice, PayloadAction } from 'redux-starter-kit';

type GetTimezoneAction = {};

type TimezoneAction = {
  timezone: string;
};

type BeforeAction = {
  before: number;
};

export enum ReminderBeforeTask {
  ZERO_MIN_BEFORE = 0,
  FIVE_MIN_BEFORE = 1,
  TEN_MIN_BEFORE = 2,
  THIRTY_MIN_BEFORE = 3,
  ONE_HR_BEFORE = 4,
  TWO_HR_BEFORE = 5,
  NONE = 6
}

export const ReminderBeforeTaskText = [
  '0 minute before',
  '5 minutes before',
  '10 minutes before',
  '30 minutes before',
  '1 hour before',
  '2 hours before',
  'No reminder'
];

let initialState = {
  timezone: '',
  before: 0
};

const slice = createSlice({
  name: 'settings',
  initialState,
  reducers: {
    updateTimezone: (state, action: PayloadAction<TimezoneAction>) => {
      const { timezone } = action.payload;
      state.timezone = timezone;
    },
    updateBefore: (state, action: PayloadAction<BeforeAction>) => {
      const { before } = action.payload;
      state.before = before;
    }
  }
});

export const reducer = slice.reducer;
export const actions = slice.actions;