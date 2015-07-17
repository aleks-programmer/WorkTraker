import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * User Detals
 */
@XmlRootElement(name = "userDetails")
public class UserDetails implements Serializable {

    @XmlElement
    private Integer userID;
    @XmlElement
    private String userName;

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
