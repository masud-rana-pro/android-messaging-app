import {initializeApp} from "firebase-admin/app";
import {DocumentReference, getFirestore} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";
import {logger} from "firebase-functions";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {HttpsError, onCall} from "firebase-functions/v2/https";
import {defineSecret, defineString} from "firebase-functions/params";
import {generateZegoToken04} from "./zegoToken04";

initializeApp();

const db = getFirestore();
const messaging = getMessaging();
const zegoAppId = defineString("ZEGO_APP_ID");
const zegoServerSecret = defineSecret("ZEGO_SERVER_SECRET");

const ZEGO_TOKEN_LIFETIME_SECONDS = 10 * 60;

export const issueZegoCallToken = onCall(
  {
    region: "asia-south1",
    secrets: [zegoServerSecret],
  },
  async (request) => {
    const userId = request.auth?.uid;
    const callId = stringValue(request.data?.callId).trim();
    if (!userId) throw new HttpsError("unauthenticated", "Sign in before joining a call.");
    if (!isSafeCallId(callId)) throw new HttpsError("invalid-argument", "Invalid call id.");

    const callSnapshot = await db.collection("calls").doc(callId).get();
    if (!callSnapshot.exists) throw new HttpsError("not-found", "Call not found.");
    const participantIds = stringArray(callSnapshot.get("participantIds"));
    if (participantIds.length !== 2 || new Set(participantIds).size !== 2 ||
      !participantIds.includes(userId)) {
      throw new HttpsError("permission-denied", "You are not a participant in this call.");
    }

    const status = stringValue(callSnapshot.get("status"));
    const callExpiresAtMillis = timestampMillis(callSnapshot.get("expiresAt"));
    if (!["ringing", "accepted"].includes(status) || callExpiresAtMillis <= Date.now()) {
      throw new HttpsError("failed-precondition", "This call is no longer active.");
    }

    const roomId = stringValue(callSnapshot.get("roomId"));
    if (!isSafeRoomId(roomId)) throw new HttpsError("failed-precondition", "Call room is invalid.");
    const appId = Number(zegoAppId.value());
    if (!Number.isSafeInteger(appId) || appId <= 0) {
      logger.error("ZEGO_APP_ID is not configured");
      throw new HttpsError("failed-precondition", "Calling is not configured.");
    }

    const payload = JSON.stringify({
      room_id: roomId,
      privilege: {1: 1, 2: 1},
      stream_id_list: null,
    });
    const token = generateZegoToken04(
      appId,
      userId,
      zegoServerSecret.value(),
      ZEGO_TOKEN_LIFETIME_SECONDS,
      payload,
    );
    return {
      appId,
      token,
      roomId,
      expiresAtSeconds: Math.floor(Date.now() / 1000) + ZEGO_TOKEN_LIFETIME_SECONDS,
    };
  },
);

type TokenTarget = {
  token: string;
  reference: DocumentReference;
};

