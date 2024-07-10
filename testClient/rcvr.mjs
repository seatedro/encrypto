/**
 * @param {string} username
 */
async function fetchPublicKey(username) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/users/${username}/public_key`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      },
    );
    if (!response.ok) {
      throw new Error("Public key retrieval failed");
    }
    const publicKey = (await response.json()).publicKey;
    const publicKeyArrayBuffer = Uint8Array.from(atob(publicKey), (c) =>
      c.charCodeAt(0),
    ).buffer;
    return publicKeyArrayBuffer;
  } catch (error) {
    console.error("Error fetching public key:", error);
    return null;
  }
}

async function generateECDHKeyPair() {
  return window.crypto.subtle.generateKey(
    {
      name: "ECDH",
      namedCurve: "P-384",
    },
    true,
    ["deriveKey"],
  );
}

const ecdhKeyPair = await generateECDHKeyPair();

let publicKey = await window.crypto.subtle.exportKey(
  "raw",
  ecdhKeyPair.publicKey,
);

const publicKeyString = btoa(String.fromCharCode(...new Uint8Array(publicKey)));

/**
 * @param {Uint8Array} array
 * @returns {ArrayBuffer}
 */
function typedArrayToBuffer(array) {
  return array.buffer.slice(
    array.byteOffset,
    array.byteLength + array.byteOffset,
  );
}

/**
 * @param {string} serializedData
 * @param {CryptoKey} aesKey
 * @returns {Promise<string>}
 */
async function decryptMessage(serializedData, aesKey) {
  const decodedData = atob(serializedData);
  const dataArray = new Uint8Array(decodedData.length);
  for (let i = 0; i < decodedData.length; i++) {
    dataArray[i] = decodedData.charCodeAt(i);
  }

  const iv = dataArray.slice(0, 12); // Extract the IV (first 12 bytes)
  console.log("IV: ", iv);
  const encryptedData = dataArray.slice(12); // Extract the encrypted data
  const buffer = typedArrayToBuffer(encryptedData);
  console.log("Encrypted Data: ", buffer);
  const aesKeyBytes = await window.crypto.subtle.exportKey("raw", aesKey);
  console.log("AES key bytes:", aesKeyBytes);

  const decryptedData = await window.crypto.subtle.decrypt(
    { name: "AES-GCM", iv: iv },
    aesKey,
    buffer,
  );

  const decoder = new TextDecoder();
  return decoder.decode(decryptedData);
}

async function deriveSharedSecret(privateKey, receiverPublicKeyArrayBuffer) {
  const receiverPublicKey = await window.crypto.subtle.importKey(
    "raw",
    receiverPublicKeyArrayBuffer,
    { name: "ECDH", namedCurve: "P-384" },
    false,
    [],
  );
  const sharedSecret = await window.crypto.subtle.deriveKey(
    { name: "ECDH", public: receiverPublicKey },
    privateKey,
    { name: "AES-GCM", length: 256 },
    true,
    ["encrypt", "decrypt"],
  );
  return sharedSecret;
}

const register = await fetch("http://localhost:8080/api/auth/register", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    username: "alan123",
    password: btoa("password"),
    dateOfBirth: "1999-06-03",
    publicKey: publicKeyString,
  }),
});

const registerResponse = await register.json();
console.log("Register Response: ", registerResponse);

const login = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  credentials: "include",
  body: JSON.stringify({
    username: "alan123",
    password: btoa("password"),
  }),
});

const loginResponse = await login.json();
console.log("Login Response: ", loginResponse);

const socket = new WebSocket("ws://localhost:8080/encrypto");

socket.onopen = () => {
  // Send username for registration
  socket.send("alan123");
};

socket.onmessage = async (event) => {
  try {
    const message = JSON.parse(event.data);
    console.log("Message: ", message);
    const publicKeyBuffer = await fetchPublicKey(message.senderId);
    console.log("Public Key: ", publicKeyBuffer);
    const aesKey = await deriveSharedSecret(
      ecdhKeyPair.privateKey,
      publicKeyBuffer,
    );
    const encryptedMessage = message.content;
    const decryptedMessage = await decryptMessage(encryptedMessage, aesKey);
    console.log("Decrypted Message: ", decryptedMessage);
  } catch (error) {
    console.error("Error decrypting message: ", error);
  }
};

receiverClient.activate();
