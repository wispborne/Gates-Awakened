#!/bin/sh

modFolderName="Gates-Awakened"
version=$(git describe --tags)
git archive master -o artifacts/$modFolderName-$version.zip --prefix $modFolderName-$version/