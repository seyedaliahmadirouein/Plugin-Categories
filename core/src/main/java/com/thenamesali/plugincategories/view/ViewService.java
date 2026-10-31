package com.thenamesali.plugincategories.view;

import com.thenamesali.plugincategories.Settings;
import com.thenamesali.plugincategories.chat.ChatPart;
import com.thenamesali.plugincategories.chat.ChatRenderer;
import com.thenamesali.plugincategories.chat.LegacyText;
import com.thenamesali.plugincategories.model.CategoryData;
import com.thenamesali.plugincategories.model.JarInfo;
import com.thenamesali.plugincategories.model.PluginState;
import com.thenamesali.plugincategories.model.ScanResult;
import com.thenamesali.plugincategories.scan.ScanManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ViewService {

    private final Settings settings;
    private final ChatRenderer renderer;
    private final ScanManager scanManager;

    public ViewService(Settings settings, ChatRenderer renderer, ScanManager scanManager) {
        this.settings = settings;
        this.renderer = renderer;
        this.scanManager = scanManager;
    }

    public void showMain(CommandSender viewer) {
        if (!viewer.hasPermission(this.settings.getPermission())) {
            return;
        }
        if (!this.scanManager.isReady()) {
            viewer.sendMessage(LegacyText.translate(this.settings.getMsgScanning()));
            this.scanManager.requestScan(true);
            return;
        }
        ScanResult result = this.scanManager.latest();
        List<List<ChatPart>> lines = new ArrayList<List<ChatPart>>();
        lines.add(countLine(result));
        if (this.settings.isCategoryDetection() && !result.getCategories().isEmpty()) {
            for (CategoryData category : result.getCategories()) {
                lines.add(categoryLine(category));
            }
        } else {
            for (JarInfo info : result.getAllPlugins()) {
                lines.add(pluginLine(info));
            }
        }
        this.renderer.send(viewer, lines);
        this.scanManager.requestScan(false);
    }

    public void showCategory(CommandSender viewer, String categoryName) {
        if (!viewer.hasPermission(this.settings.getPermission())) {
            return;
        }
        if (!this.scanManager.isReady()) {
            viewer.sendMessage(LegacyText.translate(this.settings.getMsgScanning()));
            this.scanManager.requestScan(true);
            return;
        }
        CategoryData category = findCategory(categoryName);
        if (category == null) {
            viewer.sendMessage(LegacyText.translate(this.settings.getMsgUnknownCategory()).replace("%category%", categoryName));
            return;
        }
        List<List<ChatPart>> lines = new ArrayList<List<ChatPart>>();
        lines.add(Arrays.asList(new ChatPart(category.getName(), this.settings.getCategoryColor())));
        for (JarInfo jar : category.getJars()) {
            lines.add(pluginLine(jar));
        }
        lines.add(backLine());
        this.renderer.send(viewer, lines);
    }

    private CategoryData findCategory(String name) {
        String key = name.trim().toLowerCase(Locale.ROOT);
        for (CategoryData category : this.scanManager.latest().getCategories()) {
            if (category.getName().toLowerCase(Locale.ROOT).equals(key)) {
                return category;
            }
        }
        return null;
    }

    private List<ChatPart> countLine(ScanResult result) {
        String format = this.settings.getCountFormat();
        List<ChatPart> parts = new ArrayList<ChatPart>();
        int index = 0;
        while (index < format.length()) {
            int successPos = format.indexOf("%success%", index);
            int failedPos = format.indexOf("%failed%", index);
            int position;
            boolean isSuccess;
            if (successPos == -1 && failedPos == -1) {
                parts.addAll(LegacyText.parse(format.substring(index), this.settings.getHeaderColor()));
                break;
            }
            if (successPos == -1) {
                position = failedPos;
                isSuccess = false;
            } else if (failedPos == -1) {
                position = successPos;
                isSuccess = true;
            } else if (successPos <= failedPos) {
                position = successPos;
                isSuccess = true;
            } else {
                position = failedPos;
                isSuccess = false;
            }
            if (position > index) {
                parts.addAll(LegacyText.parse(format.substring(index, position), this.settings.getHeaderColor()));
            }
            String number = String.valueOf(isSuccess ? result.getSuccessCount() : result.getFailedCount());
            parts.add(new ChatPart(number, isSuccess ? this.settings.getSuccessColor() : this.settings.getFailedColor()));
            index = position + (isSuccess ? 9 : 8);
        }
        if (parts.isEmpty()) {
            parts.add(new ChatPart("", this.settings.getHeaderColor()));
        }
        return parts;
    }

    private List<ChatPart> categoryLine(CategoryData category) {
        ChatPart part = new ChatPart(this.settings.getCategoryPrefix() + category.getName(), this.settings.getCategoryColor());
        part.click("/" + this.settings.getNavigationCommand() + " view " + category.getName());
        if (this.settings.isHoverMessages()) {
            part.hover(this.settings.getCategoryHover().replace("%category%", category.getName()));
        }
        return Arrays.asList(part);
    }

    private List<ChatPart> pluginLine(JarInfo info) {
        ChatPart part = new ChatPart(info.getName(), info.isSuccessful() ? this.settings.getSuccessColor() : this.settings.getFailedColor());
        if (this.settings.isPluginHover()) {
            String location = info.getFile() != null ? info.getFile().getPath() : "unknown";
            part.hover("&7State: &f" + stateLabel(info.getState()) + "\n&7File: &f" + location);
        }
        return Arrays.asList(part);
    }

    private List<ChatPart> backLine() {
        ChatPart button = new ChatPart("[Back]", this.settings.getBackColor(), this.settings.isBackBold(), false, false, false, false);
        button.click("/" + this.settings.getNavigationCommand() + " back");
        if (this.settings.isHoverMessages()) {
            button.hover(this.settings.getBackHover());
        }
        List<ChatPart> line = new ArrayList<ChatPart>();
        String padding = padding(this.settings.getBackPadding());
        if (!padding.isEmpty()) {
            line.add(new ChatPart(padding, null));
        }
        line.add(button);
        return line;
    }

    private String padding(int spaces) {
        if (spaces <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    private String stateLabel(PluginState state) {
        switch (state) {
            case ENABLED:
                return "Loaded and enabled";
            case DISABLED:
                return "Loaded but disabled";
            case NOT_LOADED:
                return "Not loaded by the server";
            default:
                return "Invalid or unreadable plugin file";
        }
    }
}
