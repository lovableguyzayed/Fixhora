# Forensic Audit

## 1. Functionality Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Critical** | `TaskReviewScreen.kt` | 52-137 | Hardcoded data in Review Screen | Task review screen completely ignores user selections from previous steps. Everything is hardcoded (Category, Description, Location, Photos, Budget), defeating the purpose of the flow. |
| **Critical** | `RoleSelectionScreen.kt` | 96 | Dead Button ("I want to help") | The "I want to help" role has an empty `onClick` handler (`onRoleSelected("helper")` is not handled in `FixhoraApp.kt`), making the entire helper flow a dead end. |
| **High** | `TaskCategoryScreen.kt` | 71 | Broken Search Input | The `OutlinedTextField` has hardcoded `value = ""` and empty `onValueChange`, making search non-functional. |
| **High** | `TaskLocationScreen.kt` | 56 | Broken Location Input | The address search `OutlinedTextField` is static, non-interactive, and does nothing. |
| **High** | `TaskPhotosScreen.kt` | 53-128 | Fake Photo Upload | Photo upload area is purely visual. Tapping the upload areas does not launch an image picker. Mock images cannot be removed. |
| **Medium** | `TaskCategoryScreen.kt` | 34 | Missing Validation on Continue | A user can press "Continue" without selecting any category. |
| **Medium** | `TaskFlowContainer.kt` | 76 | Dead App Bar Actions | The top App Bar "Edit" / "Skip" buttons have empty `TODO` handlers. |

## 2. UI / UX Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Medium** | `RoleSelectionScreen.kt` | 134 | Fake Language selection | Hindi language selection changes button state but not the actual language of the app. Strings are not extracted to `strings.xml`. |
| **Medium** | `TaskLocationScreen.kt` | 128 | Static Switch State Effect | Toggling "Use my current location" does not update the mock location or request GPS permissions. |
| **Low** | Global | N/A | Missing Loading States | Data is assumed instantaneous; there are no skeletons or loading indicators for category fetching, map loading, or submission. |
| **Low** | Global | N/A | Accessibility | Many generic decorative icons have `contentDescription = null` which can be fine, but interactive mock photos/areas lack semantic descriptions. |

## 3. Code Quality Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Critical** | Global | N/A | Missing ViewModels (No State Management) | The app lacks a centralized state holder (ViewModel) for the Task Creation funnel. State is non-existent between screens, meaning data cannot be passed or persisted. |
| **High** | Global | N/A | Hardcoded Strings | Almost all strings are hardcoded in the Compose files instead of being extracted to `res/values/strings.xml`, breaking i18n capabilities. |
| **Medium** | `FixhoraApp.kt` | 12 | String-based Navigation | Navigation relies on hardcoded strings instead of type-safe serialization which is error-prone. |
| **Low** | `build.gradle.kts` | 91, 102 | Unused Dependencies | Room and Retrofit dependencies are declared and KSP is running but they are completely unused in the code, increasing compile times. |

## 4. Backend / API Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Critical** | Global | N/A | No Backend Architecture | Retrofit dependencies exist but there are no api interfaces, models, or network client setup. All interactions simulate successful backend operations. |
| **High** | `TaskFlowContainer.kt` | 114 | Mock Submission | Hitting "Post Task" just triggers a local navigation pop-up. There is no API call. |

## 5. Database Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **High** | Global | N/A | Missing Local Persistence | App is missing Room DB setup (entities, DAOs, Database class) despite having Room dependencies. Task drafts are not saved locally, leading to data loss on app kill. |

## 6. Security Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Medium** | `TaskLocationScreen.kt` | N/A | Missing Location Permissions | App mentions "location is safe" but doesn't implement or request runtime permissions (Manifest or dynamically) to fetch real locations. |

## 7. Performance Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Medium** | `TaskCategoryScreen.kt` | 48 | Recreation of immutable lists | The `categories` list is created inside the `@Composable` function, causing it to be recreated on every recomposition. |

## 8. DevOps / Deployment Audit

| Severity | File Path | Line | Description | Problem |
| :--- | :--- | :--- | :--- | :--- |
| **Medium** | `.env.example` | N/A | Empty Environment definitions | Base secrets are not declared. Any future API integrations will lack clear configuration expectations. |
