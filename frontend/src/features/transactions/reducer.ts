import { createSlice, PayloadAction } from 'redux-starter-kit';
import { Transaction, LedgerSummary } from './interface';
import { History } from 'history';

export type TransactionApiErrorAction = {
  error: string;
};

export type UpdateTransactions = {
  projectId: number;
  timezone: string;
  frequencyType: string;
  ledgerSummaryType: string;
  startDate?: string;
  endDate?: string;
};

export type updateVisibleAction = {
  visible: boolean;
};

export type CreateTransaction = {
  projectId: number;
  amount: number;
  name: string;
  payer: string;
  transactionType: number;
  date: string;
  timezone: string;
  time?: string;
};

export type GetTransaction = {
  transactionId: number;
};

export type TransactionsAction = {
  ledgerSummary: LedgerSummary;
};

export type TransactionAction = {
  transaction: Transaction;
};

export type DeleteTransaction = {
  transactionId: number;
};

export type updateFrequencyAction = {
  frequencyType: string;
};

export type PatchTransaction = {
  transactionId: number;
  amount: number;
  name: string;
  payer: string;
  transactionType: number;
  date?: string;
  time?: string;
  timezone?: string;
};

export type MoveTransaction = {
  transactionId: number;
  targetProject: number;
  history: History;
};

export type ShareTransaction = {
  targetUser: string;
  transactionId: number;
  targetGroup: number;
  generateLink: boolean;
};

export type SetTransactionLabels = {
  transactionId: number;
  labels: number[];
};

let initialState = {
  transaction: {} as Transaction,
  ledgerSummary: {} as LedgerSummary,
  addTransactionVisible: false
};

const slice = createSlice({
  name: 'transaction',
  initialState,
  reducers: {
    transactionsReceived: (
      state,
      action: PayloadAction<TransactionsAction>
    ) => {
      const { ledgerSummary } = action.payload;
      state.ledgerSummary = ledgerSummary;
    },
    transactionReceived: (state, action: PayloadAction<TransactionAction>) => {
      const { transaction } = action.payload;
      state.transaction = transaction;
    },
    UpdateAddTransactionVisible: (
      state,
      action: PayloadAction<updateVisibleAction>
    ) => {
      const { visible } = action.payload;
      state.addTransactionVisible = visible;
    },
    transactionApiErrorReceived: (
      state,
      action: PayloadAction<TransactionApiErrorAction>
    ) => state,
    TransactionsUpdate: (state, action: PayloadAction<UpdateTransactions>) =>
      state,
    TransactionsCreate: (state, action: PayloadAction<CreateTransaction>) =>
      state,
    TransactionGet: (state, action: PayloadAction<GetTransaction>) => state,
    TransactionDelete: (state, action: PayloadAction<DeleteTransaction>) =>
      state,
    TransactionPatch: (state, action: PayloadAction<PatchTransaction>) => state,
    TransactionMove: (state, action: PayloadAction<MoveTransaction>) => state,
    TransactionShare: (state, action: PayloadAction<ShareTransaction>) => state,
    TransactionSetLabels: (
      state,
      action: PayloadAction<SetTransactionLabels>
    ) => state
  }
});

export const reducer = slice.reducer;
export const actions = slice.actions;
