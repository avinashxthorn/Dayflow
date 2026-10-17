# Replace Omnichat with DayFlow on GitHub (Android phone instructions)

These steps are for the owner of the existing repository only. They intentionally **remove Omnichat files from the latest `main` branch**, retaining older commits in Git history. They create the new `dayflow` branch with the same DayFlow project.

## Option A: GitHub Codespaces browser editor

1. Download `DayFlow-Android-Project.zip` to your phone.
2. Open your existing repository in a browser, and choose **Code > Codespaces > Create codespace on main** (you may need **Desktop site** mode in Chrome).
3. In the browser VS Code editor, upload `DayFlow-Android-Project.zip` into the project root using the Explorer **Upload** action.
4. In the Codespaces **Terminal**, run the commands below **inside your existing repository**:

```sh
git switch main
git rm -r .
unzip -o DayFlow-Android-Project.zip
rm DayFlow-Android-Project.zip
git add -A
git commit -m "Replace Omnichat with DayFlow planner"
git push origin main
git switch -c dayflow
git push -u origin dayflow
```

The commands above assume that the file upload is at the repository root, and that you have permission to push to `main`. If `dayflow` already exists, do not blindly overwrite it.

5. Optionally rename the **repository** from `Omnichat` to `DayFlow` at **Settings > General > Repository name**. This is different from creating a `dayflow` branch.
6. Open **Actions > Build DayFlow APK**. Select the latest successful run and download the **DayFlow-APK** artifact, then unzip it to install `app-debug.apk`.

## Option B: Restore GitHub write permissions for ChatGPT

The GitHub connector currently responds with **403 Resource not accessible by integration** to write requests. Reconnect it with repository **Contents: Read and write** access if your setup offers that permission, then request a retry. This is an issue with the current integration authorization, not with the Android source ZIP.

## Important

Your existing commit history is not deleted by these commands. This project is source code plus an automated APK builder; an APK is only available after GitHub Actions runs successfully. Uninstalling your old app might delete its app data, which DayFlow does not migrate.
