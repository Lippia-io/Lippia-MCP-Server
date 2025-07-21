package io.lippia.utils;

import com.crowdar.core.actions.ActionManager;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public enum QuestionsWatcher {
    IS_VISIBLE("isVisible", ActionManager::isVisible),
    IS_ENABLED("isEnabled", ActionManager::isEnabled),
    IS_PRESENT("isPresent", ActionManager::isPresent),
    IS_SELECTED("isSelected", ActionManager::isSelected);

    private Predicate<String> condition = null;
    private Function<String, Boolean> action = null;

    QuestionsWatcher(String regexp, Function<String, Boolean> action) {
        this(matches(regexp), action);
    }

    QuestionsWatcher(Predicate<String> condition, Function<String, Boolean> action) {
        this.condition = condition;
        this.action = action;
    }

    private static Predicate<String> matches(String regexp) {
        return (sequence) -> Pattern.compile(regexp).matcher(sequence).matches();
    }

    public static QuestionsWatcher find(String key) {
        QuestionsWatcher watcher = null;
        for (QuestionsWatcher w : QuestionsWatcher.values()) {
            if (w.condition.test(key)) {
                watcher = w;
                break;
            }
        }

        return watcher;
    }

    public String call(String locator) {
        String outString = "Action: " + action;

        if (!locator.isBlank()) {
            Object out = action.apply(locator);

            if (out != null) {
                outString += " executed with result: " + out;
            } else {
                outString += " executed with no result";
            }
        } else {
            throw new IllegalArgumentException("Locator cannot be blank");
        }

        return outString;
    }
}
