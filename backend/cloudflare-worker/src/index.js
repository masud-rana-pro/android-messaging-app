export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return new Response("Method Not Allowed", { status: 405 });
    }

    try {
      const payload = await request.json();
      const accessToken = await getAccessToken(env);
      if (payload.type === "message") {
        return await handleMessageNotification(env, accessToken, payload);
      }
      const { callId, receiverId } = payload;
      if (!callId || !receiverId) return new Response("Missing parameters", { status: 400 });
      const call = await getFirestoreDocument(env, accessToken, `calls/${callId}`);
      const callReceiverId = call?.fields?.receiverId?.stringValue;
      const callStatus = call?.fields?.status?.stringValue;
      if (callReceiverId !== receiverId || callStatus !== "ringing") {
        return new Response("Invalid call", { status: 403 });
      }

      const fcmTokens = await getReceiverFcmTokens(env, accessToken, receiverId);

      if (fcmTokens.length === 0) {
        return new Response("Receiver token not found", { status: 404 });
      }

      const results = await Promise.allSettled(
        fcmTokens.map((token) => sendCallFcmNotification(env, accessToken, token, callId, receiverId))
      );
      if (!results.some((result) => result.status === "fulfilled")) {
        throw new Error("FCM rejected every registered receiver device");
      }

      return Response.json({ sent: results.filter((result) => result.status === "fulfilled").length });
    } catch (error) {
      console.error("Worker error:", error);
      return new Response("Internal Server Error", { status: 500 });
    }
  }
};

async function getFirestoreDocument(env, accessToken, documentPath) {
  const url = `https://firestore.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/databases/(default)/documents/${documentPath}`;
  const response = await fetch(url, {
    headers: { "Authorization": `Bearer ${accessToken}` }
  });
  if (!response.ok) return null;
  return response.json();
}

async function getReceiverFcmTokens(env, accessToken, receiverId) {
  const url = `https://firestore.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/databases/(default)/documents/user_devices/${receiverId}/devices`;
  const response = await fetch(url, {
    headers: { "Authorization": `Bearer ${accessToken}` }
  });
  if (!response.ok) return [];

  const data = await response.json();
  const deviceTokens = (data.documents || [])
    .map((document) => document.fields?.token?.stringValue)
    .filter(Boolean);
  if (deviceTokens.length > 0) return [...new Set(deviceTokens)];

  const user = await getFirestoreDocument(env, accessToken, `users/${receiverId}`);
  const legacyToken = user?.fields?.fcmToken?.stringValue;
  return legacyToken ? [legacyToken] : [];
}

async function sendCallFcmNotification(env, accessToken, fcmToken, callId, receiverId) {
  const url = `https://fcm.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/messages:send`;
  const payload = {
    message: {
      token: fcmToken,
      data: {
        type: "incoming_call",
        callId: callId,
        receiverId: receiverId
      },
      android: {
        priority: "high"
      }
    }
  };

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`FCM error: ${errorBody}`);
  }
}

