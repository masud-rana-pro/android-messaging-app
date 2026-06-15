# Auth Identity Strategy

ContactMe should use a phone-first identity model with email/password as a secondary fallback.

## Decision

```text
Primary identity: phone number + OTP
Fallback identity: email + password
```

## Why Phone First

- Matches WhatsApp-like user expectations.
- Makes contact discovery easier.
- Works naturally with phonebook/contact sync.
- Helps users recognize people by real-world contact identity.
- Supports call/message discovery through app user identity.

## Why Keep Email/Password

- Useful for development and testing.
- Useful for users who prefer not to use phone login.
- Useful for future web/admin/multi-device access.
- Helpful for recovery and support flows.

## Important Distinction

Phone number does not directly create audio/video calls.

```text
Phone number -> user discovery and identity
Firebase uid -> account identity
ZegoCloud/user call id -> in-app audio/video call session
```

Calls will be internet calls inside ContactMe, not normal mobile carrier calls.

## Recommended User Profile Fields

```text
users/{uid}
  uid
  phoneNumber
  email
  displayName
  username
  photoUrl
  primaryAuthProvider
  createdAt
  updatedAt
  lastSeenAt
```

## Account Linking Rule

Avoid creating duplicate accounts for the same person.

Future target:

```text
One user -> one Firebase uid -> phone provider + optional email provider linked
```

## Current Code Status

- Phone-first UI is added.
- Email login/register fallback is available.
- Real Firebase email/password auth is wired.
- Real Firebase phone OTP is not implemented yet.

## Next Step

Implement Firebase Phone Auth:

- SHA-1/SHA-256 setup if required
- OTP send
- OTP verification screen/state
- Firebase credential sign-in
- account linking strategy later
