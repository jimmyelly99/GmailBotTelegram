package Dao;

import Config.BotConstants;
import Dto.User;

import java.io.*;

public class UserFileDao implements UserDao {
    @Override
    public void addUser(User user) {
        try {
            removeUserCsv(user.getUsername());
            writeUserCsv(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getUser(String username) {
        try {
            return findUserCsv(username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUser(String username) {
        try {
            removeUserCsv(username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private User findUserCsv(String username) throws IOException {
        User user = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(BotConstants.DATA_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (findUser(username, line)) {
                    String[] args = line.split("-")[1].split(";");
                    user = new User(username, args[0], args[1]);
                    break;
                }
            }
        }
        return user;
    }

    private boolean findUser(String userName, String line) {
        String[] args = line.split("-");
        return args[0].equals(userName);
    }

    private void removeUserCsv(String username) throws IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(BotConstants.DATA_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!findUser(username, line))
                    data.append(line);
            }
        }

        File file = new File(BotConstants.DATA_PATH);
        if (file.exists())
            file.delete();
        file.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            //format: email-login;password
            writer.write(data.toString());
        }
    }

    private void writeUserCsv(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BotConstants.DATA_PATH, true))) {
            //format: email-login;password
            writer.write(user.getUsername() + "-" + user.getEmail() + ";" + user.getPassword());
        }
    }
}
