package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "AcidKoss_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7582583026:AAHObaQmUxb7E-cYYL66zvn0a-axs0smpfk"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "javcgknGzsQ2Hpv/Ox5/mEBt7jGZ9odEzk0d18IOaiI7kZO0GoWcatc1JMWuW+cZMw7KNEcSPPfJGOtkB5xmhKKVSHR7Oa/004F9C2eDQw2QnGq5nHX9QtYk4Ge9k+VPqgm+21HRqWwHfGpvNNoj5ZtytYrKXB3jv6MxFNeVNSkI6UbY1JO0hn1U7vIG45MSb0UpjfFsf/nG/M1IpLb2b5OlGC0UgMzeSRSpup+dqCs8wWidM="; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList <String> list = new ArrayList<>();
    private UserInfo me;
    private UserInfo she;
    private int questionCount;


    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }


    @Override
    public void onUpdateEventReceived(Update update) {

        sendWelcomeMessage();

        String massage = getMessageText();

        if(massage.equals("/start")){
            currentMode = DialogMode.MAIN;
            String text = loadMessage("main");
            sendPhotoTextMessage("main",text);
            showMainMenu("главное меню бота","/start",
                    "генерация Tinder-профля \uD83D\uDE0E","/profile",
                    "сообщение для знакомства \uD83E\uDD70","/opener",
                    "переписка от вашего имени \uD83D\uDE08","/message",
                    "переписка со звездами \uD83D\uDD25","/date",
                    "задать вопрос чату GPT \uD83E\uDDE0","/gpt"
                    );
            return;
        }

        if (massage.equals("/profile")){
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько лет?");
            return;
        }

        if (massage.equals("/opener")){
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");
            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя собеседника?");
            return;
        }

        if (massage.equals("/message")){
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение","message_next",
                    "Пригласить на свидание","message_date");
            return;
        }

        if (massage.equals("/date")){
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана","date_grande",
                    "Марго","date_robbie",
                    "Зендея","date_zendaya",
                    "Райн","date_gosling",
                    "Том","date_hardy");
            return;
        }

        if (massage.equals("/gpt")){
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage("Напишите свое сообщение:");
            return;
        }

        if(currentMode ==DialogMode.PROFILE){
            switch ((questionCount)){
                case 1:
                    me.age = massage;
                    questionCount = 2;
                    sendTextMessage("Кем работаете?");
                    return;
                case 2:
                    me.occupation = massage;
                    questionCount = 3;
                    sendTextMessage("Какое хобби?");
                    return;
                case 3:
                    me.hobby = massage;
                    questionCount = 4;
                    sendTextMessage("Что не нравится в людях?");
                    return;
                case 4:
                    me.annoys = massage;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = massage;
                   String aboutMaself = me.toString();
                   String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите ...");
                    String answer = chatGPT.sendMessage(prompt,aboutMaself);
                    updateTextMessage(msg,answer);
                    return;
            }
        }



        if(currentMode ==DialogMode.OPENER){
            switch ((questionCount)){
                case 1:
                    she.name = massage;
                    questionCount = 2;
                    sendTextMessage("Сколько лет?");
                    return;
                case 2:
                    she.age = massage;
                    questionCount = 3;
                    sendTextMessage("Какое хобби?");
                    return;
                case 3:
                    she.hobby = massage;
                    questionCount = 4;
                    sendTextMessage("Кем работает?");
                    return;
                case 4:
                    she.occupation = massage;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = massage;
                    String aboutFriend = she.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите ...");
                    String answer = chatGPT.sendMessage(prompt,aboutFriend);
                    updateTextMessage(msg,answer);
                    return;
            }
        }

        if(currentMode ==DialogMode.MESSAGE){

            String query = getCallbackQueryButtonKey();

            if (query.startsWith("message")){
                String prompt = loadPrompt(query);
                String userChatHist = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите ...");
                String answer = chatGPT.sendMessage(prompt,userChatHist);
                updateTextMessage(msg,answer);
                return;
            }

            list.add(massage);
            return;
        }

        if(currentMode ==DialogMode.DATE){

            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date")){
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор!!!");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }

            Message msg = sendTextMessage("Собеседник набирает сообщение ...");
            String answer = chatGPT.addMessage(massage);
            updateTextMessage(msg,answer);
            return;
        }

        if(currentMode ==DialogMode.GPT ){

            Message msg = sendTextMessage("Подождите ...");
            String prompt = loadPrompt("gpt");
            String answer = chatGPT.sendMessage(prompt,massage);
            updateTextMessage(msg,answer);
            return;
        }

    }
    public void sendWelcomeMessage() {
        String htmlText = """
        <b>Привет!</b> 👋\n
        Я твой новый <i>Telegram-бот</i>.\n
        Нажми на кнопку ниже, чтобы узнать больше 🚀\n
        <a href="https://core.telegram.org/bots">Документация Telegram Bot API</a>
        """;

        SendMessage message = new SendMessage();
        message.setChatId(getCurrentChatId());
        message.setParseMode("html"); // включаем HTML
        message.setText(htmlText);

        // Добавим кнопку
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Открыть сайт 🌐");
        button.setUrl("https://google.com");

        keyboard.add(List.of(button));
        markup.setKeyboard(keyboard);

        message.setReplyMarkup(markup);

        executeTelegramApiMethod(message);
    }
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
