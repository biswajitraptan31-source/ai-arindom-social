const PROJECT_ID = "arindom-neon-x";
const API_KEY = process.env.FIREBASE_API_KEY;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const BOT_EMAIL = process.env.NEON_AI_EMAIL;
const BOT_PASSWORD = process.env.NEON_BOT_PASSWORD;

for (const [name, value] of Object.entries({API_KEY, GEMINI_API_KEY, BOT_EMAIL, BOT_PASSWORD})) {
  if (!value) throw new Error(`Missing required environment variable: ${name}`);
}

async function postJson(url, body, headers = {}) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${data.error?.message || data.error?.status || "Request failed"}`);
  }
  return data;
}

const topics = [
  "a surprising but accurate technology or science fact",
  "a useful artificial intelligence tip for beginners",
  "a short original motivational thought, not a famous quote",
  "a clean, friendly technology joke",
  "a creative idea people can try today",
  "a practical beginner-friendly app development tip"
];
const hourSeed = Math.floor(Date.now() / 3600000);
const topic = topics[hourSeed % topics.length];

const prompt = `Write one original social-media post for the official AI account of Arindom Neon X. Topic: ${topic}. Keep it friendly, useful, accurate, and suitable for all ages. Use 45-100 words, plain text, at most 2 relevant emojis. Do not claim personal experiences, invent breaking news, give dangerous instructions, or include markdown headings. Return only the post text.`;

async function generatePost() {
  const models = ["gemini-3.8-flash", "gemini-3.5-flash-lite"];
  let lastError;
  for (const model of models) {
    for (let attempt = 1; attempt <= 3; attempt++) {
      try {
        return await postJson(
          `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
          { contents: [{ parts: [{ text: prompt }] }] },
          { "x-goog-api-key": GEMINI_API_KEY }
        );
      } catch (error) {
        lastError = error;
        const message = String(error.message || error);
        const temporary = /HTTP (429|500|502|503|504)/.test(message) ||
          /high demand|temporar|overloaded|unavailable/i.test(message);
        if (!temporary) throw error;
        if (attempt < 3) {
          const waitMs = attempt * 4000;
          console.log(`${model} temporarily busy; retrying in ${waitMs / 1000}s (attempt ${attempt}/3).`);
          await new Promise(resolve => setTimeout(resolve, waitMs));
        }
      }
    }
    console.log(`${model} remained busy; trying fallback model.`);
  }
  throw lastError || new Error("All Gemini models are temporarily unavailable.");
}

const generated = await generatePost();

const postText = (generated.candidates || [])
  .flatMap(candidate => candidate.content?.parts || [])
  .filter(part => typeof part.text === "string")
  .map(part => part.text)
  .join("\\n")
  .trim();

if (!postText || postText.length > 2200) {
  throw new Error("Gemini returned an empty or too-long post.");
}

const auth = await postJson(
  `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${encodeURIComponent(API_KEY)}`,
  { email: BOT_EMAIL, password: BOT_PASSWORD, returnSecureToken: true }
);

const fields = {
  uid: { stringValue: auth.localId },
  user: { stringValue: "Neon AI" },
  handle: { stringValue: "@neon.ai" },
  text: { stringValue: postText },
  art: { stringValue: "AI GENERATED" },
  createdAt: { stringValue: new Date().toISOString() },
  aiGenerated: { booleanValue: true }
};

const docUrl = new URL(`https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/posts`);
docUrl.searchParams.set("documentId", `neon-ai-${Date.now()}`);
const saved = await postJson(docUrl.toString(), { fields }, {
  Authorization: `Bearer ${auth.idToken}`
});

console.log("Neon AI post published:", saved.name);
console.log(postText);
