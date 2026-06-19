import {createCipheriv, randomBytes, randomInt} from "node:crypto";

type TokenInfo = {
  app_id: number;
  user_id: string;
  nonce: number;
  ctime: number;
  expire: number;
  payload: string;
};

export function generateZegoToken04(
  appId: number,
  userId: string,
  serverSecret: string,
  effectiveTimeSeconds: number,
  payload: string,
): string {
  if (!Number.isSafeInteger(appId) || appId <= 0) throw new Error("Invalid ZEGO app id");
  if (!userId || Buffer.byteLength(userId, "utf8") > 64) throw new Error("Invalid ZEGO user id");
  if (Buffer.byteLength(serverSecret, "utf8") !== 32) {
    throw new Error("ZEGO server secret must be 32 bytes");
  }
  if (!Number.isInteger(effectiveTimeSeconds) || effectiveTimeSeconds <= 0) {
    throw new Error("Invalid ZEGO token lifetime");
  }

  const nowSeconds = Math.floor(Date.now() / 1000);
  const tokenInfo: TokenInfo = {
    app_id: appId,
    user_id: userId,
    nonce: randomInt(0, 2147483647),
    ctime: nowSeconds,
    expire: nowSeconds + effectiveTimeSeconds,
    payload,
  };
  const iv = randomBytes(8).toString("hex");
  const key = Buffer.from(serverSecret, "utf8");
  const cipher = createCipheriv("aes-256-cbc", key, Buffer.from(iv, "utf8"));
  const encrypted = Buffer.concat([
    cipher.update(JSON.stringify(tokenInfo), "utf8"),
    cipher.final(),
  ]);
  const expiry = Buffer.alloc(8);
  expiry.writeBigInt64BE(BigInt(tokenInfo.expire));
  const ivLength = Buffer.alloc(2);
  ivLength.writeUInt16BE(Buffer.byteLength(iv));
  const encryptedLength = Buffer.alloc(2);
  encryptedLength.writeUInt16BE(encrypted.length);

  return "04" + Buffer.concat([
    expiry,
    ivLength,
    Buffer.from(iv, "utf8"),
    encryptedLength,
    encrypted,
  ]).toString("base64");
}
