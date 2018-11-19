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

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class Exporter implements ApplicationComponent {
    public void initComponent() {
        generateAllKeymapsByAction();
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



    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public @NotNull
    String getComponentName() {
        return "Exporter";
    }
}
