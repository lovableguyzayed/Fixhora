# Gap Analysis

## 1. Missing Features

| Category | Gap | Recommended Solution | Priority |
| :--- | :--- | :--- | :--- |
| Core Flow | State Preservation between Steps | Implement a shared `TaskViewModel` mapped to a nested navigation graph to retain draft inputs across screens. | Must-have |
| Core Flow | User Authentication | Add Sign Up, Log In, and Guest browsing modes. | Must-have |
| Provider Flow | "I Want to Help" Dashboard | Implement the provider side to see a feed of tasks, filter by distance/category, and accept tasks. | Must-have |
| User Flow | Task History & Status | Add a screen showing past tasks, active tasks, and their resolution statuses. | Must-have |
| Chat / Comm | In-App Messaging | Allow users who need help to chat with the accepted helpers. | Should-have |
| Reviews | Rating and Reviews | Implement a 5-star rating system and text reviews for completed tasks. | Should-have |

## 2. Missing Design Elements

| Category | Gap | Recommended Solution | Priority |
| :--- | :--- | :--- | :--- |
| States | Loading Skeletons | Implement `Accompanist` placeholder or manual pulse animations for loading maps, categories, and task feed. | Must-have |
| States | Empty States | Add "No Tasks Found" and "No Offers Yet" empty screens with appropriate illustrations for dashboards. | Must-have |
| Error Handling | Validation Visuals | Show red error borders and helper text under TextFields if submission is attempted without required fields. | Must-have |
| Modals | Confirmation Dialogs | Add an alert dialog confirming cancellation of a drafted task when hitting back on the Task Flow. | Should-have |
| UI Polish | Responsive Design | Add Window Size Classes support to render gracefully on landscape and tablet widths instead of stretching single columns. | Nice-to-have |

## 3. Missing APIs / Backend Endpoints

| Category | Gap | Recommended Solution | Priority |
| :--- | :--- | :--- | :--- |
| Tasks | CRUD Endpoints | `POST /tasks`, `GET /tasks`, `GET /tasks/{id}`, `PATCH /tasks/{id}/status` | Must-have |
| Auth | Identity Endpoints | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh` | Must-have |
| Taxonomy | Categories API | `GET /categories` - Replace hardcoded categories with dynamic definitions from the backend. | Should-have |
| Media | Image Upload API | `POST /upload` - Expect multipart/form-data for task images. | Must-have |
| Matching | Search & Filter | `GET /tasks/search?category=x&lat=y&lng=z` | Must-have |

## 4. Missing Integrations

| Category | Gap | Recommended Solution | Priority |
| :--- | :--- | :--- | :--- |
| Maps | Google Maps SDK | Embed `com.google.maps.android:maps-compose` for accurate visual picking instead of a static UI box. | Must-have |
| Identity | Firebase / OAuth | Integrate Google Sign-in to lower onboarding friction. | Should-have |
| Storage | S3 or Cloudinary | Set up an external bucket for handling uploaded task images. | Must-have |
| Payments | Stripe / local gateway | Escrow or payment processing integration for paid tasks. | Nice-to-have |
| Notifications | FCM (Firebase) | Push notifications for "Offer Received" or "Task Accepted". | Must-have |

## 5. Missing Infrastructure

| Category | Gap | Recommended Solution | Priority |
| :--- | :--- | :--- | :--- |
| Architecture | Dependency Injection (DI) | Implement Hilt for easier scaling and passing of Repositories to ViewModels. | Must-have |
| Local DB | Room Database implementation | Define Entities (`TaskEntity`), DAOs, and a Database builder for local caching. | Should-have |
| Network | Retrofit Setup | Define base `ApiClient`, authentication interceptors, and Moshi JSON parsing. | Must-have |
| Env | Secrets Management | Configure `.env` structure through BuildConfig or a Secrets Gradle plugin to hide map/api keys. | Must-have |
| CI/CD | GitHub Actions / Build Scripts | Implement testing pipelines, Lint checks, and debug APK generation on Pull Requests. | Nice-to-have |
