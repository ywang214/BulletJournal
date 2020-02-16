import { takeEvery, call, all, put } from 'redux-saga/effects';
import { toast } from 'react-toastify';
import {
  actions as groupsActions,
  ApiErrorAction,
  GroupsAction
} from './reducer';
import { PayloadAction } from 'redux-starter-kit';
import { fetchGroups } from '../../apis/groupApis';

function* apiErrorReceived(action: PayloadAction<ApiErrorAction>) {
  yield call(toast.error, `Error Received: ${action.payload.error}`);
}

function* groupsUpdate(action: PayloadAction<GroupsAction>) {
  try {
    const data = yield call(fetchGroups);
    yield put(groupsActions.groupsReceived(data.groups));
  } catch (error) {
    yield call(toast.error, `Error Received: ${error}`);
  }
}

export default function* userSagas() {
  yield all([
    yield takeEvery(groupsActions.groupsReceived.type, apiErrorReceived),
    yield takeEvery(groupsActions.groupsUpdate.type, groupsUpdate)
  ]);
}