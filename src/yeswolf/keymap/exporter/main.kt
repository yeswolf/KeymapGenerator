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
  try {
    result += "<html><body>"

    val actionsJSON = keymaps.getJSONArray("actions")

    val sectionsJSON = toPrint.getJSONArray("sections")
    for (i in 0 until sectionsJSON.length()) {
      val sectionJSON = sectionsJSON.getJSONObject(i)
      result += "<strong>" + sectionJSON.getString("name") + "</strong>"
      result += "<table>"
      val actionIDS = sectionJSON.getJSONArray("actions")
      for (j in 0 until actionIDS.length()) {
        val actionConfigJSON = actionIDS.getJSONObject(j)
        val ids = actionConfigJSON.getString("id")
        val actions = ids.split("\\,".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        result += "<tr>"
        result += ("<td>")
        var description = actionConfigJSON.getString("description")
        for (k in actions.indices) {
          val action = actions[k]
          val actionJSON = findActionJSON(action, actionsJSON)
          if (actionJSON == null) {
            result += ("<span style='background-color:red;'>Can't find $action</span>")
            continue
          }
          if (description.isEmpty()) {
            if (actionJSON.has("description")) {
              description = actionJSON.getString("description")
            } else {
              result += ("description missed for $action")
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
              if(mac){
                shortcut = shortcut.replace("\\[".toRegex(), "")
                    .replace("]".toRegex(), "")
                    .replace("ctrl".toRegex(), "⌃")
                    .replace("shift".toRegex(), "⇧")
                    .replace("meta".toRegex(), "⌘")
                    .replace("alt".toRegex(), "⌥")
                    .replace("pressed".toRegex(), "")
                    .replace("SPACE".toRegex(), "Space")
                    .replace("ENTER".toRegex(), "⏎")
                    .replace("SLASH".toRegex(), "/")
                    .replace("OPEN_BRACKET".toRegex(), "[")
                    .replace("CLOSE_BRACKET".toRegex(), "]")
                    .replace("UP".toRegex(), "↑")
                    .replace("DOWN".toRegex(), "↓")
                    .replace("DELETE".toRegex(), "⌫")
                    .replace("BACK_Space".toRegex(), "⌦")
                    .replace("SUBTRACT".toRegex(), "-")
                    .replace("ADD".toRegex(), "+")
                    .replace("ESCAPE".toRegex(), "⎋")
                    .replace("BACK_QUOTE".toRegex(), "`")
                    .replace("QUOTE".toRegex(), "'")
                    .replace("TAB".toRegex(), "⇥")
                    .replace("LEFT".toRegex(), "←")
                    .replace("RIGHT".toRegex(), "→")
                    .replace("\\s".toRegex(), "")
              }
            }

            result += (shortcut + if (k != actions.size - 1) " / " else "")
          }
        }
        result += ("</td>")
        result += ("<td" + (if (description.isEmpty()) " style=\"background-color:red;\"" else "") + ">" + description + "</td>")
        result += ("</tr>")
      }
      result += ("</table>")

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
  if(args.count() < 3){
    print("Usage: <allactions.json> <patch.json> <keymap name> <os (mac or empty for win/linux)>")
  }

  try {
    val allactions = args[0]
    val patch = args[1]
    val keymapName = args[2]
    val mac = args.count() == 4 && !(args[3].isEmpty())

    val actions = JSONObject(String(Files.readAllBytes(Paths.get(allactions))))
    val toPrint = JSONObject(String(Files.readAllBytes(Paths.get(patch))))
    val file = createTempFile()
    val newFile = File(file.absolutePath.replace(".tmp", ".html"))
    file.writeText(convert(actions, toPrint, keymapName, mac))
    file.renameTo(newFile)
    Runtime.getRuntime().exec("open ${newFile.absolutePath}")
  } catch (e: FileNotFoundException) {
    e.printStackTrace()
  } catch (e: IOException) {
    e.printStackTrace()
  } catch (e: JSONException) {
    e.printStackTrace()
  }
}