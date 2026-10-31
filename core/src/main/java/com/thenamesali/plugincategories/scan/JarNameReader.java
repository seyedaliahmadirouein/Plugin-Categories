package com.thenamesali.plugincategories.scan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class JarNameReader {

    private static final String[] ENTRIES = {"plugin.yml", "paper-plugin.yml"};
    private static final int MAX_BYTES = 65536;

    private JarNameReader() {
    }

    public static String readName(File jar) {
        try (JarFile file = new JarFile(jar, false)) {
            for (String entryName : ENTRIES) {
                String name = readEntry(file, entryName);
                if (name != null) {
                    return name;
                }
            }
        } catch (Throwable throwable) {
            return null;
        }
        return null;
    }

    private static String readEntry(JarFile file, String entryName) {
        try {
            ZipEntry entry = file.getEntry(entryName);
            if (entry == null) {
                return null;
            }
            InputStream stream = file.getInputStream(entry);
            if (stream == null) {
                return null;
            }
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int read;
                int total = 0;
                while ((read = stream.read(chunk)) != -1 && total < MAX_BYTES) {
                    buffer.write(chunk, 0, read);
                    total += read;
                }
                return parseName(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
            } finally {
                stream.close();
            }
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static String parseName(String yaml) {
        String content = yaml;
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        String[] lines = content.split("\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            if (!key.equals("name")) {
                continue;
            }
            String value = line.substring(separator + 1).trim();
            int comment = value.indexOf(" #");
            if (comment >= 0) {
                value = value.substring(0, comment).trim();
            }
            if (value.length() >= 2 && (isQuoted(value, '"') || isQuoted(value, '\''))) {
                value = value.substring(1, value.length() - 1);
            }
            value = value.replace("\u00A7", "").trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static boolean isQuoted(String value, char quote) {
        return value.charAt(0) == quote && value.charAt(value.length() - 1) == quote;
    }
}
