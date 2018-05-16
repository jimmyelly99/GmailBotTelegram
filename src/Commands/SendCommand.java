package Commands;

import Dao.UserDao;
import Dao.UserFileDao;
import Services.MailService;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.Arrays;

public class SendCommand extends BotCommand {
    public SendCommand() {
        super("send", "send email");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            UserDao userDao = new UserFileDao();
            Dto.User userMail = userDao.getUser(user.getUserName());

            sendEmail(userMail, strings);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat.getId());
            sendMessage.setText("Email successfully sent");
            absSender.execute(sendMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(Dto.User userMail, String[] strings) throws IOException, MessagingException {
        MailService mailService = new MailService(userMail);
        Address to = new InternetAddress(strings[0]);
        Address from = new InternetAddress(userMail.getEmail());
        mailService.sendEmail(new Address[] {to}, from, Arrays.copyOfRange(strings, 1, strings.length));
    }
}
