import {initializeApp} from "firebase-admin/app";
import {DocumentReference, getFirestore} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";
import {logger} from "firebase-functions";
import {onDocumentCreated} from "firebase-functions/v2/firestore";

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

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

function chunks<T>(items: T[], size: number): T[][] {
  const result: T[][] = [];
  for (let index = 0; index < items.length; index += size) {
    result.push(items.slice(index, index + size));
  }
  return result;
}
