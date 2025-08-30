package com.example.sellify.bot;

import com.example.sellify.constants.Constants;
import com.example.sellify.dto.ProductRequest;
import com.example.sellify.dto.UserRequest;
import com.example.sellify.entity.Photo;
import com.example.sellify.entity.Product;
import com.example.sellify.entity.UserSession;
import com.example.sellify.entity.enums.ProductStep;
import com.example.sellify.entity.enums.Category;
import com.example.sellify.service.ProductService;
import com.example.sellify.service.SessionManager;
import com.example.sellify.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotCode extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;
    @Value("${bot.username}")
    private String username;
    @Value("${bot.channel.id}")
    private String channelId;

    private final UserService userService;
    private final SessionManager sessionManager;
    private final ProductService productService;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() && !update.hasCallbackQuery()) return;
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        String chatId = String.valueOf(update.getMessage().getChatId());
        String text = update.getMessage().hasText() ? update.getMessage().getText() : "";
        String uname = update.getMessage().getChat().getUserName();
        String firstName = update.getMessage().getChat().getFirstName();
        String lastName = update.getMessage().getChat().getLastName();

        if ("/panel".equals(text)) {
            showAdminMenu(chatId);
            return;
        }

        UserSession session = sessionManager.getSession(chatId);
        if (session != null) {
            handleProductFlow(update, chatId, text, session, uname);
            return;
        }

        switch (text) {
            case "/start" -> {
                UserRequest userRequest = UserRequest.builder()
                        .chatId(chatId)
                        .username(uname)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();
                String response = userService.saveUser(userRequest);
                sendMessageWithKeyboard(chatId, "👋 Salom " + firstName + "!\n" + response, Constants.USER_MENU);
            }
            case "Yordam" -> sendMessage(chatId, "🛠️ " + Constants.HELP_MESSAGE);
            case "Elon joylash" -> {
                sendMessage(chatId, "⚠️ Ogohlantirish: E'lonlarda nomaqbul so'zlar va taqiqlangan rasmlardan foydalanish jinoiy javobgarlikka olib keladi.");
                sessionManager.startSession(chatId);
                sendMessage(chatId, "📌 E'lon sarlavhasini kiriting:");
            }
            case "Qidirish by ID" -> sendMessage(chatId, "ID ni kiriting:");
            default -> sendMessage(chatId,"😔Buyruq tanilmadi");
        }

        try {
            Long id = Long.parseLong(text);
            sendAdminProductView(chatId, id);
        } catch (NumberFormatException ignored) {}
    }

    private void handleCallback(Update update) {
        String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        String data = update.getCallbackQuery().getData();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        if (data.startsWith("category:")) {
            UserSession session = sessionManager.getSession(chatId);
            if (session != null) {
                session.getProductRequest().setCategory(data.split(":")[1]);
                session.setStep(ProductStep.PHOTO);
                sendMessage(chatId, "📷 Kamida 2 ta rasm yuboring. Tugatgach **done** deb yozing:");
            }
        } else if (data.startsWith("admin_accept:")) {
            Long id = Long.parseLong(data.split(":")[1]);
            Product p = productService.findById(id);
            if (p != null) sendApprovedPost(p);
            deleteMessage(chatId, messageId);
        } else if (data.startsWith("admin_reject:")) {
            Long id = Long.parseLong(data.split(":")[1]);
            productService.delete(id);
            deleteMessage(chatId, messageId);
        }
    }

    private void handleProductFlow(Update update, String chatId, String text, UserSession session, String uname) {
        ProductRequest request = session.getProductRequest();
        switch (session.getStep()) {
            case TITLE -> {
                request.setTitle(text);
                session.setStep(ProductStep.DESCRIPTION);
                sendMessage(chatId, "📝 E'lon tavsifini kiriting. Iltimos tavsifda kengroq malumot berishga harakat qiling:");
            }
            case DESCRIPTION -> {
                request.setDescription(text);
                session.setStep(ProductStep.PRICE);
                sendMessage(chatId, "💰 Narxni kiriting (faqat raqam):");
            }
            case PRICE -> {
                try {
                    request.setPrice(Double.parseDouble(text));
                    session.setStep(ProductStep.CATEGORY);
                    sendCategoryButtons(chatId);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "❌ Narx faqat raqam bo‘lishi kerak.");
                }
            }
            case PHOTO -> {
                if (update.getMessage().hasPhoto()) {
                    String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId();
                    request.getPhotoInfos().put(fileId, "");
                } else if ("done".equalsIgnoreCase(text)) {
                    if (request.getPhotoInfos().size() < 2) {
                        sendMessage(chatId, "❌ Kamida 2 ta rasm kerak.");
                        return;
                    }
                    session.setStep(ProductStep.CONFIRM);
                    sendCollagePreview(chatId, request, uname);
                    sendMessageWithKeyboard(chatId, "Tasdiqlaysizmi?", Constants.CONFIRM_BUTTON);
                }
            }
            case CONFIRM -> {
                if ("Ha".equals(text)) {
                    sendMessage(chatId, "ℹ️ E'lon saqlandi. Admin tasdiqlagandan so'ng kanalga joylanadi. E'tiboringiz uchun raxmat !");
                    request.setActive(false);
                    productService.save(chatId, request, uname);
                    sessionManager.clearSession(chatId);
                } else if ("Yo'q".equals(text)) {
                    sendMessage(chatId, "Bekor qilindi");
                    sessionManager.clearSession(chatId);
                } else {
                    sendMessage(chatId, "Ha yoki Yo'q tanlang");
                }
            }
        }
    }

    private void sendCategoryButtons(String chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category c : Category.values()) {
            InlineKeyboardButton b = InlineKeyboardButton.builder().text(c.getUzName()).callbackData("category:" + c.name()).build();
            rows.add(List.of(b));
        }
        markup.setKeyboard(rows);
        SendMessage m = SendMessage.builder().chatId(chatId).text("🏷️ Kategoriya tanlang:").replyMarkup(markup).build();
        try {
            execute(m);
        } catch (TelegramApiException e) {
            log.error("Category btn {}", e.getMessage());
        }
    }

    private void showAdminMenu(String chatId) {
        List<Product> pending = productService.getPendingProducts();
        StringBuilder sb = new StringBuilder("🛠️ Tasdiqlash uchun e'lonlar:\n");
        for (Product p : pending) sb.append("ID: ").append(p.getId()).append("\n");
        sendMessageWithKeyboard(chatId, sb.toString(), List.of("Qidirish by ID"));
    }

    private void sendCollagePreview(String chatId, ProductRequest request, String uname) {
        try {
            byte[] collage = createCollage(new ArrayList<>(request.getPhotoInfos().keySet()));
            InputFile file = new InputFile(new ByteArrayInputStream(collage), "preview.jpg");
            String preview = "✅ E'lon tayyor:\n📌 Nomi: " +
                    request.getTitle() + "\n📝 Tavsif: " +
                    request.getDescription() + "\n💰 Narxi: " +
                    request.getPrice() + "\n🏷️ Turkum: " +
                    request.getCategory() + "\n👤Sotuvchi: @" +
                    uname;
            execute(SendPhoto.builder().chatId(chatId).photo(file).caption(preview).build());
        } catch (Exception e) {
            log.error("Preview err {}", e.getMessage());
        }
    }

    private void sendAdminProductView(String chatId, Long id) {
        Product p = productService.findById(id);
        if (p == null) {
            sendMessage(chatId, "E'lon topilmadi");
            return;
        }
        try {
            List<String> fileIds = p.getPhotos().stream().map(Photo::getFileId).toList();
            byte[] collage = createCollage(fileIds);
            InputFile file = new InputFile(new ByteArrayInputStream(collage), "preview.jpg");
            String caption = "ID: " + p.getId() + "\n📌 " + p.getName() + "\n📝 " + p.getDescription() + "\n💰 " + p.getPrice() + "\n🏷️ " + p.getCategory() + "\n👤 @" + p.getOwner().getUsername() + "\n🕒 " + p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            InlineKeyboardButton accept = InlineKeyboardButton.builder().text("Accept").callbackData("admin_accept:" + id).build();
            InlineKeyboardButton reject = InlineKeyboardButton.builder().text("Reject").callbackData("admin_reject:" + id).build();
            rows.add(List.of(accept, reject));
            markup.setKeyboard(rows);

            execute(SendPhoto.builder().chatId(chatId).photo(file).caption(caption).replyMarkup(markup).build());
        } catch (Exception e) {
            log.error("Admin view err {}", e.getMessage());
        }
    }

    @Async
    @Transactional
    public void sendApprovedPost(Product p) {
        try {
            List<String> fileIds = p.getPhotos().stream().map(Photo::getFileId).toList();
            byte[] collage = createCollage(fileIds);
            InputFile file = new InputFile(new ByteArrayInputStream(collage), "post.jpg");

            String post = "📌 Nomi: " + p.getName() + "\n" +
                    "📝 Tavsif: " + p.getDescription() + "\n" +
                    "💰 Narx: " + p.getPrice() + "\n" +
                    "🏷️ Turkum: " + p.getCategory().getUzName() + "\n" +
                    "👤 Sotuvchi: @" + p.getOwner().getUsername() + "\n" +
                    "🔶Mahsulot raqami: " + p.getId() + "\n" +
                    "🕒 Joylangan vaqt: " + p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            execute(SendPhoto.builder()
                    .chatId(channelId)
                    .photo(file)
                    .caption(post)
                    .parseMode("Markdown")
                    .build());

            p.setActive(true);
            productService.savePost(p);
        } catch (Exception e) {
            log.error("Post send error: {}", e.getMessage());
        }
    }

    private byte[] createCollage(List<String> fileIds) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        int totalWidth = 0, maxHeight = 0;
        for (String fileId : fileIds) {
            String fileUrl = execute(org.telegram.telegrambots.meta.api.methods.GetFile.builder().fileId(fileId).build())
                    .getFileUrl(getBotToken());
            BufferedImage img = ImageIO.read(new URL(fileUrl));
            images.add(img);
            totalWidth += img.getWidth();
            maxHeight = Math.max(maxHeight, img.getHeight());
        }
        BufferedImage collage = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = collage.createGraphics();
        int x = 0;
        for (BufferedImage img : images) {
            g.drawImage(img, x, 0, null);
            x += img.getWidth();
        }
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(collage, "jpg", baos);
        return baos.toByteArray();
    }

    private void deleteMessage(String chatId, int messageId) {
        try {
            execute(DeleteMessage.builder().chatId(chatId).messageId(messageId).build());
        } catch (TelegramApiException e) {
            log.error("Delete msg err {}", e.getMessage());
        }
    }

    public void sendMessageWithKeyboard(String chatId, String text, List<String> commands) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        commands.forEach(cmd -> {
            KeyboardRow row = new KeyboardRow();
            row.add(cmd);
            keyboard.add(row);
        });
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        SendMessage message = SendMessage.builder().chatId(chatId).text(text).replyMarkup(keyboardMarkup).build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Keyboard msg {}", e.getMessage());
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(text).parseMode("HTML").build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Msg err {}", e.getMessage());
        }
    }

    private void editMessageText(String chatId, int messageId, String text) {
        try {
            execute(EditMessageText.builder().chatId(chatId).messageId(messageId).text(text).build());
        } catch (Exception e) {
            log.error("Edit msg err {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}