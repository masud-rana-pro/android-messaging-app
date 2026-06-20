# ধাপ ৮৮: Free WebRTC engine ও secure TURN config

## কী সরানো হয়েছে

ZEGOCLOUD Token04, Firebase callable function, ServerSecret flow এবং Android Firebase Functions dependency সরানো হয়েছে। No-card architecture এখন pure WebRTC + Firestore signaling।

## ICE server config

Google STUN সবসময় থাকে: `stun:stun.l.google.com:19302`। `apps/ContactMe/webrtc.properties`-এ TURN URL, username ও password তিনটিই থাকলে Metered.ca TURN যোগ হয়। কোনো value না থাকলে app শুধু STUN config নিয়ে compile/run করতে পারে।

## Local config

`webrtc.properties.example` copy করে `webrtc.properties` বানাতে হবে। Local file Git-ignored। Real credential example file বা Kotlin source-এ লেখা যাবে না। BuildConfig source-control leakage বন্ধ করে, তবে APK reverse engineering থেকে static TURN credential পুরোপুরি রক্ষা করে না—এটি personal-beta limitation।

## Engine foundation

`WebRtcEngineFactory` একবার WebRTC initialize করে, EGL context সহ video encoder/decoder factory বানায় এবং configured `PeerConnection` তৈরি করতে পারে। Call UI/tracks/offer-answer orchestration Step 89+ scope হওয়ায় এখানে যোগ করা হয়নি।

## Audit correction summary

Step 82–88 এখন Firebase Auth, Firestore signaling, WebRTC, Google STUN, optional local Metered TURN এবং future Cloudflare Worker + FCM plan অনুসরণ করে। Step 85/86 implementation অপরিবর্তিত রাখা হয়েছে।
