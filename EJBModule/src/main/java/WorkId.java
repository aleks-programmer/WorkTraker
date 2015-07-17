import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 * Primary Key For Work
 */

@Embeddable
public class WorkId implements Serializable{

    private User user;

    private Task task;

    @ManyToOne
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    @OneToOne
    @JoinColumn(name = "TASK_ID")
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
