# Action Plan & Roadmap

## 1. Critical Fixes (Day 1)

| WP ID | Title | Description | Files Affected | Effort | Dependencies | Acceptance Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **WP-001** | **Implement TaskViewModel** | Create a `TaskViewModel` to act as the centralized state holder for the task creation flow. | `TaskViewModel.kt` (new), `TaskFlowContainer.kt` | M | None | State survives screen transitions within the flow. |
| **WP-002** | **Wire Up Review Screen** | Update `TaskReviewScreen` to fetch data from the `TaskViewModel` instead of using hardcoded mock data. | `TaskReviewScreen.kt`, `TaskFlowContainer.kt` | S | WP-001 | Review screen displays the exact category, location, and details entered by the user. |
| **WP-003** | **Fix Helper Flow Dead End** | Implement functionality or a placeholder screen for the "I want to help" flow instead of a dead button. | `RoleSelectionScreen.kt`, `FixhoraApp.kt` | S | None | Tapping "I want to help" navigates to a designated helper dashboard or coming soon screen. |

## 2. High Priority (Week 1)

| WP ID | Title | Description | Files Affected | Effort | Dependencies | Acceptance Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **WP-004** | **Functional Inputs & Validation** | Make category search and location text inputs functional. Add validation before proceeding to the next step. | `TaskCategoryScreen.kt`, `TaskLocationScreen.kt` | M | WP-001 | Prevent navigation if required fields are empty; search filters categories. |
| **WP-005** | **Photo Picker Integration** | Replace mock photo upload boxes with a real `ActivityResultContracts.PickVisualMedia` image picker implementation. | `TaskPhotosScreen.kt` | M | WP-001 | Users can pick images from the device gallery and preview them in the app. |
| **WP-006** | **Extract Hardcoded Strings** | Move all hardcoded text strings to `res/values/strings.xml` to support localization and maintainability. | All `.kt` files, `strings.xml` | M | None | No hardcoded strings remain in the Compose screens. |

## 3. Medium Priority (Week 2-3)

| WP ID | Title | Description | Files Affected | Effort | Dependencies | Acceptance Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **WP-007** | **Room Database Implementation** | Configure Room to persist task drafts and completed task history locally. | `build.gradle.kts`, `AppDatabase.kt` (new), `TaskEntity.kt` (new), `TaskDao.kt` (new) | L | None | Force closing the app and reopening restores the task draft. |
| **WP-008** | **UI States & Polish** | Add empty states, error states, and basic loading animations to transitions and inputs. | Multiple UI screens | M | None | Visual feedback is provided when actions are taken or data is missing. |

## 4. Low Priority (Backlog)

| WP ID | Title | Description | Files Affected | Effort | Dependencies | Acceptance Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **WP-009** | **Setup Network Layer** | Configure Retrofit, OkHttp, and API interfaces for the eventual backend connection. | `ApiClient.kt` (new), `TaskApiService.kt` (new) | L | None | Network client is configured and ready for endpoint definitions. |
| **WP-010** | **Map SDK Integration** | Replace the placeholder box with a real Google Maps SDK instance in the location picker. | `TaskLocationScreen.kt` | XL | None | User sees a real map and can drop a pin for task location. |
