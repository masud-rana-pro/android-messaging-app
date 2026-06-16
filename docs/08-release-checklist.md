# Release Checklist

## Current Development Checklist

- [x] Android project scaffold exists.
- [x] ContactMe theme exists.
- [x] Splash/auth/profile/home/settings screens exist.
- [x] Firebase Auth is connected.
- [x] Session restore and logout exist.
- [x] Firestore profile persistence exists.
- [x] Username reservation and discovery exist.
- [x] Direct conversation foundation exists.
- [x] Real text message send/render exists.
- [x] Real conversation list exists.
- [x] Unread/read foundation exists.
- [x] Message sent status foundation exists.
- [x] Firestore MVP rules exist.
- [ ] Chat MVP polish is complete.
- [ ] Phone search is implemented.
- [ ] Profile photo upload is implemented.
- [ ] Push notifications are implemented.
- [ ] Media messages are implemented.
- [ ] Groups are implemented.
- [ ] Calls are implemented.
- [ ] Status/channels are implemented.

## Personal Beta APK Checklist

- [ ] Run `assembleDebug` successfully.
- [ ] Test fresh install.
- [ ] Test login/register with phone and email fallback.
- [ ] Test session restore after app restart.
- [ ] Test profile setup and edit.
- [ ] Test user discovery.
- [ ] Test direct conversation creation.
- [ ] Test message send/receive between two accounts.
- [ ] Test unread/read behavior.
- [ ] Test notification permission flow after notifications are added.
- [ ] Test denied permissions for media/call features after they are added.
- [ ] Check Firebase rules before sharing APK.
- [ ] Add screenshots under `docs/screenshots`.
- [ ] Update README and changelog.
- [ ] Prepare release notes.

## General Release Checklist

- Update roadmap docs.
- Update learning docs.
- Run manual tests.
- Run relevant unit tests.
- Check security rules.
- Check Firebase console data shape.
- Build tested APK.
- Confirm no secrets are committed.
