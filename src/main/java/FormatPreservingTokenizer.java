package org.tokenization;

import com.idealista.fpe.FormatPreservingEncryption;
import com.idealista.fpe.builder.FormatPreservingEncryptionBuilder;
import com.idealista.fpe.component.functions.prf.DefaultPseudoRandomFunction;
import com.idealista.fpe.config.LengthRange;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatPreservingTokenizer {

    private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TWEAK = "tweak123".getBytes(StandardCharsets.UTF_8);

    private final FormatPreservingEncryption fpe;

    public FormatPreservingTokenizer() {
        this.fpe = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDefaultDomain()
                .withPseudoRandomFunction(new DefaultPseudoRandomFunction(KEY))
                .withLengthRange(new LengthRange(2, 256)) // define input length bounds
                .build();
    }

    public String tokenize(String input) {
        if (isEmail(input)) {
            return tokenizeEmail(input);
        } else if (isDate(input)) {
            return tokenizeDate(input);
        } else {
            return fpe.encrypt(input.toLowerCase(), TWEAK);
        }
    }

    public String detokenize(String input) {
        if (isEmail(input)) {
            return detokenizeEmail(input);
        } else if (isDate(input)) {
            return detokenizeDate(input);
        } else {
            return fpe.decrypt(input.toLowerCase(), TWEAK);
        }
    }

    private String tokenizeEmail(String email) {
        String[] parts = email.split("@");
        String local = fpe.encrypt(parts[0].toLowerCase(), TWEAK);
        return local + "@" + parts[1];
    }

    private String detokenizeEmail(String email) {
        String[] parts = email.split("@");
        String local = fpe.decrypt(parts[0].toLowerCase(), TWEAK);
        return local + "@" + parts[1];
    }

    private String tokenizeDate(String dateStr) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            String numeric = new SimpleDateFormat("yyyyMMdd").format(date);
            String encrypted = fpe.encrypt(numeric, TWEAK);
            return encrypted;
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String detokenizeDate(String encrypted) {
        try {
            String decrypted = fpe.decrypt(encrypted, TWEAK);
            Date date = new SimpleDateFormat("yyyyMMdd").parse(decrypted);
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (Exception e) {
            return encrypted;
        }
    }

    private boolean isEmail(String input) { return input.contains("@"); }
    private boolean isDate(String input) { return input.matches("\\d{4}-\\d{2}-\\d{2}"); }

    public static void main(String[] args) {
        FormatPreservingTokenizer tokenizer = new FormatPreservingTokenizer();

        String number = "123456";
        String email = "john.doe@example.com";
        String date = "2025-08-31";
        String alpha = "helloWorld";

        System.out.println("Original Number: " + number + " -> " + tokenizer.tokenize(number) + " -> " + tokenizer.detokenize(tokenizer.tokenize(number)));
        System.out.println("Original Email: " + email + " -> " + tokenizer.tokenize(email) + " -> " + tokenizer.detokenize(tokenizer.tokenize(email)));
        System.out.println("Original Date: " + date + " -> " + tokenizer.tokenize(date) + " -> " + tokenizer.detokenize(tokenizer.tokenize(date)));
        System.out.println("Original Alpha: " + alpha + " -> " + tokenizer.tokenize(alpha) + " -> " + tokenizer.detokenize(tokenizer.tokenize(alpha)));
    }
}
