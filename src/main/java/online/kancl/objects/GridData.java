package online.kancl.objects;

import java.util.ArrayList;
import java.util.List;

public class GridData {
    List<User> users;

    public GridData() {
        this.users = new ArrayList<>();
    }

    public void addUser(User user){
        users.add(user);
    }

    public void removeUser(String username){
        users.removeIf(user -> user.username.equals(username));
    }

}
