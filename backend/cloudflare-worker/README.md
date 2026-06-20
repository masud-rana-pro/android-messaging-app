# Cloudflare Worker + FCM Future Path

This folder records the no-card notification direction for ContactMe. Step 84 does not deploy or
implement the Worker yet.

The future Worker will:

1. receive an authenticated, rate-limited notification request;
2. verify the Firebase user/call or message relationship;
3. obtain an OAuth access token for FCM HTTP v1 using credentials stored only as Cloudflare secrets;
4. send a data-only FCM payload that the existing Android messaging service can render and route.

Never put a Firebase service-account private key, FCM access token, TURN password, or Worker secret
in Android source, Firestore, Git, or this directory. Direct client-side FCM HTTP v1 sending is not
an acceptable alternative because it would expose privileged credentials.
