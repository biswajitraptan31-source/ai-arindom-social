import { execFileSync } from "node:child_process";
import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const PROJECT_ID = "arindom-neon-x";
const API_KEY = process.env.FIREBASE_API_KEY;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const BOT_EMAIL = process.env.NEON_AI_EMAIL;
const BOT_PASSWORD = process.env.NEON_BOT_PASSWORD;
const GITHUB_TOKEN = process.env.GITHUB_TOKEN;
const GITHUB_REPOSITORY = process.env.GITHUB_REPOSITORY;

for (const [name, value] of Object.entries({
  API_KEY, GEMINI_API_KEY, BOT_EMAIL, BOT_PASSWORD, GITHUB_TOKEN, GITHUB_REPOSITORY
})) {
  if (!value) throw new Error("Missing required environment variable: " + name);
}

async function requestJson(url, body, headers = {}) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(60000)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error("HTTP " + response.status + ": " +
      (data.message || data.error?.message || data.error?.status || "Request failed"));
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
const hourSlot = Math.floor(Date.now() / 3600000);
const isVideo = hourSlot % 2 === 0;
const topic = topics[hourSlot % topics.length];
const prompt = isVideo
  ? "Write a short original voiceover-style caption for an 8-second vertical animated social video for Arindom Neon X. Topic: " + topic + ". Use 25 to 45 words, simple language, one clear idea, and at most 2 emojis. All ages. No markdown, no hashtags, no quotes from famous people. Return only the caption."
  : "Write one original social-media post for the official AI account of Arindom Neon X. Topic: " + topic + ". Keep it friendly, useful, accurate, and suitable for all ages. Use 45-100 words, plain text, at most 2 relevant emojis. Do not claim personal experiences, invent breaking news, give dangerous instructions, or include markdown headings. Return only the post text.";

async function generateText() {
  const models = ["gemini-3.8-flash", "gemini-3.5-flash-lite"];
  let lastError;
  for (const model of models) {
    for (let attempt = 1; attempt <= 3; attempt++) {
      try {
        const data = await requestJson(
          "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent",
          { contents: [{ parts: [{ text: prompt }] }] },
          { "x-goog-api-key": GEMINI_API_KEY }
        );
        const text = (data.candidates || [])
          .flatMap(candidate => candidate.content?.parts || [])
          .filter(part => typeof part.text === "string")
          .map(part => part.text).join("\n").trim();
        if (!text || text.length > 2200) throw new Error("Gemini returned empty or too-long text.");
        return text;
      } catch (error) {
        lastError = error;
        const message = String(error.message || error);
        const temporary = /HTTP (429|500|502|503|504)/.test(message) ||
          /high demand|temporar|overloaded|unavailable|fetch failed|timeout|UND_ERR/i.test(message);
        if (!temporary) throw error;
        if (attempt < 3) {
          const waitMs = attempt * 4000;
          console.log(model + " temporarily busy; retrying in " + (waitMs / 1000) + "s.");
          await new Promise(resolve => setTimeout(resolve, waitMs));
        }
      }
    }
    console.log(model + " remained busy; trying fallback model.");
  }
  throw lastError || new Error("All Gemini models are temporarily unavailable.");
}

function wrapText(text, maxChars) {
  const words = text.replace(/\s+/g, " ").trim().split(" ");
  const lines = [];
  let line = "";
  for (const word of words) {
    if (line && (line + " " + word).length > maxChars) {
      lines.push(line);
      line = word;
    } else {
      line = line ? line + " " + word : word;
    }
  }
  if (line) lines.push(line);
  return lines.join("\n");
}

