# Privacy Policy for GDrivePlay

**Last Updated:** May 24, 2026

Rohan Alag ("we," "our," or "us") operates the GDrivePlay Android application. This Privacy Policy explains our policies regarding the collection, use, and disclosure of personal data when you use our application and the choices you have associated with that data.

We use your data strictly to provide and improve the application. By using the app, you agree to the collection and use of information in accordance with this policy.

---

## 1. Information Collection and Use

GDrivePlay is a client-side media player designed to stream videos directly from your Google Drive. 

### Google User Data
To function, the application requests access to specific Google Services via Google Sign-In and OAuth 2.0. We request the following permissions:
*   **Google Drive API (`https://www.googleapis.com/auth/drive.readonly`)**: Used solely to read, list, and stream video and subtitle files stored in your "My Drive" and Shared Drives. 
*   **Basic Profile Information (Email, Name, Profile Picture)**: Used purely to identify the currently logged-in account within the application's user interface.

### No Remote Storage or Third-Party Sharing
*   **100% Client-Side Processing**: All operations (fetching file listings, resolving stream URLs, and playing videos) occur locally on your device.
*   **No Third-Party Transmission**: We **never** collect, store, or transmit your Google Drive files, directory structures, profile details, or login tokens to any third-party servers, databases, or external services. 
*   **Local Storage Only**: Your authorization tokens, playback positions (continue watching history), aspect ratios, and theme preferences are stored strictly inside your device's secured local storage (SharedPreferences and Room SQLite Database).

---

## 2. Google API Services User Data Policy Compliance

GDrivePlay's use and transfer to any other app of information received from Google APIs will adhere to the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy), including the **Limited Use** requirements:

*   We only use access to read Google Drive files to list and play media content inside the app.
*   We do not use or transfer Google user data for serving ads, building user profiles, retargeting, or any marketing purposes.
*   We do not transfer this data to any third parties unless necessary to provide or improve our application's features, to comply with applicable law, or as part of a merger, acquisition, or sale of assets.

---

## 3. Data Retention and Security

*   **Token Retention**: Access and refresh tokens are stored locally on your device. You can completely erase all tokens, settings, and cached history at any time by clicking **Sign Out** in the Settings screen or by clearing the app's data in the Android System Settings.
*   **Security**: We value your trust in providing us access to your files. The app utilizes official, secured Google Android SDKs and encrypted OAuth standard flows to handle credentials, ensuring that your raw Google password is never exposed to the application.

---

## 4. Children's Privacy

Our Service does not address anyone under the age of 13. We do not knowingly collect personally identifiable information from anyone under the age of 13. If you are a parent or guardian and you are aware that your child has provided us with personal data, please contact us.

---

## 5. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the "Last Updated" date. You are advised to review this Privacy Policy periodically for any changes.

---

## 6. Contact Us

If you have any questions or feedback about this Privacy Policy, please contact us:

*   **Email:** alag.rohan@gmail.com
*   **GitHub Repository:** [https://github.com/rohanalag/GDrivePlay](https://github.com/rohanalag/GDrivePlay) *(or your specific repository URL)*
