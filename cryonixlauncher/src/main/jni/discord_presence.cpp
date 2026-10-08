#define DISCORDPP_IMPLEMENTATION
#include <discordpp.h>
#include <android/log.h>
#include <jni.h>
#include <atomic>
#include <chrono>
#include <mutex>
#include <string>
#include <thread>

namespace {
constexpr const char* TAG = "CryonixDiscord";
std::mutex gMutex;
std::unique_ptr<discordpp::Client> gClient;
std::thread gCallbackThread;
std::atomic<bool> gRunning{false};
std::string gDetails = "Browsing Minecraft";
std::string gState = "Cryonix Launcher";
std::string gVersion;
uint64_t gGameStart = 0;

void pushPresenceLocked() {
    if (!gClient || gClient->GetStatus() != discordpp::Client::Status::Ready) return;
    discordpp::Activity activity;
    activity.SetName("Minecraft");
    activity.SetType(discordpp::ActivityTypes::Playing);
    activity.SetDetails(gDetails);
    activity.SetState(gVersion.empty() ? gState : (gState + " • " + gVersion));

    discordpp::ActivityAssets assets;
    assets.SetLargeImage("cryonix");
    assets.SetLargeText("Cryonix Launcher");
    activity.SetAssets(std::move(assets));

    if (gGameStart != 0) {
        discordpp::ActivityTimestamps timestamps;
        timestamps.SetStart(gGameStart);
        activity.SetTimestamps(std::move(timestamps));
    }

    gClient->UpdateRichPresence(std::move(activity), [](discordpp::ClientResult result) {
        if (!result.Successful())
            __android_log_print(ANDROID_LOG_WARN, TAG, "Rich Presence update failed");
    });
}
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeInitialize(
        JNIEnv*, jclass, jlong applicationId) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gRunning.load()) return;

    gClient = std::make_unique<discordpp::Client>();
    gClient->SetApplicationId(static_cast<uint64_t>(applicationId));
    gClient->SetStatusChangedCallback([](discordpp::Client::Status status,
                                         discordpp::Client::Error, int) {
        std::lock_guard<std::mutex> callbackLock(gMutex);
        if (status == discordpp::Client::Status::Ready) pushPresenceLocked();
    });

    gRunning.store(true);
    gCallbackThread = std::thread([] {
        while (gRunning.load()) {
            discordpp::RunCallbacks();
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
        }
    });
    gClient->Connect();
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeConnect(
        JNIEnv*, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gClient) gClient->Connect();
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeUpdate(
        JNIEnv* env, jclass, jstring details, jstring state, jstring version) {
    const char* d = details ? env->GetStringUTFChars(details, nullptr) : nullptr;
    const char* s = state ? env->GetStringUTFChars(state, nullptr) : nullptr;
    const char* v = version ? env->GetStringUTFChars(version, nullptr) : nullptr;
    {
        std::lock_guard<std::mutex> lock(gMutex);
        gDetails = d ? d : "Browsing Minecraft";
        gState = s ? s : "Cryonix Launcher";
        gVersion = v ? v : "";
        pushPresenceLocked();
    }
    if (d) env->ReleaseStringUTFChars(details, d);
    if (s) env->ReleaseStringUTFChars(state, s);
    if (v) env->ReleaseStringUTFChars(version, v);
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeStartGame(
        JNIEnv* env, jclass, jstring instance, jstring version) {
    const char* i = instance ? env->GetStringUTFChars(instance, nullptr) : nullptr;
    const char* v = version ? env->GetStringUTFChars(version, nullptr) : nullptr;
    {
        std::lock_guard<std::mutex> lock(gMutex);
        gDetails = "Playing Minecraft";
        gState = i ? i : "Minecraft";
        gVersion = v ? v : "";
        gGameStart = static_cast<uint64_t>(
            std::chrono::duration_cast<std::chrono::seconds>(
                std::chrono::system_clock::now().time_since_epoch()).count());
        pushPresenceLocked();
    }
    if (i) env->ReleaseStringUTFChars(instance, i);
    if (v) env->ReleaseStringUTFChars(version, v);
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeClear(JNIEnv*, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gClient) gClient->ClearRichPresence();
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeShutdown(JNIEnv*, jclass) {
    {
        std::lock_guard<std::mutex> lock(gMutex);
        if (!gRunning.load()) return;
        gRunning.store(false);
        if (gClient) gClient->ClearRichPresence();
    }
    if (gCallbackThread.joinable()) gCallbackThread.join();
    std::lock_guard<std::mutex> lock(gMutex);
    if (gClient) gClient->Disconnect();
    gClient.reset();
}
