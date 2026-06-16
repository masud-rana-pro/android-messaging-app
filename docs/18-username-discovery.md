# Username Uniqueness And Discovery

ContactMe now reserves usernames and adds a first user discovery path.

## Username Reservation

Profile save writes two Firestore documents in one transaction:

```text
users/{uid}
usernames/{username}
```

`usernames/{username}` stores:

```text
userId
displayName
updatedAt
```

If another user already owns the username, profile save returns:

```text
This username is already taken.
```

## Discovery

Chats tab now has a username search field.

```text
Search username
-> users ordered by username
-> prefix match
-> current user excluded
```

## Firestore Rule Notes

Rules must allow the signed-in user to write their own profile and reserve a username safely. Final production rules should protect `usernames/{username}` from being overwritten by other users.

## Current Limitations

- Tapping a discovered user opens the existing placeholder chat detail by display name.
- Real conversation creation is not implemented yet.
- Search is prefix-based by username only.
