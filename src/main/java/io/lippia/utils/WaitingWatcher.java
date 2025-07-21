package io.lippia.utils;

import com.crowdar.core.actions.ActionManager;

import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import java.util.regex.Pattern;

public enum WaitingWatcher {
    WAIT_CLICKABLE("waitClickable", ActionManager::waitClickable),
    WAIT_VISIBILITY("waitVisibility", ActionManager::waitVisibility),
    WAIT_VISIBILITIES("waitVisibilities", ActionManager::waitVisibilities),
    WAIT_PRESENCE("waitPresence", ActionManager::waitPresence),
    WAIT_PRESENCES("waitPresences", ActionManager::waitPresences),
    WAIT_INVISIBILITY("waitInvisibility", (arg1) -> {
        ActionManager.waitInvisibility(arg1);
        return null;
    }),
    WAIT_INVISIBILITIES("waitInvisibilities", (arg1) -> {
        ActionManager.waitInvisibilities(arg1);
        return null;
    });

    private Predicate<String> condition = null;
    private Function<String, ?> action = null;

    WaitingWatcher(String regexp, Function<String, ?> action) {
        this(matches(regexp), action);
    }

    WaitingWatcher(Predicate<String> condition, Function<String, ?> action) {
        this.condition = condition;
        this.action = action;
    }

    private static Predicate<String> matches(String regexp) {
        return (sequence) -> Pattern.compile(regexp).matcher(sequence).matches();
    }

    public static Function<String, ?> find(String key) {
        WaitingWatcher watcher = null;
        for (WaitingWatcher w : WaitingWatcher.values()) {
            if (w.condition.test(key)) {
                watcher = w;
                break;
            }
        }

        return (watcher == null) ? null : watcher.action;
    }

    public String call(Function<String, ?> action, String locator) {
        String outString = "Action: " + action;

        if (!locator.isBlank()) {
            Object out = action.apply(locator);

            if (out != null) {
                if (out instanceof WebElement) {
                    outString += " executed on element: " + out;
                } else if (out instanceof List) {
                    outString += " executed on elements: " + out;
                }
            } else {
                outString += " executed with no result";
            }
        } else {
            throw new IllegalArgumentException("Locator cannot be blank");
        }

        return outString;
    }
}
