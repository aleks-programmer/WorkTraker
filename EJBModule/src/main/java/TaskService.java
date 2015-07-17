import org.apache.log4j.Logger;
import DAO.QueryConstants;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Login Session Bean
 */

@Stateless
@LocalBean
@ApplicationPath("/taskService")
@Path("service")
public class TaskService extends Application implements QueryConstants {
    private static final Logger LOG =  Logger.getLogger(TaskService.class);
    @PersistenceContext(unitName = "JPAUSERS")
    private EntityManager entityManager;

    private User getUser(int id) {
        return entityManager.find(User.class, id);
    }


    @GET
    @Path("user")
    public UserDetails getUserByName(@QueryParam("userName") String userName) {
        User user = null;
        UserDetails user1 = new UserDetails();
        try {
            Query query = entityManager.createQuery(GET_USER_BY_NAME);
            query.setParameter(1, userName);
            user = (User) query.getSingleResult();

            user1.setUserID(user.getUserID());
            user1.setUserName(user.getUserName());
        } catch(NoResultException nre) {
            return null;
        }
        return user1;
    }

    @POST
    @Path("user")
    public UserDetails storeUser(UserDetails user1) {
        User user = new User();
        user.setUserName(user1.getUserName());
        entityManager.persist(user);
        user1.setUserID(user.getUserID());
        return user1;
    }

    @GET
    @Path("user/{userId}")
    public UserDetails getUser(@PathParam("userId")
                               Integer userID) {
        User user = entityManager.find(User.class, userID);
        UserDetails user1 = new UserDetails();
        user1.setUserID(user.getUserID());
        user1.setUserName(user.getUserName());
        return user1;
    }
    @GET
    @Path("task/{userTaskId}")
    public Task getTask(@PathParam("userTaskId")
                        Integer userTaskId) {
        return entityManager.find(Task.class, userTaskId);
    }

    @GET
    @Path("user/{userId}/task/{taskId}")
    public WorkDetails getWork(
            @PathParam("userId")
            Integer userId,
            @PathParam("taskId")
            Integer taskID) {
        WorkDetails work = new WorkDetails();
        User user = entityManager.find(User.class, userId);
        Task task = entityManager.find(Task.class, taskID);
        if(user == null)
            throw new NullPointerException("User with ID: " + userId + " is unexpectively undefined");
        if(task == null)
            throw new NullPointerException("Task with ID: " + taskID + " is unexpectively undefined");

        Set<Work> workSet =  user.getWorks();

        for (Work work1 : workSet)
            if (work1.getTask().equals(task)) {
                work.setTaskID(work1.getTask().getTaskID());
                work.setUserID(work1.getUser().getUserID());
                work.setWorkStatus(work1.getStatus());
                work.setWorkTime(work1.getWorkTime());
                return work;
            }
        return null;
    }

    @GET
    @Path("task")
    public Task getTask(
            @QueryParam("taskName")
            String taskName) {
        Task task = null;
        try {
            Query query = entityManager.createQuery(GET_TASK_BY_NAME);
            query.setParameter(1, taskName);
            task = (Task) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        return task;
    }

    @POST
    @Path("task")
    public Task storeTask(Task task1) {
        Task task = new Task();
        task.setTaskName(task1.getTaskName());
        task.setTaskDescription(task1.getTaskName());
        entityManager.persist(task);
        return task;
    }

    @POST
    @Path("user/task")
    public WorkDetails storeWork(WorkDetails work1) {
        User user = entityManager.find(User.class, work1.getUserID());
        Task task = entityManager.find(Task.class, work1.getTaskID());
        if(user == null)
            throw new NullPointerException("User with ID: " + work1.getUserID() + " is unexpectively undefined");
        if(task == null)
            throw new NullPointerException("Task with ID: " + work1.getTaskID() + " is unexpectively undefined");
        Work userWork = new Work();
        userWork.setUser(user);
        userWork.setTask(task);
        userWork.setStatus(work1.getWorkStatus());
        userWork.setWorkTime(work1.getWorkTime());
        user.addWork(userWork);
        entityManager.merge(user);
        entityManager.flush();
        return work1;
    }

    @GET
    @Path("user/task")
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedListWrapper<WorkDetailsView> getWorkList(@DefaultValue("1")
                                                             @QueryParam("page")
                                                             Integer page,
                                                             @DefaultValue("userName")
                                                             @QueryParam("sortFields")
                                                             String sortFields,
                                                             @DefaultValue("asc")
                                                             @QueryParam("sortDirections")
                                                             String sortDirections) {
        PaginatedListWrapper<WorkDetailsView> paginatedListWrapper = new PaginatedListWrapper<WorkDetailsView>();
        paginatedListWrapper.setCurrentPage(page);
        paginatedListWrapper.setSortFields(sortFields);
        paginatedListWrapper.setSortDirections(sortDirections);
        paginatedListWrapper.setPageSize(10);
        return findWorks(paginatedListWrapper);
    }

    private String modifySortField(String sortFieldsStr) {
        StringBuilder sb = new StringBuilder();
        String[] sortFields = sortFieldsStr.split(",");
        String delim = "";
        for (String sortField : sortFields) {
            switch (sortField) {
                case "userName":
                    sb.append(delim).append("user.userName");
                    break;
                case "taskName":
                    sb.append(delim).append("task.taskName");
                    break;
                case "workTime":
                    sb.append(delim).append("work.workTime");
                    break;
                case "workStatus":
                    sb.append(delim).append("work.status");
                    break;
                default:
                    break;
            }
            delim = ",";
        }
        return sb.toString();
    }

    private List<WorkDetailsView> findWorks(int startPosition, int maxResults, String sortFields, String sortDirections) {
        List<WorkDetailsView> lWorks= new ArrayList<WorkDetailsView>();
        String finalSortField = modifySortField(sortFields);
        String finalOrderBy = String.format(ORDER_BY, finalSortField, sortDirections);

        Query query = entityManager.createQuery(GET_WORKS2 + finalOrderBy);
        query.setFirstResult(startPosition)
                .setMaxResults(maxResults);
        List list = query.getResultList();
        for(Object l : list) {
            Object[] o = (Object[]) l;
            WorkDetailsView work = new WorkDetailsView();
            work.setUserName((String) o[0]);
            work.setTaskName((String) o[1]);
            work.setWorkTime((Integer) o[2]);
            work.setWorkStatus((String) o[3]);
            lWorks.add(work);
        }
        return lWorks;
    }

    private PaginatedListWrapper<WorkDetailsView> findWorks(PaginatedListWrapper<WorkDetailsView> paginatedList) {
        paginatedList.setTotalResults(countWorks());
        int start = (paginatedList.getCurrentPage() - 1) *  paginatedList.getPageSize();
        paginatedList.setList(findWorks(start,
                paginatedList.getPageSize(),
                paginatedList.getSortFields(),
                paginatedList.getSortDirections()));
        return paginatedList;
    }

    private Long countWorks() {
        Query query = entityManager.createQuery(GET_NUMBER_OF_WORKS);
        return ((Long) query.getSingleResult()).longValue();
    }





}
