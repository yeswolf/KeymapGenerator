package yeswolf.keymap.exporter;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("ALL")
public class Exporter implements ApplicationComponent {
    public void initComponent() {
        testgenerate();
//        generateAllKeymapsByAction();
    }

    private void testgenerate() {
        try {
            JSONObject actions = new JSONObject(new String(Files.readAllBytes(Paths.get("/Users/jetbrains/all_config.json"))));
            JSONObject toPrint = new JSONObject(new String(Files.readAllBytes(Paths.get("/Users/jetbrains/to_print.json"))));
            testPrint(actions, toPrint, "Xcode");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testPrint(JSONObject keymaps, JSONObject toPrint, String name) {
        try {
            System.out.println("<html><body>");

            JSONArray actionsJSON = keymaps.getJSONArray("actions");

            JSONArray sectionsJSON = toPrint.getJSONArray("sections");
            for (int i = 0; i < sectionsJSON.length(); i++) {
                JSONObject sectionJSON = sectionsJSON.getJSONObject(i);
                System.out.println("<strong>" + sectionJSON.getString("name") + "</strong>");
                System.out.println("<table>");
                JSONArray actionIDS = sectionJSON.getJSONArray("actions");
                for (int j = 0; j < actionIDS.length(); j++) {
                    JSONObject actionConfigJSON = actionIDS.getJSONObject(j);
                    String ids = actionConfigJSON.getString("id");
                    String[] actions = ids.split("\\,");
                    System.out.println("<tr>");
                    System.out.print("<td>");
                    String description = actionConfigJSON.getString("description");
                    for (int k = 0; k < actions.length; k++) {
                        String action = actions[k];
                        JSONObject actionJSON = findActionJSON(action, actionsJSON);
                        if (actionJSON == null) {
                            System.out.println("<span style='background-color:red;'>Can't find " + action+"</span>");
                            continue;
                        }
                        if (description.isEmpty()) {
                            if (actionJSON.has("description")) {
                                description = actionJSON.getString("description");
                            } else {
                                System.out.println("description missed for " + action);
                                description = actionJSON.getString("name");
                            }
                        }
                        if (actionJSON != null) {
                            JSONArray targetKeymapsJSON = actionJSON.getJSONArray("keymaps");
                            JSONObject targetKeymap = null;
                            for (int m = 0; m < targetKeymapsJSON.length(); m++) {
                                targetKeymap = targetKeymapsJSON.getJSONObject(m);
                                if (targetKeymap.getString("keymap").equalsIgnoreCase(name)) {
                                    break;
                                }
                            }

                            JSONArray shortcutsJSONArray = targetKeymap.getJSONArray("shortcuts");
                            String shortcut = "";
                            if (shortcutsJSONArray.length() > 0) {
                                shortcut = shortcutsJSONArray.getString(0);
                                shortcut = shortcut.replaceAll("\\[", "")
                                        .replaceAll("]", "")
                                        .replaceAll("ctrl", "⌃")
                                        .replaceAll("shift", "⇧")
                                        .replaceAll("meta", "⌘")
                                        .replaceAll("alt", "⌥")
                                        .replaceAll("pressed", "")
                                        .replaceAll("SPACE", "Space")
                                        .replaceAll("ENTER", "⏎")
                                        .replaceAll("SLASH", "/")
                                        .replaceAll("OPEN_BRACKET", "[")
                                        .replaceAll("CLOSE_BRACKET", "]")
                                        .replaceAll("UP", "↑")
                                        .replaceAll("DOWN", "↓")
                                        .replaceAll("DELETE", "⌫")
                                        .replaceAll("BACK_Space", "⌦")
                                        .replaceAll("SUBTRACT", "-")
                                        .replaceAll("ADD", "+")
                                        .replaceAll("ESCAPE", "⎋")
                                        .replaceAll("BACK_QUOTE", "`")
                                        .replaceAll("QUOTE", "'")
                                        .replaceAll("TAB", "⇥")
                                        .replaceAll("LEFT", "←")
                                        .replaceAll("RIGHT", "→")
                                        .replaceAll("\\s", "");
                            }

                            System.out.print(shortcut + (k != actions.length - 1 ? " / " : ""));
                        }
                    }
                    System.out.println("</td>");
                    System.out.println("<td" + (description.isEmpty() ? " style=\"background-color:red;\"" : "") + ">" + description + "</td>");
                    System.out.println("</tr>");
                }
                System.out.println("</table>");

            }

            System.out.println("</body></html>");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject findActionJSON(String id, JSONArray actionsJSON) throws JSONException {
        for (int i = 0; i < actionsJSON.length(); i++) {
            JSONObject action = actionsJSON.getJSONObject(i);
            if (action.getString("id").equalsIgnoreCase(id)) {
                return action;
            }
        }
        return null;
    }

    private void generateAllKeymapsByAction() {
        try {
            Keymap[] allKeymaps = KeymapManagerEx.getInstanceEx().getAllKeymaps();
            ArrayList<String> allActionIds = new ArrayList<>();
            for (Keymap keymap : allKeymaps) {
                @NotNull String[] actionIds = keymap.getActionIds();
                for (String action : actionIds) {
                    if (!allActionIds.contains(action)) {
                        allActionIds.add(action);
                    }
                }
            }
            JSONArray actionsJSON = new JSONArray();
            JSONObject root = new JSONObject();


            for (String actionId : allActionIds) {
                JSONObject actionJSON = new JSONObject();
                AnAction action = ActionManager.getInstance().getAction(actionId);
                if (action == null) {
                    continue;
                }
                Presentation templatePresentation = action.getTemplatePresentation();
                actionJSON.put("id", actionId)
                        .put("name", isNullOrEmpty(templatePresentation.getText()) ? "" : templatePresentation.getText())
                        .put("description", isNullOrEmpty(templatePresentation.getDescription()) ? "" : templatePresentation.getDescription());

                JSONArray keymapsJSON = new JSONArray();
                for (Keymap keymap : allKeymaps) {
                    JSONObject keymapJSON = new JSONObject();
                    @NotNull Shortcut[] shortcuts = keymap.getShortcuts(actionId);
                    JSONArray shortcutsJSON = new JSONArray();
                    for (Shortcut scut : shortcuts) {
                        shortcutsJSON.put(scut.toString());
                    }
                    keymapJSON.put("keymap", keymap.getPresentableName());
                    keymapJSON.put("shortcuts", shortcutsJSON);
                    keymapsJSON.put(keymapJSON);
                }
                actionJSON.put("keymaps", keymapsJSON);
                actionsJSON.put(actionJSON);
            }
            root.put("actions", actionsJSON);
            System.out.println(root);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void generateAllKeymapsByKeymap() {
        try {
            Keymap[] allKeymaps = KeymapManagerEx.getInstanceEx().getAllKeymaps();
            JSONObject root = new JSONObject();
            JSONArray keymapsJSON = new JSONArray();
            for (Keymap keymap : allKeymaps) {
                JSONObject keymapJSON = new JSONObject().put("name", keymap.getPresentableName());
                @NotNull String[] actionIds = keymap.getActionIds();
                JSONArray actionsJSON = new JSONArray();
                for (String actionId : actionIds) {
                    JSONObject actionJSON = new JSONObject();
                    AnAction action = ActionManager.getInstance().getAction(actionId);
                    if (action == null) {
                        continue;
                    }
                    Presentation templatePresentation = action.getTemplatePresentation();
                    actionJSON.put("id", actionId)
                            .put("name", templatePresentation.getText())
                            .put("description", templatePresentation.getDescription());

                    @NotNull Shortcut[] shortcuts = keymap.getShortcuts(actionId);
                    JSONArray shortcutsJSON = new JSONArray();
                    for (Shortcut scut : shortcuts) {
                        shortcutsJSON.put(scut.toString());
                    }
                    actionJSON.put("shortcuts", shortcutsJSON);
                    actionsJSON.put(actionJSON);
                }
                keymapJSON.put("actions", actionsJSON);
                keymapsJSON.put(keymapJSON);
            }
            root.put("keymaps", keymapsJSON);
            System.out.println(root);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public @NotNull
    String getComponentName() {
        return "Exporter";
    }
}
