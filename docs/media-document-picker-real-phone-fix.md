# Media and Document Picker Fix for Real Devices and Emulators

This update ensures that users can reliably select and upload images and documents from any source on their device, including the Downloads folder on emulators and various storage providers on real phones.

## Root Cause Found
The previous implementation used `ActivityResultContracts.OpenDocument()` with a specific list of MIME types. On many emulators and some real devices, this caused files in the **Downloads** folder to be hidden if the system could not exactly match the file extension to the provided MIME type list. Additionally, there was a dependency on potential raw file paths which is not robust across modern Android versions.

## Improvements

### 1. Robust File Picking
-   **Images:** Switched to `GetContent("image/*")` for broad compatibility across Gallery, Photos, and Recent files.
-   **Documents:** Switched to `GetContent("*/*")` for documents to ensure all files in the **Downloads** and **Documents** folders are visible and selectable.
-   **Metadata Extraction:** Implemented safe metadata extraction using `ContentResolver` and `OpenableColumns`. The app now correctly retrieves the `displayName`, `mimeType`, and `fileSizeBytes` directly from the `content://` URI.

### 2. Reliable Uploads
-   **Cloudinary Integration:** Extended the `CloudinaryUploadClient` to explicitly handle different resource types.
    -   Images are uploaded to the `/image/upload` endpoint.
    -   Documents (PDF, DOCX, TXT) are uploaded to the `/raw/upload` endpoint to ensure byte-perfect storage.
-   **Size Validation:** Enforced a 10MB limit for images and a 25MB limit for documents before enqueuing the upload, providing immediate feedback to the user.

### 3. Background Resilience
-   **PendingMediaStore:** The app continues to use `PendingMediaStore` to preserve selected files into internal app storage before background processing. This ensures that the upload can complete even if the original URI loses permission or the activity is destroyed.

## Supported File Types
-   **Images:** All standard formats (JPEG, PNG, WEBP, etc.)
-   **Documents:**
    -   PDF (`application/pdf`)
    -   Text (`text/plain`)
    -   Word (`application/msword`)
    -   Word OpenXML (`application/vnd.openxmlformats-officedocument.wordprocessingml.document`)

## Manual Test Flow

### Emulator Retest Steps
1.  Push a PDF or DOCX file to the emulator: `adb push myfile.pdf /sdcard/Download/`
2.  Open **ContactMe** and go to a chat.
3.  Tap the **Attachment** icon -> **Document**.
4.  In the picker, if "Downloads" is not visible, tap the three dots (overflow menu) and select **"Show internal storage"**.
5.  Navigate to **Downloads** and select your file.
6.  Verify that the "Uploading document..." state appears and the message is sent.

### Real Phone Retest Steps
1.  Open a chat and tap **Attachment** -> **Image**.
2.  Select an image from **Gallery** or **Google Photos**.
3.  Verify the upload and send success.
4.  Tap **Attachment** -> **Document**.
5.  Select a PDF or Word document from your phone's storage or a cloud provider (e.g., Drive).
6.  Verify the upload and send success.
