const PROJECT_ID = "arindom-neon-x";
const API_KEY = process.env.FIREBASE_API_KEY;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const BOT_EMAIL = process.env.NEON_AI_EMAIL;
const BOT_PASSWORD = process.env.NEON_AI_PASSWORD;

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
    throw new Error(data.error?.message || data.error?.status || `HTTP ${response.status}`);
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

const generated = await postJson(
  "https://generativelanguage.googleapis.com/v1/interactions",
  {
    model: "gemini-3.8-flash",
    input: `Write one original social-media post for the official AI account of Arindom Neon X. Topic: ${topic}. Keep it friendly, useful, accurate, and suitable for all ages. Use 45-100 words, plain text, at most 2 relevant emojis. Do not claim personal experiences, invent breaking news, give dangerous instructions, or include markdown headings. Return only the post text.`
  },
  { "x-goog-api-key": GEMINI_API_KEY }
);

const postText = (generated.steps || [])
  .filter(step => step.type === "model_output")
  .flatMap(step => step.content || [])
  .filter(part => part.type === "text" && typeof part.text === "string")
  .map(part => part.text)
  .join("\n")
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