async function getAccessToken(env) {
  const email = env.FIREBASE_CLIENT_EMAIL;
  const privateKey = String(env.FIREBASE_PRIVATE_KEY)
    .trim()
    .replace(/^['"]|['"]$/g, "")
    .replace(/\\n/g, "\n");
  const scope = [
    "https://www.googleapis.com/auth/firebase.messaging",
    "https://www.googleapis.com/auth/datastore"
  ].join(" ");

  const header = { alg: "RS256", typ: "JWT" };
  const now = Math.floor(Date.now() / 1000);
  const claim = {
    iss: email,
    scope: scope,
    aud: "https://oauth2.googleapis.com/token",
    exp: now + 3600,
    iat: now
  };

  const encodedHeader = b64(JSON.stringify(header));
  const encodedClaim = b64(JSON.stringify(claim));
  const tokenToSign = `${encodedHeader}.${encodedClaim}`;

  const signature = await sign(tokenToSign, privateKey);
  const jwt = `${tokenToSign}.${signature}`;

  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`
  });

  const data = await response.json();
  if (!response.ok || !data.access_token) {
    throw new Error(`OAuth token exchange failed (${response.status}): ${data.error || "unknown"}`);
  }
  return data.access_token;
}

async function handleMessageNotification(env, accessToken, payload) {
  const { conversationId, messageId, senderId } = payload;
  if (!conversationId || !messageId || !senderId) return new Response("Missing message parameters", { status: 400 });

  const conversation = await getFirestoreDocument(env, accessToken, `conversations/${conversationId}`);
  const message = await getFirestoreDocument(env, accessToken, `conversations/${conversationId}/messages/${messageId}`);
  if (!conversation || !message) return new Response("Message not found", { status: 404 });

  const messageSenderId = stringField(message, "senderId");
  const participantIds = arrayStringField(conversation, "participantIds");
  const createdAt = Date.parse(message?.fields?.createdAt?.timestampValue || "");
  const isFresh = Number.isFinite(createdAt) && Math.abs(Date.now() - createdAt) <= 5 * 60 * 1000;
  if (messageSenderId !== senderId || !participantIds.includes(senderId) || !isFresh) {
    return new Response("Invalid message", { status: 403 });
  }

  const sender = await getFirestoreDocument(env, accessToken, `users/${senderId}`);
  const senderName = stringField(sender, "displayName") || stringField(sender, "username") || "ContactMe user";
  const senderPhoto = stringField(sender, "photoUrl");
  const conversationType = stringField(conversation, "type") || "direct";
  const title = conversationType === "group" ? (stringField(conversation, "title") || "Group") : senderName;
  const photoUrl = conversationType === "group" ? stringField(conversation, "photoUrl") : senderPhoto;
  const body = messageNotificationBody(message, senderName, conversationType);
  const receiverIds = participantIds.filter((id) => id !== senderId);

  const tokenGroups = await Promise.all(receiverIds.map((id) => getReceiverFcmTokens(env, accessToken, id)));
  const tokens = [...new Set(tokenGroups.flat())];
  if (tokens.length === 0) return new Response("Receiver token not found", { status: 404 });

  const results = await Promise.allSettled(tokens.map((token) => sendMessageFcmNotification(
    env, accessToken, token, { conversationId, conversationType, title, photoUrl, body, messageId }
  )));
  if (!results.some((result) => result.status === "fulfilled")) throw new Error("FCM rejected every message notification");
  return Response.json({ sent: results.filter((result) => result.status === "fulfilled").length });
}

function messageNotificationBody(message, senderName, conversationType) {
  const type = stringField(message, "type");
  const prefix = conversationType === "group" ? `${senderName}: ` : "";
  if (type === "image") return `${prefix}Photo`;
  if (type === "voice") return `${prefix}Voice message`;
  if (type === "document") return `${prefix}${stringField(message, "fileName") || "Document"}`;
  const text = stringField(message, "text") || "New message";
  return `${prefix}${text.slice(0, 180)}`;
}

async function sendMessageFcmNotification(env, accessToken, fcmToken, data) {
  const url = `https://fcm.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/messages:send`;
  const response = await fetch(url, {
    method: "POST",
    headers: { "Authorization": `Bearer ${accessToken}`, "Content-Type": "application/json" },
    body: JSON.stringify({
      message: {
        token: fcmToken,
        data: {
          type: "message",
          conversationId: data.conversationId,
          conversationType: data.conversationType,
          title: data.title,
          photoUrl: data.photoUrl || "",
          body: data.body,
          messageId: data.messageId
        },
        android: { priority: "high" }
      }
    })
  });
  if (!response.ok) throw new Error(`FCM message error: ${await response.text()}`);
}

function stringField(document, name) {
  return document?.fields?.[name]?.stringValue || "";
}

function arrayStringField(document, name) {
  return (document?.fields?.[name]?.arrayValue?.values || []).map((value) => value.stringValue).filter(Boolean);
}

function b64(str) {
  return btoa(str).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
}

async function sign(str, key) {
  const pemHeader = "-----BEGIN PRIVATE KEY-----";
  const pemFooter = "-----END PRIVATE KEY-----";
  const pemContents = key
    .replace(pemHeader, "")
    .replace(pemFooter, "")
    .replace(/[^A-Za-z0-9+/=]/g, "");
  const binaryDerString = atob(pemContents);
  const binaryDer = new Uint8Array(binaryDerString.length);
  for (let i = 0; i < binaryDerString.length; i++) {
    binaryDer[i] = binaryDerString.charCodeAt(i);
  }

  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryDer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    new TextEncoder().encode(str)
  );

  return b64(String.fromCharCode(...new Uint8Array(signature)));
}
