package com.bulletjournal.controller;

import com.bulletjournal.clients.UserClient;
import com.bulletjournal.controller.models.ProjectItems;
import com.bulletjournal.controller.models.ProjectType;
import com.bulletjournal.controller.models.Task;
import com.bulletjournal.controller.models.Transaction;
import com.bulletjournal.controller.utils.IntervalHelper;
import com.bulletjournal.controller.utils.ProjectItemsGrouper;
import com.bulletjournal.repository.TaskDaoJpa;
import com.bulletjournal.repository.TransactionDaoJpa;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class ProjectItemController {

    protected static final String PROJECT_ITEMS_ROUTE = "/api/projectItems";

    @Autowired
    private TaskDaoJpa taskDaoJpa;

    @Autowired
    private TransactionDaoJpa transactionDaoJpa;

    @GetMapping(PROJECT_ITEMS_ROUTE)
    @ResponseBody
    public List<ProjectItems> getProjectItems(
            @Valid @RequestParam List<ProjectType> types,
            @NotBlank @RequestParam String startDate,
            @NotBlank @RequestParam String endDate,
            @NotBlank @RequestParam String timezone) {

        String username = MDC.get(UserClient.USER_NAME_KEY);

        // Set start time and end time
        ZonedDateTime startTime = IntervalHelper.getStartTime(startDate, null, timezone);
        ZonedDateTime endTime = IntervalHelper.getEndTime(endDate, null, timezone);

        Map<ZonedDateTime, List<Task>> taskMap = null;
        Map<ZonedDateTime, List<Transaction>> transactionMap = null;

        for (ProjectType projectType : types) {
            switch (projectType.getValue()) {
                case 0: // Task
                    // Query tasks from database with username, start time, end time
                    List<Task> tasks = taskDaoJpa.getTasksBetween(username, startTime, endTime);

                    // Group tasks by date
                    taskMap = ProjectItemsGrouper.groupTasksByDate(tasks);
                    break;
                case 2: // Ledger
                    // Query transactions from database with username, start time, end time
                    List<Transaction> transactions = transactionDaoJpa.getTransactionsBetween(username, startTime, endTime);

                    // Group transactions by date
                    transactionMap = ProjectItemsGrouper.groupTransactionsByDate(transactions);
                    break;
                default:
            }
        }

        Map<ZonedDateTime, ProjectItems> projectItemsMap = ProjectItemsGrouper.mergeMap(taskMap, transactionMap);
        return ProjectItemsGrouper.getProjectItems(projectItemsMap);
    }

}
