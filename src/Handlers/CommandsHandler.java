package Handlers;

import Commands.*;
import Config.BotConstants;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class CommandsHandler extends TelegramLongPollingCommandBot {
    public CommandsHandler() {
        super(BotConstants.BOT_USERNAME);
        registerAll(new LogCommand(), new GetCommand(), new SendCommand(), new EndCommand(), new StartCommand(), new EndCommand(), new DeleteCommand());

        registerDefaultAction(((absSender, message) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setText("unknown command");

            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }));

    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("require only commands");

            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotToken() {
        return BotConstants.TOKEN;
    }
}
