#!bin/bash

modFolderName="Your-Mod"
version=${git describe --tags}
git archive master -o $modFolderName-$version.zip --prefix $modFolderName-$version/