# Discovery & Research

## 1. Project Structure Map

- **Type:** Single App Project (Android - non-monorepo)
- **Root level Files:**
  - `build.gradle.kts` (Project level config)
  - `settings.gradle.kts` (Workspace config)
  - `metadata.json` (AI Studio Metadata)
  - `.env.example`
- **Frontend App directory (`/app`):**
  - `build.gradle.kts` (App level dependencies)
  - `proguard-rules.pro`
  - `src/main/`
    - `AndroidManifest.xml` (Implicit)
    - `res/` (Resources)
    - `java/com/example/` (Source Root)
      - `MainActivity.kt` (Entry Point)
      - `ui/`
        - `theme/` (Design System)
        - `screens/`
          - `FixhoraApp.kt` (Main Navigation Host)
          - `RoleSelectionScreen.kt`
          - `TaskFlowContainer.kt`
          - `taskflow/`
            - `TaskCategoryScreen.kt`
            - `TaskLocationScreen.kt`
            - `TaskPhotosScreen.kt`
            - `TaskReviewScreen.kt`

## 2. Tech Stack Detection

- **Platform:** Android Mobile App
- **Language:** Kotlin (Java 11 compatibility)
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Navigation:** Jetpack Navigation Compose
- **Architecture / API:** Retrofit + OkHttp + Moshi (added in dependencies but currently seemingly unused in UI)
- **Local Persistence / DB:** Room Database (Dependencies mapped but likely unused at the moment)
- **Testing:** Robolectric, Roborazzi
- **Dependencies list checks:** Accompanist, Coil, Firebase exist in the BOM but seem commented out or unused explicitly.

## 3. Purpose & Functionality

This application (internally named Fixhora) appears to be a marketplace or task delegation platform connecting people who need help with people who provide services. It lets a primary user select their operational role (e.g., getting help vs providing help). Users who declare they "need help" are then routed into a multi-step task creation funnel (TaskFlow) to post their specific jobs to the network (Category -> Location -> Photos -> Review). 

**Current evident flows:**
- Startup -> Role Selection
- Select "Need Help" -> Task Flow Navigation
- Task Flow: Choose Category -> Specify Location -> Add Photos / Tips -> Review Task -> Submit

## 4. Architecture Diagram (text-based)

```text
[ Mobile App (Jetpack Compose) ] 
        |
        v
[ FixhoraApp NavHost ]
       /           \
      v             v
[ Role Selection ]  [ TaskFlowContainer (Nested NavHost) ]
                       |-> TaskCategoryScreen
                       |-> TaskLocationScreen
                       |-> TaskPhotosScreen
                       |-> TaskReviewScreen
                       |-> Success Dialog/Screen

[ Backend / Service Layer ] 
- No evident implementation of backend calls yet (Retrofit mapped in gradle, but no repository layer spotted in surface-level scans). 
- All state seems local and transient.
```
