package com.bulletjournal.repository;

import com.bulletjournal.authz.AuthorizationService;
import com.bulletjournal.authz.Operation;
import com.bulletjournal.contents.ContentType;
import com.bulletjournal.controller.models.CreateTaskParams;
import com.bulletjournal.controller.models.UpdateTaskParams;
import com.bulletjournal.controller.utils.IntervalHelper;
import com.bulletjournal.exceptions.ResourceNotFoundException;
import com.bulletjournal.hierarchy.HierarchyItem;
import com.bulletjournal.hierarchy.HierarchyProcessor;
import com.bulletjournal.hierarchy.TaskRelationsProcessor;
import com.bulletjournal.notifications.Event;
import com.bulletjournal.repository.models.*;
import com.bulletjournal.repository.utils.DaoHelper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TaskDaoJpa {

    private static final Gson GSON = new Gson();
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectTasksRepository projectTasksRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private CompletedTaskRepository completedTaskRepository;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public List<com.bulletjournal.controller.models.Task> getTasks(Long projectId) {
        Optional<ProjectTasks> projectTasksOptional = this.projectTasksRepository.findById(projectId);
        if (!projectTasksOptional.isPresent()) {
            return Collections.emptyList();
        }
        ProjectTasks projectTasks = projectTasksOptional.get();
        Project project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + projectId + " not found"));
        Map<Long, Task> tasks = this.taskRepository.findTaskByProject(project)
                .stream().collect(Collectors.toMap(Task::getId, n -> n));
        return TaskRelationsProcessor.processRelations(tasks, projectTasks.getTasks());
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Task getTask(Long id) {
        return this.taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + id + " not found"));
    }

    public List<com.bulletjournal.controller.models.Task> getTasksBetween(String user, ZonedDateTime startTime, ZonedDateTime endTime) {
        return this.taskRepository.findTasksOfAssigneeBetween(user,
                Timestamp.from(startTime.toInstant()), Timestamp.from(endTime.toInstant()))
                .stream().map(Task::toPresentationModel).collect(Collectors.toList());

    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Task create(Long projectId, String owner, CreateTaskParams createTaskParams) {
        Project project = this.projectRepository
                .findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + projectId + " not found"));

        Task task = new Task();
        task.setProject(project);
        task.setAssignedTo(owner);
        task.setDueDate(createTaskParams.getDueDate());
        task.setDueTime(createTaskParams.getDueTime());
        task.setOwner(owner);
        task.setName(createTaskParams.getName());
        task.setReminderSetting(createTaskParams.getReminderSetting());
        task.setTimezone(createTaskParams.getTimezone());
        task.setStartTime(Timestamp.from(IntervalHelper.getStartTime(createTaskParams.getDueDate(),
                createTaskParams.getDueTime(),
                createTaskParams.getTimezone()).toInstant()));
        task.setEndTime(Timestamp.from(IntervalHelper.getEndTime(createTaskParams.getDueDate(),
                createTaskParams.getDueTime(),
                createTaskParams.getTimezone()).toInstant()));
        return this.taskRepository.save(task);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public List<Event> partialUpdate(String requester, Long taskId, UpdateTaskParams updateTaskParams) {
        Task task = this.taskRepository
                .findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " not found"));

        this.authorizationService.checkAuthorizedToOperateOnContent(
                task.getOwner(), requester, ContentType.TASK, Operation.UPDATE,
                taskId, task.getProject().getOwner());

        DaoHelper.updateIfPresent(updateTaskParams.hasDuration(), updateTaskParams.getDuration(),
                task::setDuration);

        DaoHelper.updateIfPresent(
                updateTaskParams.hasName(), updateTaskParams.getName(), task::setName);

        List<Event> events = updateAssignee(requester, taskId, updateTaskParams, task);

        DaoHelper.updateIfPresent(
                updateTaskParams.hasDueDate(), updateTaskParams.getDueDate(), task::setDueDate);

        DaoHelper.updateIfPresent(
                updateTaskParams.hasDueTime(), updateTaskParams.getDueTime(), task::setDueTime);

        DaoHelper.updateIfPresent(
                updateTaskParams.hasTimezone(), updateTaskParams.getTimezone(), task::setTimezone);

        DaoHelper.updateIfPresent(updateTaskParams.hasReminderSetting(), updateTaskParams.getReminderSetting(),
                task::setReminderSetting);

        DaoHelper.updateIfPresent(updateTaskParams.hasDueDate() || updateTaskParams.hasDueTime(),
                Timestamp.from(IntervalHelper.getStartTime(updateTaskParams.getDueDate(),
                        updateTaskParams.getDueTime(), updateTaskParams.getTimezone()).toInstant()),
                task::setStartTime);

        DaoHelper.updateIfPresent(updateTaskParams.hasDueDate() || updateTaskParams.hasDueTime(),
                Timestamp.from(IntervalHelper.getEndTime(updateTaskParams.getDueDate(),
                        updateTaskParams.getDueTime(), updateTaskParams.getTimezone()).toInstant()),
                task::setEndTime);

        this.taskRepository.save(task);
        return events;
    }

    private List<Event> updateAssignee(String requester, Long taskId, UpdateTaskParams updateTaskParams, Task task) {
        List<Event> events = new ArrayList<>();
        String newAssignee = updateTaskParams.getAssignedTo();
        String oldAssignee = task.getAssignedTo();
        if (!Objects.equals(newAssignee, oldAssignee)) {
            task.setAssignedTo(newAssignee);
            if (!Objects.equals(newAssignee, requester)) {
                events.add(new Event(newAssignee, taskId, task.getName()));
            }
            if (!Objects.equals(oldAssignee, requester)) {
                events.add(new Event(oldAssignee, taskId, task.getName()));
            }
        }
        return events;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public CompletedTask complete(String requester, Long taskId) {
        Task task = this.taskRepository
                .findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " not found"));

        this.authorizationService.checkAuthorizedToOperateOnContent(
                task.getOwner(), requester, ContentType.TASK, Operation.UPDATE, taskId);

        CompletedTask completedTask = new CompletedTask(task);
        this.completedTaskRepository.save(completedTask);
        this.taskRepository.delete(task);

        // TODO: remove task in relations
        return completedTask;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void updateUserTasks(Long projectId, List<com.bulletjournal.controller.models.Task> tasks) {
        Optional<ProjectTasks> projectTasksOptional = this.projectTasksRepository.findById(projectId);
        final ProjectTasks projectTasks = projectTasksOptional.orElseGet(ProjectTasks::new);

        projectTasks.setTasks(TaskRelationsProcessor.processRelations(tasks));
        projectTasks.setProjectId(projectId);

        this.projectTasksRepository.save(projectTasks);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public List<Event> deleteTask(String requester, Long taskId) {
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " not found"));

        Project project = task.getProject();
        Long projectId = project.getId();
        this.authorizationService.checkAuthorizedToOperateOnContent(task.getOwner(), requester, ContentType.PROJECT,
                Operation.DELETE, projectId, project.getOwner());

        ProjectTasks projectTasks = this.projectTasksRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectTasks by " + projectId + " not found"));

        String relations = projectTasks.getTasks();

        // delete tasks and its subTasks
        List<Task> targetTasks = this.taskRepository.findAllById(HierarchyProcessor.getSubItems(relations, taskId));
        this.taskRepository.deleteAll(targetTasks);

        // Update task relations
        List<HierarchyItem> hierarchy = HierarchyProcessor.removeTargetItem(relations, taskId);
        projectTasks.setTasks(GSON.toJson(hierarchy));
        this.projectTasksRepository.save(projectTasks);

        return generateEvents(task, requester, project);
    }

    private List<Event> generateEvents(Task task, String requester, Project project) {
        List<Event> events = new ArrayList<>();
        for (UserGroup userGroup : project.getGroup().getUsers()) {
            if (!userGroup.isAccepted()) {
                continue;
            }
            // skip send event to self
            String username = userGroup.getUser().getName();
            if (userGroup.getUser().getName().equals(requester)) {
                continue;
            }
            events.add(new Event(username, task.getId(), task.getName()));
        }
        return events;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public List<CompletedTask> getCompletedTasks(Long projectId) {
        // TODO: sort by last_updated
        Project project = this.projectRepository
                .findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + projectId + " not found"));
        return this.completedTaskRepository.findCompletedTaskByProject(project);
    }
}
