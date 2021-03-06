import { doFetch, doPost, doDelete, doPut, doPatch } from './api-helper';
import { Task, ReminderSetting } from '../features/tasks/interface';

export const fetchTasks = (projectId: number) => {
  return doFetch(`/api/projects/${projectId}/tasks`)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err.message);
    });
};

export const fetchCompletedTasks = (projectId: number) => {
  return doFetch(`/api/projects/${projectId}/completedTasks`)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err.message);
    });
};

export const getTaskById = (taskId: number) => {
    return doFetch(`/api/tasks/${taskId}`)
        .then((res) => res.json())
        .catch((err) => {
            throw Error(err.message);
        });
};

export const getCompletedTaskById = (taskId: number) => {
    return doFetch(`/api/completedTasks/${taskId}`)
        .then((res) => res.json())
        .catch((err) => {
            throw Error(err.message);
        });
};

export const deleteTaskById = (taskId: number) => {
  return doDelete(`/api/tasks/${taskId}`)
    .then((res) => res)
    .catch((err) => {
      throw Error(err.message);
    });
};

export const deleteCompletedTaskById = (taskId: number) => {
  return doDelete(`/api/completedTasks/${taskId}`)
    .then((res) => res)
    .catch((err) => {
      throw Error(err.message);
    });
};

export const createTask = (
  projectId: number,
  name: string,
  assignedTo: string,
  reminderSetting: ReminderSetting,
  timezone: string,
  dueDate?: string,
  dueTime?: string,
  duration?: number,
  recurrenceRule?: string
) => {
  const postBody = JSON.stringify({
    name: name,
    assignedTo: assignedTo,
    dueDate: dueDate,
    dueTime: dueTime,
    duration: duration,
    reminderSetting: reminderSetting,
    recurrenceRule: recurrenceRule,
    timezone: timezone,
  });
  return doPost(`/api/projects/${projectId}/tasks`, postBody)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err.message);
    });
};

export const putTasks = (projectId: number, tasks: Task[]) => {
  const putBody = JSON.stringify(tasks);
  return doPut(`/api/projects/${projectId}/tasks`, putBody).catch((err) => {
    throw Error(err.message);
  });
};

export const updateTask = (
  taskId: number,
  name?: string,
  assignedTo?: string,
  dueDate?: string,
  dueTime?: string,
  duration?: number,
  timezone?: string,
  reminderSetting?: ReminderSetting,
  recurrenceRule?: string
) => {
  const patchBody = JSON.stringify({
    name: name,
    assignedTo: assignedTo,
    dueDate: dueDate,
    dueTime: dueTime,
    duration: duration,
    timezone: timezone,
    reminderSetting: reminderSetting,
    recurrenceRule: recurrenceRule
  });
  return doPatch(`/api/tasks/${taskId}`, patchBody)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err);
    });
};

export const completeTaskById = (taskId: number, dateTime?: string) => {
  return doPost(`/api/tasks/${taskId}/complete`, dateTime)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err);
    });
};

export const uncompleteTaskById = (taskId: number) => {
  return doPost(`/api/tasks/${taskId}/uncomplete`)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err);
    });
};

export const setTaskLabels = (taskId: number, labels: number[]) => {
  const putBody = JSON.stringify(labels);
  return doPut(`/api/tasks/${taskId}/setLabels`, putBody)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err.message);
    });
};

export const moveToTargetProject = (taskId: number, targetProject: number) => {
  const postBody = JSON.stringify({
    targetProject: targetProject,
  });
  return doPost(`/api/tasks/${taskId}/move`, postBody)
    .then((res) => res)
    .catch((err) => {
      throw Error(err);
    });
};

export const shareTaskWithOther = (
  taskId: number,
  generateLink: boolean,
  targetUser?: string,
  targetGroup?: number,
  ttl?: number,
) => {
  const postBody = JSON.stringify({
    targetUser: targetUser,
    targetGroup: targetGroup,
    generateLink: generateLink,
    ttl: ttl
  });
  return doPost(`/api/tasks/${taskId}/share`, postBody)
    .then(res => generateLink ? res.json() : res)
    .catch((err) => {
      throw Error(err);
    });
};

export const getSharables = (taskId: number) => {
  return doFetch(`/api/tasks/${taskId}/sharables`)
      .then(res => res.json())
      .catch(err => {
        throw Error(err.message);
      });
};

export const revokeSharable = (taskId: number, user?: string, link?: string) => {
  const postBody = JSON.stringify({
    user: user,
    link: link
  });

  return doPost(`/api/tasks/${taskId}/revokeSharable`, postBody)
      .then(res => res)
      .catch(err => {
        throw Error(err);
      });
};

export const getContents = (taskId: number) => {
  return doFetch(`/api/tasks/${taskId}/contents`)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err.message);
    });
};

export const getCompletedTaskContents = (taskId: number) => {
    return doFetch(`/api/completedTasks/${taskId}/contents`)
        .then((res) => res.json())
        .catch((err) => {
            throw Error(err.message);
        });
};

export const addContent = (taskId: number, text: string) => {
  const postBody = JSON.stringify({
    text: text,
  });

  return doPost(`/api/tasks/${taskId}/addContent`, postBody)
    .then((res) => res.json())
    .catch((err) => {
      throw Error(err);
    });
};

export const deleteContent = (taskId: number, contentId: number) => {
  return doDelete(`/api/tasks/${taskId}/contents/${contentId}`)
    .then((res) => res)
    .catch((err) => {
      throw Error(err.message);
    });
};

export const updateContent = (
  taskId: number,
  contentId: number,
  text: string
) => {
  const patchBody = JSON.stringify({
    text: text,
  });
  return doPatch(`/api/tasks/${taskId}/contents/${contentId}`, patchBody)
    .then((res) => res)
    .catch((err) => {
      throw Error(err);
    });
};

export const getContentRevision = (taskId: number, contentId: number, revisionId: number) => {
    return doFetch(`/api/tasks/${taskId}/contents/${contentId}/revisions/${revisionId}`)
        .then((res) => res.json())
        .catch((err) => {
            throw Error(err.message);
        });
};
