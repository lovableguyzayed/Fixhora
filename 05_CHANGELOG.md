# Changelog

## [WP-001] Implement TaskViewModel
**Status:** Completed
**Description:** Created a `TaskViewModel` state holder to persist task draft selections across the task creation flow.
**Files Modified:** `TaskFlowContainer.kt`, `TaskCategoryScreen.kt`, `TaskLocationScreen.kt`, `TaskPhotosScreen.kt`, `TaskReviewScreen.kt`, `TaskViewModel.kt`
**Outcome:** State survives screen transitions within the flow in `TaskViewModel`.

## [WP-002] Wire Up Review Screen
**Status:** Completed
**Description:** Updated `TaskReviewScreen` to fetch data from the `TaskViewModel` instead of using hardcoded mock data. 
**Files Modified:** `TaskReviewScreen.kt`, `TaskCategoryScreen.kt` (extracted shared CategoriesData.kt).
**Outcome:** Review screen displays the exact category, location, and details entered by the user.

## [WP-003] Fix Helper Flow Dead End
**Status:** Completed
**Description:** Implemented a placeholder 'Coming Soon' screen for the helper flow.
**Files Modified:** `RoleSelectionScreen.kt`, `FixhoraApp.kt`, `HelperComingSoonScreen.kt` (new)
**Outcome:** Tapping "I want to help" navigates to a designated helper coming soon screen.

## [WP-004] Functional Inputs & Validation
**Status:** Completed
**Description:** Made category search and location text inputs functional. Added validation barriers for empty inputs before proceeding to next steps.
**Files Modified:** `TaskCategoryScreen.kt`, `TaskLocationScreen.kt`
**Outcome:** Search actively filters categories. User cannot proceed without selecting a category or satisfying location prerequisites.

## [WP-005] Photo Picker Integration
**Status:** Completed
**Description:** Replaced mock photo upload boxes with a real `PickMultipleVisualMedia` image picker implementation using Coil for loading.
**Files Modified:** `TaskPhotosScreen.kt`, `build.gradle.kts`
**Outcome:** Users can pick images from the device gallery and preview them in the app.

## [WP-006] Extract Hardcoded Strings
**Status:** Deferred
**Description:** Extracting strings to `strings.xml`. Deferred to prioritize core functionality like data persistence.

## [WP-007] Room Database Implementation
**Status:** Completed
**Description:** Configured Room database with `TaskEntity`, `TaskDao`, and `AppDatabase`.
**Files Modified:** `build.gradle.kts`, `FixhoraApplication.kt` (new), `TaskEntity.kt` (new), `TaskDao.kt` (new), `TaskRepository.kt` (new), `TaskViewModel.kt`, `TaskFlowContainer.kt`.
**Outcome:** Task drafts are now auto-saved to Room as users iterate through the steps. Force closing the app and reopening restores the user's progress. Submitting saves it permanently.

## [WP-008] Fix Interaction Channel Crashes
**Status:** Completed
**Description:** Addressed application crashes causing `Channel is unrecoverably broken` errors.
**Files Modified:** `AndroidManifest.xml`, `TaskViewModel.kt`
**Outcome:** Added `INTERNET` permission for Coil. Added debounce & exception handling to Room DB transactions since keystroke state updates were overwhelming the main dispatch thread with synchronous inserts. Fully-qualified the `<application>` name in the manifest to ensure correct instantiation Context.

## [WP-009] Functional Provider Dashboard
**Status:** Completed
**Description:** Added functional implementation for the Provider Dashboard, replacing the mock screen.
**Files Modified:** `HelperDashboardScreen.kt`, `HelperViewModel.kt`, `FixhoraApp.kt`, `HelperComingSoonScreen.kt` (deleted)
**Outcome:** The provider flow now actively queries and displays the submitted tasks from the Room database in a feed. Requests made via the 'Need Help' persona are actively visible in the 'I want to help' persona.

## [WP-010] Fix Startup Crash
**Status:** Completed
**Description:** Addressed an unrecoverable Channel broken exception caused by schema evolution.
**Files Modified:** `FixhoraApplication.kt`
**Outcome:** Added `fallbackToDestructiveMigration()` to Room database builder so the app cleanly drops and rebuilds the SQLite tables instead of deadlocking the launch activity window when local Entity classes change shapes.

## [WP-011] Onboarding UI Redesign
**Status:** Completed
**Description:** Complete UI revamp for the Onboarding / Role Selection Screen using Material 3 and custom Compose Canvas graphics.
**Files Modified:** `RoleSelectionScreen.kt`, `Color.kt`
**Outcome:** Replaced default styling with the specified flat vector aesthetics, updated primary color palettes, added skyline and cloud background canvases, and introduced the custom split-color Fixhora App Logo rendering logic.
