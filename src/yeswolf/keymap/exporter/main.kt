package yeswolf.keymap.exporter

import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONException
import org.codehaus.jettison.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


fun start(logoPath:String) = """<?xml version="1.0" encoding="utf-8"?>
<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px"
     y="0px"
     viewBox="0 0 842 596" style="enable-background:new 0 0 842 596;" xml:space="preserve">
<style type="text/css">
	.headerline{fill:none;stroke:#000000;stroke-miterlimit:10;}
	.row_line{fill:none;stroke:#77787B;stroke-width:0.25;stroke-miterlimit:10;}
	.medium{font-family:'Gotham-Medium';}
	.font_size{font-size:7px;}
	.light{font-family:'Gotham-Light';}
	.slash{fill:#77787B;}
	.medium_plus{font-family:'Gotham-Medium+';}
	.st7{fill:#FFFFFF;}
	.st8{clip-path:url(#SVGID_2_);fill:#FFFFFF;}
	.st9{clip-path:url(#SVGID_4_);}
	.svg1{clip-path:url(#SVGID_6_);}
	.row_line1{letter-spacing:-1;}
</style>
    <image x="28" y="36" height="22" xlink:href="$logoPath" />
""".trimIndent()

val finish = """
</svg>
""".trimIndent()

private fun convert(keymaps: JSONObject, toPrint: JSONObject, name: String, logoPath: String, mac: Boolean): String {
  var result = start(logoPath)
  val firstColumnY = 76
  val columnWidth = 224
  val leftMargin = 28
  val columnMargin = leftMargin*2
  val textLineGap = 4
  val lineHeight = 8
  val allColumnY = firstColumnY - 3*(lineHeight+textLineGap)
  val shortCutGap = 156
  var headery = firstColumnY
  var y: Int
  try {
    val actionsJSON = keymaps.getJSONArray("actions")
    val sectionsJSON = toPrint.getJSONArray("sections")
    var lastColumn = 1
    for (i in 0 until sectionsJSON.length()) {
      val sectionJSON = sectionsJSON.getJSONObject(i)
      val column = sectionJSON.getInt("column")
      if (lastColumn != column) {
        headery = allColumnY
        y = headery
        lastColumn = column
      }
      y = headery
      val x = (column - 1) * (columnWidth + columnMargin) + leftMargin
      val x2 = (column) * columnWidth + (column - 1) * (columnMargin) + leftMargin

      result += "<text transform=\"matrix(1 0 0 1 $x $y)\">" +
          "<tspan x=\"0\" y=\"0\" class=\"medium font_size\">"
      result += sectionJSON.getString("name").toUpperCase()
      y += textLineGap

      result += "</tspan></text>\n" +
          "<line class=\"headerline\" x1=\"$x\" y1=\"$y\" x2=\"$x2\" y2=\"$y\"/>\n"

      val actionIDS = sectionJSON.getJSONArray("actions")
      for (j in 0 until actionIDS.length()) {
        val actionConfigJSON = actionIDS.getJSONObject(j)
        val ids = actionConfigJSON.getString("id")
        val actions = ids.split("\\,".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        y += lineHeight
        var scat = ("<text transform=\"matrix(1 0 0 1 ${x + shortCutGap} $y)\">" +
            "<tspan x=\"0\" y=\"0\" class=\"medium_plus font_size\">")
        var description = actionConfigJSON.getString("description")
        for (k in actions.indices) {
          val action = actions[k]
          val actionJSON = findActionJSON(action, actionsJSON)
          if (actionJSON == null) {
            scat += ("Can't find $action")
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
                  .replace("HOME".toRegex(), "Home")
                  .replace("BACK_SPACE".toRegex(), "⌦")
                  .replace("BACK_SLASH".toRegex(), "&#x5c;")
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
            } else {
              shortcut = shortcut.replace("\\[".toRegex(), "")
                  .replace("]".toRegex(), "")
                  .replace("Double ".toRegex(), "Double")
                  .replace("ctrl".toRegex(), "Ctrl")
                  .replace("shift".toRegex(), "Shift")
                  .replace("meta".toRegex(), "⌘")
                  .replace("alt".toRegex(), "Alt")
                  .replace("BACK_QUOTE".toRegex(), "`")
                  .replace("HOME".toRegex(), "Home")
                  .replace("BACK_SPACE".toRegex(), "Backspace")
                  .replace("BACK_SLASH".toRegex(), "&#x5c;")
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
        scat += ("</tspan></text>")
        val descr = "<text transform=\"matrix(1 0 0 1 $x $y)\">" +
            "<tspan x=\"0\" y=\"0\" class=\"light font_size\">" +
            description +
            "</tspan>" +
            "</text>\n"
        result += descr + scat
        y += textLineGap
        result += "<line class=\"row_line\" x1=\"$x\" y1=\"$y\" x2=\"$x2\" y2=\"$y\"/>\n"
      }
      headery = y + (lineHeight + textLineGap)*2
    }
  } catch (e: JSONException) {
    e.printStackTrace()
  }
  result += finish
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
  if (args.count() < 5) {
    print("Usage: <allactions.json> <patch.json> <keymap name> <output file path> <svg IDE logo path> <os (mac or empty for win/linux)>")
  }

  try {
    val allactions = args[0]
    val patch = args[1]
    val keymapName = args[2]
    val outputFile = args[3]
    val logoPath = args[4]
    val mac = args.count() == 6 && !(args[5].isEmpty())

    val actions = JSONObject(String(Files.readAllBytes(Paths.get(allactions))))
    val toPrint = JSONObject(String(Files.readAllBytes(Paths.get(patch))))
    val file = File(outputFile)
    val result = convert(actions, toPrint, keymapName, logoPath, mac)
    file.writeText(result)
    Runtime.getRuntime().exec("open ${file.absolutePath}")
  } catch (e: FileNotFoundException) {
    e.printStackTrace()
  } catch (e: IOException) {
    e.printStackTrace()
  } catch (e: JSONException) {
    e.printStackTrace()
  }
}