# Version Control with Git & GitHub/GitLab/Bitbucket

Version control is essential for tracking changes, collaborating with others, and managing your
codebase effectively. Git is the most widely used version control system, and platforms like GitHub,
GitLab, and Bitbucket provide hosting for your Git repositories.

## 1. Understanding Git Basics

* **Repository (Repo):** A collection of files and folders, along with their history. Your Android
  Studio project will be a Git repository.
* **Commit:** A snapshot of your changes at a specific point in time. Each commit has a unique ID
  and a message describing the changes.
* **Branch:** A parallel line of development. You can create branches to work on new features or bug
  fixes without affecting the main codebase.
* **Merge:** Combining changes from different branches.
* **Remote:** A version of your repository hosted on a server (e.g., on GitHub).
* **Clone:** Creating a local copy of a remote repository.
* **Push:** Sending your local commits to a remote repository.
* **Pull:** Fetching changes from a remote repository and merging them into your local branch.
* **Fetch:** Downloading changes from a remote repository without automatically merging them.

## 2. Setting Up Git

* **Install Git:** If you don't have Git installed, download it
  from [git-scm.com](https://git-scm.com/) and install it.
* **Configure Git:**
    * Open a terminal or command prompt.
    * Set your username: `git config --global user.name "Your Name"`
    * Set your email: `git config --global user.email "youremail@example.com"` (This email should
      ideally match the one you use for GitHub/GitLab/Bitbucket).
* **Initialize a Git Repository in Android Studio:**
    * When creating a new project, Android Studio often gives you an option to create a Git
      repository.
    * For an existing project: `VCS > Enable Version Control Integration... > Select "Git"` from the
      dropdown.
    * Alternatively, use the terminal: `cd /path/to/your/project` and then `git init`.

## 3. Choosing a Hosting Platform

Platforms like GitHub, GitLab, and Bitbucket offer remote hosting for your Git repositories, along
with features for collaboration, issue tracking, and more.

* **GitHub:** Very popular, especially for open-source projects. Offers free public and private
  repositories.
* **GitLab:** Provides a complete DevOps platform. Offers free public and private repositories with
  robust CI/CD features.
* **Bitbucket:** Integrates well with other Atlassian products like Jira. Offers free private
  repositories for small teams.

**Steps to host your project:**

1. Create an account on your chosen platform.
2. Create a new remote repository on the platform.
3. Link your local repository to the remote:
    * In Android Studio: `Git > Manage Remotes... > Add`. Enter a name (e.g., "origin") and the URL
      of the remote repository.
    * Or via terminal: `git remote add origin <repository_url>`
4. Push your initial code:
    * Android Studio: `Git > Push...`
    * Terminal: `git push -u origin main` (or `master` if that's your default branch name).

## 4. Branching Strategies

A branching strategy helps manage development workflows. Here's a common and effective model:

* **`main` (or `master`):**
    * This branch should always reflect a production-ready state.
    * Direct commits to `main` are generally discouraged. Changes are merged into `main` from
      `develop` or release branches.
* **`develop` (or `dev`):**
    * This is the primary development branch where all completed features and bug fixes are merged.
    * It represents the latest delivered development changes for the next release.
    * Nightly builds or CI builds are often run from this branch.
* **Feature Branches (`feature/your-feature-name`):**
    * Create a new branch from `develop` for each new feature you work on.
    * Example: `feature/user-authentication` or `feature/add-settings-screen`.
    * Once the feature is complete and tested, merge it back into `develop`.
    * Delete the feature branch after it's merged.
* **Release Branches (`release/v1.0.0`):**
    * When `develop` has enough features for a release, create a `release` branch from `develop`.
    * This branch is for final testing, bug fixes, and preparing for deployment. No new features are
      added here.
    * Once ready, merge the `release` branch into `main` (and tag it, e.g., `v1.0.0`) and also back
      into `develop` (to incorporate any last-minute fixes).
* **Hotfix Branches (`hotfix/fix-critical-bug`):**
    * If a critical bug is found in production (`main`), create a `hotfix` branch directly from
      `main`.
    * Fix the bug, test it, and then merge the `hotfix` branch back into `main` (and tag it) and
      also into `develop` (or the current `release` branch if one exists).

**Workflow Example (Feature Development):**

1. Ensure your `develop` branch is up-to-date: `git checkout develop` then
   `git pull origin develop`.
2. Create a new feature branch: `git checkout -b feature/new-cool-feature develop`.
3. Work on your feature: make changes, commit regularly (`git add .`,
   `git commit -m "Descriptive message"`).
4. Push your feature branch to the remote: `git push origin feature/new-cool-feature`.
5. When the feature is complete, create a Pull Request (PR) or Merge Request (MR) on
   GitHub/GitLab/Bitbucket to merge `feature/new-cool-feature` into `develop`.
6. After code review and approval, merge the PR/MR.
7. Delete the local and remote feature branch (optional but good practice):
    * `git branch -d feature/new-cool-feature`
    * `git push origin --delete feature/new-cool-feature`

## 5. Essential Git Commands (Terminal)

While Android Studio provides a UI for Git, knowing some basic commands is helpful:

* `git status`: Show the working tree status.
* `git add <file>` or `git add .`: Add file contents to the index (staging area).
* `git commit -m "Your commit message"`: Record changes to the repository.
* `git log`: Show commit logs.
* `git pull`: Fetch from and integrate with another repository or a local branch.
* `git push`: Update remote refs along with associated objects.
* `git branch`: List, create, or delete branches.
* `git checkout <branch-name>`: Switch branches or restore working tree files.
* `git merge <branch-name>`: Join two or more development histories together.
* `git remote -v`: Show your remote repositories.

## 6. `.gitignore` File

* Create a `.gitignore` file in the root of your project.
* This file tells Git which files or directories to ignore (e.g., build files, local configuration,
  IDE-specific files).
* Android Studio usually creates a good default `.gitignore` file for Android projects. You can find
  standard templates online (e.g.,
  on [github.com/github/gitignore](https://github.com/github/gitignore)).

By following these guidelines, you'll be well-equipped to manage your Android project's source code
effectively.