async function makeVideo(caption) {
  const captionPath = path.join(process.env.RUNNER_TEMP || "/tmp", "neon-ai-caption.txt");
  const videoPath = path.join(process.env.RUNNER_TEMP || "/tmp", "neon-ai-" + Date.now() + ".mp4");
  await writeFile(captionPath, wrapText(caption, 16), "utf8");

  const font = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf";
  const filters = [
    "drawbox=x='mod(t*90\\,w+220)-220':y=180:w=220:h=220:color=0x6d5dfc@0.30:t=fill",
    "drawbox=x='w-mod(t*70\\,w+180)':y=900:w=180:h=180:color=0x06b6d4@0.22:t=fill",
    "drawtext=fontfile=" + font + ":text='NEON AI  •  ORIGINAL':fontcolor=0x9aa7ff:fontsize=24:x=(w-text_w)/2:y=260",
    "drawtext=fontfile=" + font + ":textfile=" + captionPath + ":fontcolor=white:fontsize=36:line_spacing=14:x=(w-text_w)/2:y=(h-text_h)/2:borderw=1:bordercolor=black@0.3",
    "drawtext=fontfile=" + font + ":text='ARINDOM NEON X':fontcolor=0x9aa7ff:fontsize=22:x=(w-text_w)/2:y=1100"
  ].join(",");
  execFileSync("ffmpeg", [
    "-hide_banner", "-loglevel", "error", "-y",
    "-f", "lavfi", "-i", "color=c=0x0b1026:s=720x1280:r=24:d=8",
    "-f", "lavfi", "-i",
    "aevalsrc=0.035*sin(2*PI*220*t)+0.025*sin(2*PI*277*t)+0.02*sin(2*PI*330*t):s=44100:d=8",
    "-vf", filters,
    "-map", "0:v:0", "-map", "1:a:0",
    "-c:v", "libx264", "-preset", "veryfast", "-crf", "27",
    "-pix_fmt", "yuv420p", "-c:a", "aac", "-b:a", "64k",
    "-shortest", "-movflags", "+faststart", videoPath
  ], { stdio: "inherit" });
  return videoPath;
}

async function publishVideoAsset(videoPath) {
  const apiRoot = "https://api.github.com/repos/" + GITHUB_REPOSITORY;
  const headers = {
    Authorization: "Bearer " + GITHUB_TOKEN,
    Accept: "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28"
  };
  const tag = "neon-ai-video-" + Date.now();
  const releaseResponse = await fetch(apiRoot + "/releases", {
    method: "POST",
    headers: { ...headers, "Content-Type": "application/json" },
    body: JSON.stringify({
      tag_name: tag,
      name: "Neon AI Video",
      body: "Automatically generated original animated video for Arindom Neon X.",
      draft: false,
      prerelease: false
    })
  });
  const release = await releaseResponse.json().catch(() => ({}));
  if (!releaseResponse.ok) {
    throw new Error("Could not create video release: HTTP " + releaseResponse.status +
      " " + (release.message || ""));
  }

  const bytes = await readFile(videoPath);
  const uploadUrl = release.upload_url.replace(/\{.*$/, "") +
    "?name=" + encodeURIComponent(path.basename(videoPath));
  const uploadResponse = await fetch(uploadUrl, {
    method: "POST",
    headers: { ...headers, "Content-Type": "video/mp4" },
    body: bytes
  });
  const asset = await uploadResponse.json().catch(() => ({}));
  if (!uploadResponse.ok || !asset.browser_download_url) {
    throw new Error("Video generated but GitHub asset upload failed: HTTP " +
      uploadResponse.status + " " + (asset.message || ""));
  }
  console.log("Neon AI video asset hosted:", asset.browser_download_url);
  return asset.browser_download_url;
}

const text = await generateText();
let mediaUrl = "";
let mediaType = "";
let art = "AI GENERATED";

if (isVideo) {
  const videoPath = await makeVideo(text);
  mediaUrl = await publishVideoAsset(videoPath);
  mediaType = "video/mp4";
  art = "NEON AI VIDEO";
} else {
  art = "AI GENERATED";
}

const auth = await requestJson(
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + encodeURIComponent(API_KEY),
  { email: BOT_EMAIL, password: BOT_PASSWORD, returnSecureToken: true }
);

const fields = {
  uid: { stringValue: auth.localId },
  user: { stringValue: "Neon AI" },
  handle: { stringValue: "@neon.ai" },
  text: { stringValue: text },
  art: { stringValue: art },
  mediaUrl: { stringValue: mediaUrl },
  mediaType: { stringValue: mediaType },
  likes: { integerValue: "0" },
  likedBy: { arrayValue: { values: [] } },
  savedBy: { arrayValue: { values: [] } },
  createdAt: { stringValue: new Date().toISOString() },
  aiGenerated: { booleanValue: true }
};

const docUrl = new URL(
  "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID +
  "/databases/(default)/documents/posts"
);
docUrl.searchParams.set("documentId", "neon-ai-" + Date.now());
const saved = await requestJson(docUrl.toString(), { fields }, {
  Authorization: "Bearer " + auth.idToken
});

console.log("Neon AI " + (isVideo ? "video" : "text post") + " published:", saved.name);
console.log(text);
