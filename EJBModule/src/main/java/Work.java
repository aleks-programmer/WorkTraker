import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Work On User Task
 */

@Entity
@Table(name = "work")
@AssociationOverrides({
        @AssociationOverride(name = "pk.user",
                joinColumns = @JoinColumn(name = "USER_ID")),
        @AssociationOverride(name = "pk.task",
                joinColumns = @JoinColumn(name = "TASK_ID")) })
public class Work implements Serializable {


    WorkId pk =  new WorkId();

    private Integer workTime;


    private String status;

    public Work() {
    }

    @EmbeddedId
    public WorkId getPk() {
        return pk;
    }

    public void setPk(WorkId pk) {
        this.pk = pk;
    }

    @Transient
    public User getUser() {
        return getPk().getUser();
    }

    public void setUser(User user) {
        getPk().setUser(user);
    }

    @Transient
    public Task getTask() {
        return getPk().getTask();
    }

    public void setTask(Task task) {
        getPk().setTask(task);
    }

    @Column(name = "WORKTIME", nullable = false, length = 11)
    public Integer getWorkTime() {
        return this.workTime;
    }

    public void setWorkTime(Integer workTime) {
        this.workTime = workTime;
    }

    @Column(name = "STATUS", nullable = false, length = 200)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Work work = (Work) o;
        if(Objects.equals(getPk().getUser().getUserID(), work.getPk().getUser().getUserID())
                && Objects.equals(getPk().getTask().getTaskID(), work.getPk().getTask().getTaskID())) return true;

        return false;
    }
}
