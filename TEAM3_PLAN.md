# TEAM-3 Development Plan
### Space Invaders Extension Project

---

## Overview
TEAM-3 focuses on implementing the **Main Menu Extension** and system-level features for *Space Invaders*, enhancing the overall UX and ensuring consistency across all modules.  
All features will be developed under **Agile Scrum** principles and follow the **Git Workflow** model (`feature/*`, `development`, `master`).

---

## 1. Sound System
### Separated Sound Categories
- **Effect Sound** — Button click, Pause, enemy kill, etc.
- **In-Game Sound** — Pause, enemy kill, etc. in in-game(pause) screen
- **Background Sound (BGM)** — Stage, menu

### Sound Control
- **Master Volume Control:** Unified control across all categories
- **Mute Function:** Toggle mute for all sound layers simultaneously

✅ *Goal:* Deliver a seamless, non-overlapping sound experience with consistent volume and mute handling.

---

## 2. Delay Management
- Synchronize **screen transition delays** across all scenes (Title → ShipSelection → Play → Score).
- Fix inconsistent frame waits and transition lag between animations.

✅ *Goal:* Ensure uniform responsiveness and smooth transitions.

---

## 3. Ship Purchase System
### Database Design
- **Coin DB:** Track and accumulate player coins (persistent between sessions)
- **Ship DB:** Store purchased ships and their availability

### Functional Features
- **Remove Unused Items:** Only retain **Score** and **Coin** item types
- **Ship Stats:** Define quantifiable performance attributes (speed, fire rate, durability)
- **Ship Lock System:** Locked ships displayed as semi-transparent until purchased
- **Coin Display:** Current coin balance visible in Ship Selection screen

✅ *Goal:* Provide a fully functional, persistent **in-game economy system**.

---

## 4. Score Stage
- Implement a **final infinite stage** that loops continuously after clearing the main game.
- Gradually increase difficulty (enemy speed, spawn rate, etc.)

✅ *Goal:* Extend playtime and challenge through endless mode mechanics.

---

## 5. Visibility Enhancement
- **Item Differentiation:**
    - Distinguish **Score Items** and **Coin Items** through unique color palettes
- **Monster Transparency:**
    - Adjust alpha values for better object visibility during fast gameplay

✅ *Goal:* Improve visual clarity and player focus during high-intensity scenes.

---

## 6. Mouse & Back Button Integration
- **Mouse Support:**
    - Full mouse interaction on all incomplete demo screens
    - Hover and click feedback added to all buttons
- **Back Button Standardization:**
    - Consistent navigation flow across all screens
    - **ESC key:** Universal back/exit function
    - Ensure full **keyboard accessibility**

✅ *Goal:* Standardize control scheme and improve UI usability.

---

## 7. Development Workflow
- **Branch Strategy:**
    - `master`: stable release branch
    - `development`: integration branch for all features
    - `feature/*`: individual feature branches
    - `hotfix/*`: urgent production fixes

- **Sprint Cycle:** 1-week Scrum sprints
- **Code Review:** Pull Request-based review before merging
- **Commit Convention:** Gitmoji + type + description  
  (e.g., `:sparkles: feat: add volume slider in SettingsScreen`)

---

## 8. Continuous Integration (CI)
TEAM-3 implements automated quality verification via **GitHub Actions**, ensuring that every pushed or merged commit maintains production-grade quality.

### 🧪 Static Analysis Automation
| Tool | Purpose | Trigger | Output |
|------|----------|----------|--------|
| **PMD** | Detects code design, style, and performance issues | `pull_request` | `/build/reports/pmd/` |
| **SpotBugs** | Detects runtime and bytecode-level bugs | `pull_request` | `/build/reports/spotbugs/` |

---

## 9. Continuous Deployment (CD)
TEAM-3 uses **GitHub Actions** to automate deployment whenever a Pull Request (PR) is **merged into the `development` branch**.  
This ensures consistent versioning, automatic tagging, release generation, and Slack notifications without manual work.

---

### ⚙️ Workflow Overview
| Stage | Description |
|--------|-------------|
| **Trigger** | Runs when a PR targeting `development` is closed **and merged** |
| **Build** | Executes Gradle build and test tasks to verify the project |
| **Versioning** | Determines the next version tag based on the PR title (e.g., `:sparkles: feat: ...` → minor bump) |
| **Tag & Release** | Creates a new Git tag and GitHub Release with attached build artifacts |
| **Slack Notification** | Posts a message in Slack summarizing the new release |
