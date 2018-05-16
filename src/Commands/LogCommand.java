package Commands;

import Dao.UserDao;
import Dao.UserFileDao;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class LogCommand extends BotCommand {
    public LogCommand() {
        super("log", "set's up email config");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            UserDao userDao = new UserFileDao();
            Dto.User userMail = new Dto.User(user.getUserName(), strings[0], strings[1]);
            userDao.addUser(userMail);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat.getId());
            sendMessage.setText("Nice account, " + strings[0] + "!");

            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
