export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return new Response("Method Not Allowed", { status: 405 });
    }

    try {
      const { callId, receiverId } = await request.json();
      if (!callId || !receiverId) {
        return new Response("Missing parameters", { status: 400 });
      }

      const accessToken = await getAccessToken(env);
      const fcmToken = await getReceiverFcmToken(env, receiverId);

      if (!fcmToken) {
        return new Response("Receiver token not found", { status: 404 });
      }

      await sendFcmNotification(env, accessToken, fcmToken, callId, receiverId);

      return new Response("Notification sent", { status: 200 });
    } catch (error) {
      console.error("Worker error:", error);
      return new Response("Internal Server Error", { status: 500 });
    }
  }
};

async function getReceiverFcmToken(env, receiverId) {
  const url = `https://firestore.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/databases/(default)/documents/users/${receiverId}`;
  const response = await fetch(url);
  if (!response.ok) return null;

  const data = await response.json();
  return data.fields?.fcmToken?.stringValue;
}

async function sendFcmNotification(env, accessToken, fcmToken, callId, receiverId) {
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
  const privateKey = env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n');
  const scope = "https://www.googleapis.com/auth/firebase.messaging";

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
  return data.access_token;
}

function b64(str) {
  return btoa(str).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
}

async function sign(str, key) {
  const pemHeader = "-----BEGIN PRIVATE KEY-----";
  const pemFooter = "-----END PRIVATE KEY-----";
  const pemContents = key.substring(pemHeader.length, key.length - pemFooter.length).replace(/\s/g, "");
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
