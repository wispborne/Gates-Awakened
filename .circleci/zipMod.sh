#!/bin/sh

# CHANGE ME
modFolderName="Gates-Awakened"

version=$(git describe --tags)
zipName=$modFolderName-$version.zip
git archive master -o $zipName --prefix $modFolderName-$version/
mkdir artifacts
mv $zipName artifacts/