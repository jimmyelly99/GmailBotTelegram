package Commands;

import Dao.UserDao;
import Dao.UserFileDao;
import Services.MailService;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.util.*;

public class GetCommand extends BotCommand {
    public GetCommand() {
        super("get", "get unread emails");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            UserDao userDao = new UserFileDao();
            Dto.User userMail = userDao.getUser(user.getUserName());
            if (userMail == null)
                throw new IllegalArgumentException("Unlogged user");
            operateEmails(userMail, chat, absSender);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //todo operate exceptions by sending message
    }

    private void operateEmails(Dto.User userMail, Chat chat, AbsSender absSender) throws IOException, TelegramApiException, MessagingException, InterruptedException {
        MailService mailService = new MailService(userMail);
        javax.mail.Message[] emails = mailService.getEmails();

        if (emails == null || emails.length == 0) {
            sendTextMessage(absSender, chat.getId(), "No new emails / too many emails");
            return;
        }

        for (javax.mail.Message email : emails) {
            mailService.parseContent(email);

            StringBuilder text = createMessageHeader(email);
            text.append(mailService.getText());
            try {
                sendTextMessage(absSender, chat.getId(), text.toString());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            Map<String, InputStream> files = mailService.getFiles();
                if (files.size() > 0) {
                    for (Map.Entry<String, InputStream> file : files.entrySet()) {
                        try {
                            sendDocMessage(absSender, chat.getId(), file.getValue(), file.getKey());
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

        }
    }

    private void sendDocMessage(AbsSender sender, long chatId, InputStream stream, String docName) throws TelegramApiException, IOException {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setNewDocument(docName, stream);
        sendDocument.validate();
        sender.sendDocument(sendDocument);
        stream.close();
    }

    private void sendTextMessage(AbsSender sender, long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText( validateText(text));
        sendMessage.validate();
        sender.execute(sendMessage);
    }

    private StringBuilder createMessageHeader(Message email) throws MessagingException {
        String from = ((InternetAddress) email.getFrom()[0]).getAddress();
        StringBuilder text = new StringBuilder("From: ").append(from).append("\n");
        text.append("Theme: ").append((email.getSubject() != null ? email.getSubject() : "")).append("\n").append("------------------------\n");
        return text;
    }

    private String validateText(String text) {
        List<String> split = new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        String result = split.stream()
                .filter((line) -> line.trim() != "" && line.trim() != " ")
                .map((line) -> line += "\n")
                .collect(() -> new StringBuilder(), (res, line) -> res.append(line), (res, line) -> res.append(line))
                .toString();
        return result;
    }

}
