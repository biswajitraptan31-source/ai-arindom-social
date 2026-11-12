# ARINDOM NEON X — Full Feature Roadmap

This file tracks the complete product scope. A feature is **not complete** until its backend, security rules, UI, and real-device tests pass. A screen or placeholder alone does not count as a working feature.

## Existing core scope (retain all)
- [ ] Username-based private messaging, image sharing, online presence, read receipts
- [ ] Reels: upload, vertical feed, likes, comments
- [ ] Stories: 24-hour expiry, viewer list, reactions
- [ ] AI assistant: chat, captions, hashtags, post ideas, content creation
- [ ] Creator tools: analytics, scheduling, creator profiles and insights
- [ ] Notifications: likes, comments, follows, messages
- [ ] Security: Firestore/Storage rules, spam protection, reports, blocks, performance
- [ ] Authentication, password reset, profile, follow/unfollow, explore, search, post interactions, account switching

## Additional requested features

### Social and communication
- [ ] Live streaming — streaming provider, broadcaster/participant controls, moderation, reporting, network recovery
- [ ] Voice calls — call invitations, accept/reject, mute, speaker, call state and disconnect handling
- [ ] Video calls — camera/microphone permissions, call controls, reconnect and end-call handling
- [ ] Group chats — group creation, member roles, add/remove members, group media and notifications

### Discovery and creator experience
- [ ] Music library for Reels — licensed/approved audio catalog, attribution, trim/volume controls and copyright-safe publishing
- [ ] Advanced search — users, posts, Reels, hashtags and filters with indexed queries
- [ ] AI video creation — prompt workflow, generation provider, job status, moderation, output storage and usage limits
- [ ] Verified badges — application/review workflow and server-controlled badge assignment
- [ ] Private accounts — follow requests, approval/decline, and private-content access checks

### Safety and accessibility
- [ ] Content moderation — report queues, moderation states, automated checks and human review path
- [ ] Multiple languages — translated interface strings, language selector and locale-aware dates/numbers

## Required platform work
- [ ] Media hosting for images, audio and video with upload limits and secure access
- [ ] Trusted backend/API for privileged actions and AI provider credentials (never put secret keys in the Android/Web client)
- [ ] Push notification delivery and device-token lifecycle
- [ ] Real-time presence/call signaling and a supported calling/streaming provider
- [ ] Firestore indexes and least-privilege rules for every new collection
- [ ] Rate limits, abuse prevention, block/report enforcement, privacy and retention controls
- [ ] Accessibility, low-bandwidth behavior, loading/error/empty states
- [ ] Automated build plus emulator/real-device testing

## Suggested implementation order
1. Security and data model foundations: private accounts, blocks/reports, moderation states, rate limits.
2. Messaging completion: presence, image sharing, group chats, push notifications.
3. Media foundations: reliable image/video hosting, Reels, Stories, music licensing.
4. Calls and live streaming using a dedicated real-time provider.
5. AI assistant and AI video generation through a secure backend.
6. Creator analytics/scheduling, verification workflow, multilingual UI.
7. Performance, accessibility, regression and real-device acceptance tests.

## Current implementation notes
The current app is an Android WebView shell with Firebase Authentication and Firestore REST integration. Some existing UI and flows are present, but several items above require external services, configuration, secure backend work, and testing. Do not label planned items as shipped or working until verified.
