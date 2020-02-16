package com.bulletjournal.controller;

import com.bulletjournal.controller.models.*;
import com.bulletjournal.controller.utils.ProjectRelationsProcessorTest;
import com.bulletjournal.notifications.Action;
import com.bulletjournal.notifications.JoinGroupEvent;
import com.bulletjournal.notifications.JoinGroupResponseEvent;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests {@link ProjectController}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProjectControllerTest {
    private static final String ROOT_URL = "http://localhost:";
    private final String expectedOwner = "BulletJournal";
    private final String[] sampleUsers = {
            "Xavier",
            "bbs1024",
            "ccc",
            "Thinker",
            "Joker",
            "mqm",
            "hero",
            "bean",
            "xlf",
            "999999",
            "0518",
            "Scarlet",
            "lsx9981"};
    private TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    int randomServerPort;

    @Before
    public void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    public void testCRUD() throws Exception {
        answerNotifications();
        String projectName = "P0";
        List<GroupsWithOwner> groups = createGroups(expectedOwner);
        Group group = groups.get(0).getGroups().get(0);
        int count = 1;
        for (String username : Arrays.asList(sampleUsers).subList(0, 3)) {
            group = addUserToGroup(group, username, ++count);
        }

        group = groups.get(0).getGroups().get(2);
        addUsersToGroup(group, Arrays.asList(sampleUsers).subList(0, 5));

        Project p1 = createProject(projectName, expectedOwner);
        p1 = updateProject(p1);

        // create other projects
        Project p2 = createProject("P2", expectedOwner);
        Project p3 = createProject("P3", expectedOwner);
        Project p4 = createProject("P4", expectedOwner);
        Project p5 = createProject("P5", expectedOwner);
        Project p6 = createProject("P6", expectedOwner);
        updateProjectRelations(p1, p2, p3, p4, p5, p6);

        deleteProject(p1);

        createTasks(p5);
        getNotifications();
    }

    private void createTasks(Project project) {
        Task t1 = createTask(project, "t1");
        Task t2 = createTask(project, "t2");
        Task t3 = createTask(project, "t3");
    }

    private Task createTask(Project project, String taskName) {
        CreateTaskParams task = new CreateTaskParams(taskName, project.getId(), null, null);
        ResponseEntity<Task> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + TaskController.TASKS_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(task),
                Task.class,
                project.getId());
        Task created = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(taskName, created.getName());
        assertEquals(project.getId(), created.getProjectId());
        return created;
    }

    private void deleteProject(Project p) {
        /**  After deletion
         *
         *  p5
         *   |
         *    -- p6
         */

        ResponseEntity<?> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECT_ROUTE,
                HttpMethod.DELETE,
                null,
                Void.class,
                p.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseEntity<Projects> getResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.GET,
                null,
                Projects.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        List<Project> projects = getResponse.getBody().getOwned();
        assertEquals(1, projects.size());
        assertEquals("P5", projects.get(0).getName());
        assertEquals(1, projects.get(0).getSubProjects().size());
        assertEquals("P6", projects.get(0).getSubProjects().get(0).getName());
    }

    private void answerNotifications() {
        ResponseEntity<Notification[]> notificationsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + NotificationController.NOTIFICATIONS_ROUTE,
                HttpMethod.GET,
                null,
                Notification[].class);
        assertEquals(HttpStatus.OK, notificationsResponse.getStatusCode());
        List<Notification> notifications = Arrays.asList(notificationsResponse.getBody());
        assertEquals(8, notifications.size());
        // reject invitations to join group
        for (int i = 1; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            AnswerNotificationParams answerNotificationParams =
                    new AnswerNotificationParams(Action.DECLINE.getDescription());
            ResponseEntity<?> response = this.restTemplate.exchange(
                    ROOT_URL + randomServerPort + NotificationController.ANSWER_NOTIFICATION_ROUTE,
                    HttpMethod.POST,
                    new HttpEntity<>(answerNotificationParams),
                    Void.class,
                    notification.getId());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(JoinGroupEvent.class.getSimpleName(), notification.getType());
        }
    }

    private void updateProjectRelations(Project p1, Project p2, Project p3, Project p4, Project p5, Project p6) {
        /**
         *  p1
         *   |
         *    -- p2
         *   |   |
         *   |    -- p3
         *   |
         *    -- p4
         *
         *  p5
         *   |
         *    -- p6
         */
        List<Project> projectRelations = ProjectRelationsProcessorTest.createSampleProjectRelations(
                p1, p2, p3, p4, p5, p6);
        // Set user's project relations
        ResponseEntity<?> updateProjectRelationsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.PUT,
                new HttpEntity<>(projectRelations),
                Void.class
        );
        assertEquals(HttpStatus.OK, updateProjectRelationsResponse.getStatusCode());

        ResponseEntity<Projects> projectsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.GET,
                null,
                Projects.class);
        assertEquals(HttpStatus.OK, projectsResponse.getStatusCode());
        List<Project> projects = projectsResponse.getBody().getOwned();
        assertEquals(2, projects.size());
        assertEquals(p1, projects.get(0));
        assertEquals(p5, projects.get(1));
        assertEquals(2, projects.get(0).getSubProjects().size());
        assertEquals(p2, projects.get(0).getSubProjects().get(0));
        assertEquals(p4, projects.get(0).getSubProjects().get(1));
        assertEquals(1, projects.get(1).getSubProjects().size());
        assertEquals(p6, projects.get(1).getSubProjects().get(0));
        assertEquals(1, projects.get(0).getSubProjects().get(0).getSubProjects().size());
        assertEquals(p3, projects.get(0).getSubProjects().get(0).getSubProjects().get(0));

        List<ProjectsWithOwner> l = projectsResponse.getBody().getShared();
        assertEquals(2, l.size());
        projects = l.get(0).getProjects();
        assertEquals("Scarlet", l.get(0).getOwner());
        assertEquals(2, projects.size());
        assertEquals("P1", projects.get(0).getName());
        assertEquals("P5", projects.get(1).getName());
        assertEquals(2, projects.get(0).getSubProjects().size());
        assertEquals("P2", projects.get(0).getSubProjects().get(0).getName());
        assertEquals("P4", projects.get(0).getSubProjects().get(1).getName());
        assertEquals(1, projects.get(1).getSubProjects().size());
        assertEquals("P6", projects.get(1).getSubProjects().get(0).getName());
        assertEquals(1, projects.get(0).getSubProjects().get(0).getSubProjects().size());
        assertEquals("P3", projects.get(0).getSubProjects().get(0).getSubProjects().get(0).getName());

        projects = l.get(1).getProjects();
        assertEquals("lsx9981", l.get(1).getOwner());
        assertEquals(1, projects.size());
        assertEquals("P1", projects.get(0).getName());

        // change order of shared projects
        UpdateSharedProjectsOrderParams updateSharedProjectsOrderParams =
                new UpdateSharedProjectsOrderParams(new String[]{"lsx9981", "Scarlet"});
        ResponseEntity<?> updateSharedProjectsOrderResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.UPDATE_SHARED_PROJECTS_ORDER_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(updateSharedProjectsOrderParams),
                Void.class);
        assertEquals(HttpStatus.OK, updateSharedProjectsOrderResponse.getStatusCode());
        projectsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.GET,
                null,
                Projects.class);
        assertEquals(HttpStatus.OK, projectsResponse.getStatusCode());
        l = projectsResponse.getBody().getShared();
        assertEquals(2, l.size());
        projects = l.get(0).getProjects();
        assertEquals("lsx9981", l.get(0).getOwner());
        assertEquals(1, projects.size());

        projects = l.get(1).getProjects();
        assertEquals("Scarlet", l.get(1).getOwner());
        assertEquals(2, projects.size());

        testProjectsEtag(projectsResponse.getHeaders().getETag(), p1);
    }

    private void testProjectsEtag(String eTagFromFirstResponse, Project p1) {
        validateProjectResponseEtagMatch(eTagFromFirstResponse);

        String eTagFromSecondResponse = getEtagAfterUpdateProject(p1);
        validateProjectResponseEtagNotMatch(eTagFromFirstResponse);
        validateProjectResponseEtagMatch(eTagFromSecondResponse);
    }

    private String getEtagAfterUpdateProject(Project p) {
        // update project name to "P11"
        UpdateProjectParams updateProjectParams = new UpdateProjectParams();
        updateProjectParams.setName("P11");
        updateProjectParams.setDescription("ddddd");
        ResponseEntity<Project> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECT_ROUTE,
                HttpMethod.PATCH,
                new HttpEntity<>(updateProjectParams),
                Project.class,
                p.getId());
        p = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("P11", p.getName());
        assertEquals(expectedOwner, p.getOwner());
        assertEquals(ProjectType.LEDGER, p.getProjectType());
        assertEquals(com.bulletjournal.repository.models.Group.DEFAULT_NAME, p.getGroup().getName());
        assertEquals(expectedOwner, p.getGroup().getOwner());

        ResponseEntity<Projects> projectsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.GET,
                null,
                Projects.class);
        return projectsResponse.getHeaders().getETag();
    }

    private void validateProjectResponseEtagMatch(String matchETag) {
        HttpHeaders eTagRequestHeader = new HttpHeaders();
        eTagRequestHeader.setIfNoneMatch(matchETag);
        HttpEntity eTagRequestEntity = new HttpEntity(eTagRequestHeader);

        ResponseEntity<Projects> projectsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.GET,
                eTagRequestEntity,
                Projects.class);

        assertEquals(HttpStatus.NOT_MODIFIED, projectsResponse.getStatusCode());
        assertNull(projectsResponse.getBody());
    }

    private void validateProjectResponseEtagNotMatch(String notMatchEtag) {
        HttpHeaders eTagRequestHeader = new HttpHeaders();
        eTagRequestHeader.setIfNoneMatch(notMatchEtag);
        HttpEntity eTagRequestEntity = new HttpEntity(eTagRequestHeader);

        ResponseEntity<Projects> projectsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.GET,
                eTagRequestEntity,
                Projects.class);

        assertNotEquals(HttpStatus.NOT_MODIFIED, projectsResponse.getStatusCode());
        assertEquals(HttpStatus.OK, projectsResponse.getStatusCode());
        assertNotNull(projectsResponse.getBody());
    }

    private Project updateProject(Project p1) {
        // update project name from "P0" to "P1"
        String projectNewName = "P1";
        UpdateProjectParams updateProjectParams = new UpdateProjectParams();
        updateProjectParams.setName(projectNewName);
        updateProjectParams.setDescription("d2");
        ResponseEntity<Project> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECT_ROUTE,
                HttpMethod.PATCH,
                new HttpEntity<>(updateProjectParams),
                Project.class,
                p1.getId());
        p1 = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(projectNewName, p1.getName());
        assertEquals(expectedOwner, p1.getOwner());
        assertEquals(ProjectType.LEDGER, p1.getProjectType());
        assertEquals(com.bulletjournal.repository.models.Group.DEFAULT_NAME, p1.getGroup().getName());
        assertEquals(expectedOwner, p1.getGroup().getOwner());
        assertEquals("d2", p1.getDescription());
        return p1;
    }

    private void getNotifications() {
        ResponseEntity<Notification[]> notificationsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + NotificationController.NOTIFICATIONS_ROUTE,
                HttpMethod.GET,
                null,
                Notification[].class);
        assertEquals(HttpStatus.OK, notificationsResponse.getStatusCode());

        List<Notification> notifications = Arrays.asList(notificationsResponse.getBody());
        assertEquals(8, notifications.size());
        Notification notification = notifications.get(0);
        assertEquals("Xavier invited you to join Group Default", notification.getTitle());
        assertNull(notification.getContent());
        assertEquals("Xavier", notification.getOriginator().getName());
        assertEquals(ImmutableList.of(Action.ACCEPT.getDescription(), Action.DECLINE.getDescription()),
                notification.getActions());
        assertEquals(JoinGroupEvent.class.getSimpleName(), notification.getType());

        for (int i = 1; i < 8; i++) {
            assertTrue(notifications.get(i).getTitle()
                    .endsWith("declined your invitation to join Group Default"));
            assertEquals(JoinGroupResponseEvent.class.getSimpleName(), notifications.get(i).getType());
        }
    }

    private List<GroupsWithOwner> getGroups(List<GroupsWithOwner> expected) {
        ResponseEntity<GroupsWithOwner[]> groupsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.GROUPS_ROUTE,
                HttpMethod.GET,
                null,
                GroupsWithOwner[].class);
        List<GroupsWithOwner> groupsBody = Arrays.asList(groupsResponse.getBody());
        if (expected != null) {
            assertEquals(expected.size(), groupsBody.size());
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i), groupsBody.get(i));
            }
        }
        return groupsBody;
    }

    private List<GroupsWithOwner> createGroups(String owner) {
        List<GroupsWithOwner> groups = getGroups(null);
        assertEquals(4, groups.size());
        Group g = groups.get(0).getGroups().get(0);
        assertEquals(expectedOwner, g.getOwner());
        assertEquals(1, g.getUsers().size());
        Group invitedToJoin = groups.get(2).getGroups().get(0);
        assertEquals(2, invitedToJoin.getUsers().size());
        assertEquals("Xavier", invitedToJoin.getOwner());
        assertEquals("Xavier", invitedToJoin.getUsers().get(0).getName());
        assertEquals(true, invitedToJoin.getUsers().get(0).isAccepted());
        assertEquals(expectedOwner, invitedToJoin.getUsers().get(1).getName());
        assertEquals(false, invitedToJoin.getUsers().get(1).isAccepted());
        Group joinedGroup = groups.get(1).getGroups().get(0);
        assertEquals(2, joinedGroup.getUsers().size());
        assertEquals("Scarlet", joinedGroup.getOwner());
        assertEquals("Scarlet", joinedGroup.getUsers().get(0).getName());
        assertEquals(true, joinedGroup.getUsers().get(0).isAccepted());
        assertEquals(expectedOwner, joinedGroup.getUsers().get(1).getName());
        assertEquals(true, joinedGroup.getUsers().get(1).isAccepted());
        Group joinedGroup2 = groups.get(3).getGroups().get(0);
        Group g1 = createGroup("G0", owner);
        Group g2 = createGroup("G2", owner);
        Group g3 = createGroup("G3", owner);
        getGroups(ImmutableList.of(
                new GroupsWithOwner(expectedOwner, ImmutableList.of(g, g1, g2, g3)),
                new GroupsWithOwner("Scarlet", ImmutableList.of(joinedGroup)),
                new GroupsWithOwner("Xavier", ImmutableList.of(invitedToJoin)),
                new GroupsWithOwner("lsx9981", ImmutableList.of(joinedGroup2))));

        String groupNewName = "G1";
        UpdateGroupParams updateGroupParams = new UpdateGroupParams();
        updateGroupParams.setName(groupNewName);

        // Update group name from "G0" to "G1"
        ResponseEntity<Group> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.GROUP_ROUTE,
                HttpMethod.PATCH,
                new HttpEntity<>(updateGroupParams),
                Group.class,
                g1.getId());
        g1 = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupNewName, g1.getName());

        // Delete Group "G3"
        ResponseEntity<?> deleteResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.GROUP_ROUTE,
                HttpMethod.DELETE,
                null,
                Void.class,
                g3.getId());
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        groups = getGroups(ImmutableList.of(
                new GroupsWithOwner(expectedOwner, ImmutableList.of(g, g1, g2)),
                new GroupsWithOwner("Scarlet", ImmutableList.of(joinedGroup)),
                new GroupsWithOwner("Xavier", ImmutableList.of(invitedToJoin)),
                new GroupsWithOwner("lsx9981", ImmutableList.of(joinedGroup2))));

        // Delete Group "Default"
        deleteResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.GROUP_ROUTE,
                HttpMethod.DELETE,
                null,
                Void.class,
                g.getId());
        assertEquals(HttpStatus.UNAUTHORIZED, deleteResponse.getStatusCode());
        groups = getGroups(ImmutableList.of(
                new GroupsWithOwner(expectedOwner, ImmutableList.of(g, g1, g2)),
                new GroupsWithOwner("Scarlet", ImmutableList.of(joinedGroup)),
                new GroupsWithOwner("Xavier", ImmutableList.of(invitedToJoin)),
                new GroupsWithOwner("lsx9981", ImmutableList.of(joinedGroup2))));
        return groups;
    }

    private List<GroupsWithOwner> addUsersToGroup(final Group group, List<String> usernames) {
        AddUserGroupsParams addUserGroupsParams = new AddUserGroupsParams();
        for (String username : usernames) {
            addUserGroupsParams.getUserGroups().add(new AddUserGroupParams(group.getId(), username));
        }
        ResponseEntity<GroupsWithOwner[]> groupsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.ADD_USER_GROUPS_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(addUserGroupsParams),
                GroupsWithOwner[].class);
        List<GroupsWithOwner> groups = Arrays.asList(groupsResponse.getBody());
        Group updated = groups.stream().filter(g -> group.getOwner().equals(g.getOwner()))
                .findFirst().get().getGroups()
                .stream().filter(g -> group.getName().equals(g.getName())).findFirst().get();
        assertEquals(usernames.size() + 1, updated.getUsers().size());
        return groups;
    }

    private Group addUserToGroup(Group group, String username, int expectedSize) {
        AddUserGroupParams addUserGroupParams = new AddUserGroupParams(group.getId(), username);
        ResponseEntity<Group> groupsResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.ADD_USER_GROUP_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(addUserGroupParams),
                Group.class);
        Group updated = groupsResponse.getBody();
        assertEquals(expectedSize, updated.getUsers().size());
        return updated;
    }

    private Group createGroup(String groupName, String expectedOwner) {
        CreateGroupParams group = new CreateGroupParams(groupName);
        ResponseEntity<Group> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.GROUPS_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(group),
                Group.class);
        Group created = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(groupName, created.getName());
        assertEquals(expectedOwner, created.getOwner());

        return created;
    }

    private Project createProject(String projectName, String expectedOwner) {
        CreateProjectParams project = new CreateProjectParams(projectName, ProjectType.LEDGER, "d1");
        ResponseEntity<Project> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(project),
                Project.class);
        Project created = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(projectName, created.getName());
        assertEquals(expectedOwner, created.getOwner());
        assertEquals(ProjectType.LEDGER, created.getProjectType());
        assertEquals(com.bulletjournal.repository.models.Group.DEFAULT_NAME, created.getGroup().getName());
        assertEquals(expectedOwner, created.getGroup().getOwner());
        assertEquals("d1", created.getDescription());
        return created;
    }
}

