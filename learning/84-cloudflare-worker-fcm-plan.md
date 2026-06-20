# ধাপ ৮৪: Cloudflare Worker + FCM no-card plan

Spark plan-এ Firebase Cloud Functions ব্যবহার না করার সিদ্ধান্ত নেওয়া হয়েছে। তাই Functions project/config এবং Android callable dependency সরানো হয়েছে।

ভবিষ্যতে Cloudflare Worker authenticated request গ্রহণ করে Firebase membership যাচাই করবে এবং FCM HTTP v1 data payload পাঠাবে। Service-account key/OAuth credential Worker secret হিসেবে থাকবে। Android থেকে সরাসরি FCM HTTP v1 call করা নিরাপদ নয়, কারণ APK থেকে privileged credential বের করা যায়।

এই step শুধু future notification boundary নথিভুক্ত করেছে; Worker implementation Step 89+ scope-এ ঢোকানো হয়নি।
