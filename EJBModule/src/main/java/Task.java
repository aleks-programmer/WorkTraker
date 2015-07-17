import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity Task
 */

@Entity
@Table(name = "task", uniqueConstraints = {
        @UniqueConstraint(columnNames = "TASK_NAME")})
public class Task implements Serializable {


    private Integer taskID;

    private String taskName;

    private String taskDescription;


    //private Work work = new Work();

    public Task() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TASK_ID", unique = true, nullable = false)
    public Integer getTaskID() {
        return this.taskID;
    }
    public void setTaskID(Integer id) {
        this.taskID = id;
    }
    @Column(name = "TASK_NAME", unique = true, nullable = false, length = 200)
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String name) {
        this.taskName = name;
    }


    @Column(name = "TASK_DESCRIPTION", nullable = false)
    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String description) {
        this.taskDescription = description;
    }

    /*@OneToOne(fetch = FetchType.LAZY, mappedBy = "pk.task", cascade=CascadeType.ALL)
    public Work getWork() {
        return this.work;
    }
    public void setWork( Work w) {
        this.work = w;
    }*/

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if(!taskID.equals(task.getTaskID())) return false;
        if(!taskName.equals(task.getTaskName())) return false;

        return true;
    }
}
