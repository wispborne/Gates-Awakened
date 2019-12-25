#!/bin/bash

modFolderName="Gates-Awakened"
version=$(git describe --tags)
git archive master -o $modFolderName-$version.zip --prefix $modFolderName-$version/