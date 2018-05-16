package Services;

import Dto.User;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MailService {
    private static String propertiesPath = "mail.properties";
    private Session session;
    private User user;
    private Map<String, InputStream> files = new HashMap<>();
    private StringBuilder text = new StringBuilder();

    public MailService(User user) throws IOException {
        this.user = user;
        Properties properties = new Properties();
        properties.load(MailService.class.getClassLoader().getResourceAsStream(propertiesPath));
        connectSession(user, properties);
    }

    private void connectSession(User user, Properties properties) {
        session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user.getEmail(), user.getPassword());
            }
        });
    }

    public Message[] getEmails() throws MessagingException {
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", user.getEmail(), user.getPassword());

        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        if (inbox.getUnreadMessageCount() > 50)
            return null;

        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        //loads all messages
        //Arrays.sort(messages, getMessageReceivedDateComparator());
        return messages;
    }

    private Comparator<Message> getMessageReceivedDateComparator() {
        return (o1, o2) -> {
            try {
                return o2.getReceivedDate().compareTo(o1.getReceivedDate());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void parseContent(Part mess) throws MessagingException, IOException {
        text = new StringBuilder();
        files = new HashMap<>();
        parseContentPart(mess);
    }

    private void parseContentPart(Part mess) throws MessagingException, IOException {
        if (mess.isMimeType("text/plain")) {
            text.append((String) mess.getContent());
        } else if (mess.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) mess.getContent();
            for (int i = 0; i < multipart.getCount(); i++)
                parseContentPart(multipart.getBodyPart(i));

        } else if (mess.isMimeType("message/rfc822")) {
            parseContentPart((Part) mess.getContent());

        } else if (mess.getContentType().toLowerCase().contains("image/") || mess.getContentType().toLowerCase().contains("application/")) {
            files.put(mess.getFileName(), mess.getInputStream());
        }
    }

    public void sendEmail(Address[] to, Address from, String[] text) throws MessagingException {
        Message message = new MimeMessage(session);
        Transport transport = session.getTransport();

        message.setFrom(from);
        message.setRecipients(Message.RecipientType.BCC, to);
        StringBuilder result = new StringBuilder();
        for (String s : text) {
            result.append(s);
        }
        message.setText(result.toString());

        transport.send(message);
    }

    public void sendEmailWithFile() {
    }

    public Map<String, InputStream> getFiles() {
        return new HashMap<>(files);
    }

    public String getText() {
        return text.toString();
    }

    public void deleteUnreadEmails() throws MessagingException {
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", user.getEmail(), user.getPassword());

        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        for (Message message : messages) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        inbox.close(true);
        store.close();
    }
}
