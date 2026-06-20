# Step 84: Cloudflare Worker + FCM No-Card Plan

## Decision

The Spark/no-card roadmap does not use Firebase Cloud Functions. The previous Functions source and Firebase Functions Android dependency were removed.

## Future Path

- A Cloudflare Worker will be the trusted FCM HTTP v1 sender.
- Firebase/service credentials must be Cloudflare secrets, never Android or Firestore data.
- The Worker must authenticate requests, verify message/call membership, rate-limit abuse, and send data-only payloads.
- Existing Android FCM rendering and navigation remain reusable and provider-independent.

## Current Boundary

The Worker is documented but intentionally not implemented in Step 84. Direct FCM HTTP v1 calls from Android are forbidden because they expose privileged credentials.
