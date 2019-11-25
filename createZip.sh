#!/bin/bash

read -p "Enter version: " version

git archive master -o Gates-Awakened-$version.zip --prefix Gates-Awakened-$version/