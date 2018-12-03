#!/bin/sh

mv AppCode_default_keymap.pdf AppCode_default_keymap_src.pdf
mv AppCode_xcode_keymap.pdf AppCode_xcode_keymap_src.pdf
mv keymaps.pdf keymaps_src.pdf

ps2pdf keymaps_src.pdf keymaps.pdf
ps2pdf AppCode_default_keymap_src.pdf AppCode_default_keymap.pdf
ps2pdf AppCode_xcode_keymap_src.pdf AppCode_xcode_keymap.pdf
rm AppCode_*src* keymaps_src*