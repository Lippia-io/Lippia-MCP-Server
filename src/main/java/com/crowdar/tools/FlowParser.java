package com.crowdar.tools;

import com.crowdar.models.Action;
import com.crowdar.models.Flow;
import com.crowdar.utils.MapUtils;

import java.util.*;

/**
 * Utility class to parse generic input arguments into a list of Flow objects.
 */
public class FlowParser {

    /**
     * Parses a raw map of arguments into a list of Flow instances.
     * Supports both single flow (`flow`) and multiple flows (`flows`) keys.
     *
     * @param arguments the input map of arguments
     * @return a list of Flow objects parsed from the input
     */
    public static List<Flow> parse(Map<String, Object> arguments) {
        if (arguments == null) return List.of();

        Object flowsObj = arguments.get("flows");
        Object flowObj = arguments.get("flow");

        if (flowsObj != null) {
            return parseFlowList(flowsObj);
        } else if (flowObj != null) {
            return parseSingleFlow(flowObj);
        }

        return List.of();
    }

    /**
     * Parses an object expected to contain a list of flows.
     *
     * @param flowsObj the raw object expected to be List<Map<String, Object>>
     * @return a list of Flow instances
     */
    private static List<Flow> parseFlowList(Object flowsObj) {
        return MapUtils.getListOf(flowsObj, Map.class)
                .map(flowList -> flowList.stream()
                        .map(rawFlowMap -> {
                            Object rawActions = ((Map<?, ?>) rawFlowMap).get("flow");
                            return parseActionList(rawActions).map(Flow::new).orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .toList()
                )
                .orElse(List.of());
    }

    /**
     * Parses an object expected to contain a single flow.
     *
     * @param flowObj the raw object expected to be List<Map<String, String>>
     * @return a singleton list of Flow, or empty if invalid
     */
    private static List<Flow> parseSingleFlow(Object flowObj) {
        return parseActionList(flowObj)
                .map(actions -> List.of(new Flow(actions)))
                .orElse(List.of());
    }

    /**
     * Parses a list of maps into a list of Action objects.
     *
     * @param obj the object expected to be a List of Map<String, String>
     * @return an Optional list of Action objects
     */
    private static Optional<List<Action>> parseActionList(Object obj) {
        return MapUtils.getListOf(obj, Map.class)
                .map(mapList -> mapList.stream()
                        .map(raw -> MapUtils.getMap(raw, String.class, String.class))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(FlowParser::mapToAction)
                        .toList()
                );
    }

    /**
     * Converts a map of action attributes into an Action object.
     *
     * @param actionMap the map containing action fields
     * @return the Action instance
     */
    private static Action mapToAction(Map<String, String> actionMap) {
        return new Action(
                actionMap.get("action"),
                actionMap.get("url"),
                actionMap.get("by"),
                actionMap.get("value"),
                actionMap.get("text")
        );
    }
}
