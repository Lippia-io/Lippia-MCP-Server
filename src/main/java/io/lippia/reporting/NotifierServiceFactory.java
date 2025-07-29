package io.lippia.reporting;

import java.util.function.Predicate;
import java.util.function.Supplier;

import java.util.regex.Pattern;

public enum NotifierServiceFactory {
    GOOGLE("google", GoogleChat::new),
    SLACK("slack", Slack::new),
    TEAMS("teams", Teams::new),
    DISCORD("discord", Discord::new);

    private Predicate<String> condition = null;
    private Supplier<Notifier> notifier = null;

    NotifierServiceFactory(String regexp, Supplier<Notifier> notifier) {
        this(matches(regexp), notifier);
    }

    NotifierServiceFactory(Predicate<String> condition, Supplier<Notifier> notifier) {
        this.condition = condition;
        this.notifier = notifier;
    }

    private static Predicate<String> matches(String regexp) {
        return (sequence) -> Pattern.compile(regexp).matcher(sequence).matches();
    }

    public static Notifier find(String key) {
        NotifierServiceFactory watcher = null;
        for (NotifierServiceFactory w: NotifierServiceFactory.values()) {
            if (w.condition.test(key)) {
                watcher = w;
                break;
            }
        }

        return (watcher == null) ? null : watcher.notifier.get();
    }
}
