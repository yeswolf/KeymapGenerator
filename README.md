# What is it?

It's keymap generator for IntelliJ IDEs. Creates beautiful SVG that can be converted to PDF using Adobe Illustrator (or Adobe XD, for free). Use Chrome for viewing SVGs.

# Examples

1. [Default keymap for AppCode](export/default.svg)
2. [Xcode keymap for AppCode](export/default.svg)
3. [PDF](export/keymaps.pdf) with keymaps.

# How to use?

1. Install Gotham fonts (Medium, Light, Regular). Otherwise default system font will be used. 
2. Install [KeymapExporter](KeymapExporter.zip) plugin in the IDE you want to generate keymaps for, restart IDE.
3. Find ```home_folder/all_keymaps.json``` file. 
4. Replace ```all_keymaps.json``` with it. Or leave as is. 
5. Run ```main.kt all_keymaps.json patch.json "KeymapName" result_keymap.svg logo_AppCode.svg mac```, where:
    * ```all_keymaps.json``` is a generated keymap config (step 2-3)
    * ```patch.json``` is a patch file for this config. Allows to have    several shortcuts in one line (see the format below)
    * ```KeymapName``` is the keymap name (for example, "Default")
    * ```result_keymap.svg``` is an output path of our SVG with keymap
    * ```mac``` is a boolean flag for using ⌘, ⌥ and other macOS characters. If empty, generates keymap for Win/Linux. 
6. Open SVG in Chrome check, open with Adobe Illustrator or Adobe XD, insert logo, and save to PDF to have a ready-to-print keymap. Note: logo needs to be inserted manually, I can't figure out how to embed it easily, so current implementation is just to check everything is good. Also, you can't just print the SVG - none of macOS characters will be displayed. 

# Patch.json

1. Contains array of sections (shortcut blocks, like **Editing**). Each section has:
    * name
    * column number (1-3), we have only 3 columns in our keymaps
    * array of actions
2. Each action in array has:
    * id - action id **or** several comma-separated action ids. Find them in ```all_keymaps.json```. Comma-separated values like "ActionID1,ActionID2,..." are converted to "Action1Shortcut / Actions2Shortcut ..." 
    * description - if you have several shortcuts on one line or you don't want to have description from IDE, write your own that works for you.  
    
Yes, you need to create it **once** since we can't generate a keymap directly using our IDE actions. 

# Other notes

Some frequently used actions are not included into ```all_keymaps.json```. Take a look at replacements and add them by hand (or take a look at [patch.json for AppCode](patch.json) and reuse some parts). If action is missed, "Can't find <actionid>" will be generated instead of the shortcut. If some key names (like HOME or MULTIPLY) appear in the keymap - create an issue or pull request.
