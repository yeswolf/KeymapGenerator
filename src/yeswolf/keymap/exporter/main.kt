package yeswolf.keymap.exporter

import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONException
import org.codehaus.jettison.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

private fun convert(keymaps: JSONObject, toPrint: JSONObject, name: String, mac: Boolean): String {
  var result = ""
  var y = 0
  try {
    result += "<html><body>"

    val actionsJSON = keymaps.getJSONArray("actions")

    val sectionsJSON = toPrint.getJSONArray("sections")
    for (i in 0 until sectionsJSON.length()) {
      val sectionJSON = sectionsJSON.getJSONObject(i)
      result += "<div><table>"
      result += "<thead><tr><th>"
      result += sectionJSON.getString("name").toUpperCase()
      result += "</th><th></th></tr></thead>"

      val actionIDS = sectionJSON.getJSONArray("actions")
      for (j in 0 until actionIDS.length()) {
        val actionConfigJSON = actionIDS.getJSONObject(j)
        val ids = actionConfigJSON.getString("id")
        val actions = ids.split("\\,".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        result += "<tr>"
        var scat = ("<td>")
        var description = actionConfigJSON.getString("description")
        for (k in actions.indices) {
          val action = actions[k]
          val actionJSON = findActionJSON(action, actionsJSON)
          if (actionJSON == null) {
            scat += ("<span style='background-color:red;'>Can't find $action</span>")
            continue
          }
          if (description.isEmpty()) {
            if (actionJSON.has("description")) {
              description = actionJSON.getString("description")
            } else {
              scat += ("description missed for $action")
              description = actionJSON.getString("name")
            }
          }
          if (actionJSON != null) {
            val targetKeymapsJSON = actionJSON.getJSONArray("keymaps")
            var targetKeymap: JSONObject? = null
            for (m in 0 until targetKeymapsJSON.length()) {
              targetKeymap = targetKeymapsJSON.getJSONObject(m)
              if (targetKeymap!!.getString("keymap").equals(name, ignoreCase = true)) {
                break
              }
            }

            val shortcutsJSONArray = targetKeymap!!.getJSONArray("shortcuts")
            var shortcut = ""
            if (shortcutsJSONArray.length() > 0) {
              shortcut = shortcutsJSONArray.getString(0)
              if (mac) {
                shortcut = shortcut.replace("\\[".toRegex(), "")
                    .replace("]".toRegex(), "")
                    .replace("Double ".toRegex(), "Double")
                    .replace("ctrl".toRegex(), "⌃")
                    .replace("shift".toRegex(), "⇧")
                    .replace("meta".toRegex(), "⌘")
                    .replace("alt".toRegex(), "⌥")
                    .replace("BACK_QUOTE".toRegex(), "`")
                    .replace("BACK_SPACE".toRegex(), "⌦")
                    .replace("BACK_SLASH".toRegex(), "&bsol;")
                    .replace("pressed ".toRegex(), "")
                    .replace("SPACE".toRegex(), "Space")
                    .replace("ENTER".toRegex(), "⏎")
                    .replace("SLASH".toRegex(), "/")
                    .replace("OPEN_BRACKET".toRegex(), "[")
                    .replace("CLOSE_BRACKET".toRegex(), "]")
                    .replace("UP".toRegex(), "↑")
                    .replace("DOWN".toRegex(), "↓")
                    .replace("DELETE".toRegex(), "⌫")
                    .replace("SUBTRACT".toRegex(), "-")
                    .replace("ADD".toRegex(), "+")
                    .replace("ESCAPE".toRegex(), "⎋")
                    .replace("QUOTE".toRegex(), "'")
                    .replace("TAB".toRegex(), "⇥")
                    .replace("LEFT".toRegex(), "←")
                    .replace("RIGHT".toRegex(), "→")
                    .replace("\\s".toRegex(), "")
              }else{
                shortcut = shortcut.replace("\\[".toRegex(), "")
                    .replace("]".toRegex(), "")
                    .replace("Double ".toRegex(), "Double")
                    .replace("ctrl".toRegex(), "Ctrl")
                    .replace("shift".toRegex(), "Shift")
                    .replace("meta".toRegex(), "⌘")
                    .replace("alt".toRegex(), "Alt")
                    .replace("BACK_QUOTE".toRegex(), "`")
                    .replace("BACK_SPACE".toRegex(), "Backspace")
                    .replace("BACK_SLASH".toRegex(), "&bsol;")
                    .replace("pressed ".toRegex(), "")
                    .replace("SPACE".toRegex(), "Space")
                    .replace("ENTER".toRegex(), "Enter")
                    .replace("SLASH".toRegex(), "/")
                    .replace("OPEN_BRACKET".toRegex(), "[")
                    .replace("CLOSE_BRACKET".toRegex(), "]")
                    .replace("UP".toRegex(), "↑")
                    .replace("DOWN".toRegex(), "↓")
                    .replace("DELETE".toRegex(), "Delete")
                    .replace("SUBTRACT".toRegex(), "Numpad-")
                    .replace("ADD".toRegex(), "Numpad+")
                    .replace("ESCAPE".toRegex(), "Esc")
                    .replace("QUOTE".toRegex(), "'")
                    .replace("TAB".toRegex(), "⇥")
                    .replace("LEFT".toRegex(), "←")
                    .replace("RIGHT".toRegex(), "→")
                    .replace(" ".toRegex(), "+")
                    .replace("Double".toRegex(), "Double ")
              }
            }

            scat += (shortcut + if (k != actions.size - 1) " / " else "")
          }
        }
        scat += ("</td>")
        var descr = ("<td" + (if (description.isEmpty()) " style=\"background-color:red;\"" else "") + ">" + description + "</td>")
        result += descr + scat
        result += ("</tr>")
      }
      result += ("</table></div>")

    }

    result += ("</body></html>")

  } catch (e: JSONException) {
    e.printStackTrace()
  }
  return result
}

@Throws(JSONException::class)
private fun findActionJSON(id: String, actionsJSON: JSONArray): JSONObject? {
  for (i in 0 until actionsJSON.length()) {
    val action = actionsJSON.getJSONObject(i)
    if (action.getString("id").equals(id, ignoreCase = true)) {
      return action
    }
  }
  return null
}


fun main(args: Array<String>) {
  if (args.count() < 4) {
    print("Usage: <allactions.json> <patch.json> <keymap name> <output file path> <os (mac or empty for win/linux)>")
  }

  try {
    val allactions = args[0]
    val patch = args[1]
    val keymapName = args[2]
    val outputFile = args[3]
    val mac = args.count() == 5 && !(args[4].isEmpty())

    val actions = JSONObject(String(Files.readAllBytes(Paths.get(allactions))))
    val toPrint = JSONObject(String(Files.readAllBytes(Paths.get(patch))))
    val file = File(outputFile)
    file.writeText(convert(actions, toPrint, keymapName, mac))
    Runtime.getRuntime().exec("open ${file.absolutePath}")
  } catch (e: FileNotFoundException) {
    e.printStackTrace()
  } catch (e: IOException) {
    e.printStackTrace()
  } catch (e: JSONException) {
    e.printStackTrace()
  }
}