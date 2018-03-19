#!/usr/bin/env bash
echo "Initialize Repository\n"
git init

echo "Add Files\n"
git add .

echo "Initial Commit\n"
git commit -m "first commit"

echo "Set REMOTE destination\n"
git remote add origin https://github.com/FeldmanMax/DockerWrapper.git

echo "Push\n"
git push -u origin master