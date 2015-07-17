import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity User
 */
@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "USER_NAME")})
public class User implements Serializable {


    private Integer userID;

    private String userName;




    private Set<Work> works = new HashSet<Work>();

    public User() {
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", unique = true, nullable = false)
    public Integer getUserID() {
        return this.userID;
    }
    public void setUserID(Integer id) {
        this.userID = id;
    }
    @Column(name = "USER_NAME", unique = true, nullable = false, length = 200)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public void addWork(Work work) {
        works.add(work);
    }

    public void updateWork(Work work) {
        works.add(work);
    }
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.user", cascade=CascadeType.ALL)
    public Set<Work> getWorks() {
        return this.works;
    }

    public void setWorks(Set<Work> map) {
        this.works = map;
    }

}
