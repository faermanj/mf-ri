watchman watch $PWD
watchman trigger -j <<EOT
["trigger", "$PWD", {
   "name": "rebuild-previa",
   "command": "./previa.sh.sh"
}]
EOT