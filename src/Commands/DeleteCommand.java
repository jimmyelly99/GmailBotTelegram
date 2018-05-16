package Commands;

import Dao.UserDao;
import Dao.UserFileDao;
import Services.MailService;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;

import javax.mail.MessagingException;
import java.io.IOException;

public class DeleteCommand extends BotCommand {
    public DeleteCommand() {
        super("delete", "deletes all unread messages");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        UserDao userDao = new UserFileDao();
        Dto.User userMail = userDao.getUser(user.getUserName());

        try {
            MailService mailService = new MailService(userMail);
            mailService.deleteUnreadEmails();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
