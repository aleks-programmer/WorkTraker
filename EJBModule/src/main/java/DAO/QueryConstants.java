package DAO;

/**
 * Created by User on 03.07.2015.
 */
public interface QueryConstants {
    public static final String GET_WORKS2 = "SELECT user.userName AS USER_NAME, \n" +
            "task.taskName AS TASK_NAME, \n" +
            "work.workTime AS WORK_TIME, \n" +
            "work.status AS WORK_STATUS \n" +
            "FROM User as user join user.works as work join work.pk as pk join pk.task as task";
    public static final String ORDER_BY = " ORDER BY %s %s";
    public static final String GET_USER_BY_NAME = "SELECT u FROM User u WHERE u.userName = ?1";
    public static final String GET_TASK_BY_NAME = "SELECT t FROM Task t WHERE t.taskName = ?1";
    public static final String GET_NUMBER_OF_WORKS = "SELECT COUNT(*) FROM Work w";
}
