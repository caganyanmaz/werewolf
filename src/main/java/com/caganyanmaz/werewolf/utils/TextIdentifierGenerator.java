package com.caganyanmaz.werewolf.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;

public class TextIdentifierGenerator {
    private static TextIdentifierGenerator instance;
    public static TextIdentifierGenerator getInstance() {
        if (instance == null) {
            instance = new TextIdentifierGenerator();
        }
        return instance;
    }
    private final String WORDS_PATH = "words.txt";
    private final int WORD_COUNT_PER_IDENTIFIER = 5;
    private List<String> words;
    private Set<String> prev_identifiers;

    private TextIdentifierGenerator() {
        initialize_dictionary();
    }
    
    public String create_identifier() {
        String identifier;
        do {
            identifier = create_random_identifier();
        } while(prev_identifiers.contains(identifier));
        prev_identifiers.add(identifier);
        return identifier;
    }

    private String create_random_identifier() {
        String res = "";
        for (int i = 0; i < WORD_COUNT_PER_IDENTIFIER - 1; i++) {
            res += get_random_word() + "-";
        }
        res += get_random_word();
        return res;
    }

    private String get_random_word() {
        int random_index = ThreadLocalRandom.current().nextInt(words.size());
        return words.get(random_index);
    }

    private void initialize_dictionary() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(WORDS_PATH).getInputStream(),
                        StandardCharsets.UTF_8))) {
            words = reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load words.txt", e);
        }
    }
}