export const sendMessageNotification = onDocumentCreated(
  {
    document: "conversations/{conversationId}/messages/{messageId}",
    region: "asia-south1",
    retry: false,
  },
  async (event) => {
    const message = event.data?.data();
    const conversationId = event.params.conversationId;
    const messageId = event.params.messageId;
    const senderId = stringValue(message?.senderId);

    if (!message || !senderId) {
      logger.warn("Skipping malformed message notification", {conversationId, messageId});
      return;
    }

    const conversationSnapshot = await db.collection("conversations")
      .doc(conversationId)
      .get();
    const participantIds = stringArray(conversationSnapshot.get("participantIds"));
    const candidateRecipientIds = participantIds.filter((userId) => userId !== senderId);

    if (!conversationSnapshot.exists || candidateRecipientIds.length === 0) {
      logger.warn("Skipping notification without recipients", {conversationId, messageId});
      return;
    }

    const recipientIds = await recipientsWithoutBlocks(senderId, candidateRecipientIds);
    if (recipientIds.length === 0) return;

    const [senderSnapshot, tokenTargets] = await Promise.all([
      db.collection("users").doc(senderId).get(),
      loadTokenTargets(recipientIds),
    ]);

    if (tokenTargets.length === 0) return;

    const senderName = stringValue(senderSnapshot.get("displayName")) || "ContactMe user";
    const conversationType = stringValue(conversationSnapshot.get("type")) || "direct";
    const isGroup = conversationType === "group";
    const title = isGroup ? stringValue(conversationSnapshot.get("title")) || "Group" : senderName;
    const photoUrl = isGroup ?
      stringValue(conversationSnapshot.get("photoUrl")) :
      stringValue(senderSnapshot.get("photoUrl"));
    const messageBody = notificationBody(message);
    const body = isGroup ? `${senderName}: ${messageBody}` : messageBody;

    for (const tokenChunk of chunks(tokenTargets, 500)) {
      const response = await messaging.sendEachForMulticast({
        tokens: tokenChunk.map((target) => target.token),
        data: {
          type: "message",
          conversationId,
          conversationType,
          messageId,
          title,
          body,
          photoUrl,
        },
        android: {
          priority: "high",
          ttl: 24 * 60 * 60 * 1000,
        },
      });

      await deleteInvalidTokens(tokenChunk, response.responses);
      logger.info("Message notification fanout complete", {
        conversationId,
        messageId,
        successCount: response.successCount,
        failureCount: response.failureCount,
      });
    }
  },
);

async function recipientsWithoutBlocks(
  senderId: string,
  recipientIds: string[],
): Promise<string[]> {
  const checks = recipientIds.flatMap((recipientId) => [
    db.collection("blocked_users").doc(senderId).collection("items").doc(recipientId),
    db.collection("blocked_users").doc(recipientId).collection("items").doc(senderId),
  ]);
  const snapshots = await db.getAll(...checks);

  return recipientIds.filter((_, index) => {
    const senderBlockedRecipient = snapshots[index * 2]?.exists === true;
    const recipientBlockedSender = snapshots[index * 2 + 1]?.exists === true;
    return !senderBlockedRecipient && !recipientBlockedSender;
  });
}

async function loadTokenTargets(userIds: string[]): Promise<TokenTarget[]> {
  const snapshots = await Promise.all(
    userIds.map((userId) =>
      db.collection("user_devices").doc(userId).collection("devices").get(),
    ),
  );

  return snapshots.flatMap((snapshot) =>
    snapshot.docs.map((document) => ({
      token: stringValue(document.get("token")),
      reference: document.ref,
    })).filter((target) => target.token.length > 0),
  );
}

async function deleteInvalidTokens(
  targets: TokenTarget[],
  responses: Array<{success: boolean; error?: {code?: string}}>,
): Promise<void> {
  const invalidReferences = responses.flatMap((response, index) => {
    const code = response.error?.code;
    const isInvalid = code === "messaging/invalid-registration-token" ||
      code === "messaging/registration-token-not-registered";
    return !response.success && isInvalid ? [targets[index].reference] : [];
  });

  await Promise.all(invalidReferences.map((reference) => reference.delete()));
}

function notificationBody(message: Record<string, unknown>): string {
  if (stringValue(message.type) === "image") return "Photo";
  if (stringValue(message.type) === "document") {
    return stringValue(message.fileName) || "Document";
  }

  const text = stringValue(message.text).trim();
  if (!text) return "New message";
  return text.length <= 120 ? text : `${text.slice(0, 117)}...`;
}

function stringValue(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function stringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === "string") : [];
}

function isSafeCallId(value: string): boolean {
  return /^[A-Za-z0-9_-]{8,128}$/.test(value);
}

function isSafeRoomId(value: string): boolean {
  return /^[A-Za-z0-9_-]{8,128}$/.test(value);
}

function timestampMillis(value: unknown): number {
  if (typeof value !== "object" || value === null || !("toMillis" in value)) return 0;
  const toMillis = (value as {toMillis?: unknown}).toMillis;
  return typeof toMillis === "function" ? toMillis.call(value) : 0;
}

function chunks<T>(items: T[], size: number): T[][] {
  const result: T[][] = [];
  for (let index = 0; index < items.length; index += size) {
    result.push(items.slice(index, index + size));
  }
  return result;
}
