package Dao;

import Dto.User;

public interface UserDao {
    void addUser(User user);
    User getUser(String username);
    void removeUser(String username);

}
