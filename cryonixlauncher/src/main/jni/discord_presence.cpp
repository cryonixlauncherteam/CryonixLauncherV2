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

void log(const char* message) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "%s", message);
}

void pushPresenceLocked() {
    if (!gClient || gClient->GetStatus() != discordpp::Client::Status::Ready) return;

    discordpp::Activity activity;
    activity.SetName("Cryonix Launcher");
    activity.SetType(discordpp::ActivityTypes::Playing);
    activity.SetDetails(gDetails);
    activity.SetState(gVersion.empty() ? gState : (gState + " • " + gVersion));

    discordpp::ActivityAssets assets;
    assets.SetLargeImage("cryonix");
    assets.SetLargeText("Cryonix Launcher");
    activity.SetAssets(std::move(assets));

    discordpp::ActivityTimestamps timestamps;
    timestamps.SetStart(static_cast<uint64_t>(
            std::chrono::duration_cast<std::chrono::seconds>(
                    std::chrono::system_clock::now().time_since_epoch()).count()));
    activity.SetTimestamps(std::move(timestamps));

    gClient->UpdateRichPresence(std::move(activity), [](discordpp::ClientResult result) {
        if (!result.Successful()) {
            __android_log_print(ANDROID_LOG_WARN, TAG,
                    "Discord Rich Presence update failed.");
        }
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
                                         discordpp::Client::Error error,
                                         int errorCode) {
        (void) error;
        (void) errorCode;
        std::lock_guard<std::mutex> callbackLock(gMutex);
        if (status == discordpp::Client::Status::Ready) {
            pushPresenceLocked();
        }
    });

    gRunning.store(true);
    gCallbackThread = std::thread([] {
        while (gRunning.load()) {
            discordpp::RunCallbacks();
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
        }
    });

    gClient->Connect();
    log("Discord Rich Presence initialized.");
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeUpdate(
        JNIEnv* env, jclass, jstring details, jstring state, jstring version) {
    const char* detailsChars = details ? env->GetStringUTFChars(details, nullptr) : nullptr;
    const char* stateChars = state ? env->GetStringUTFChars(state, nullptr) : nullptr;
    const char* versionChars = version ? env->GetStringUTFChars(version, nullptr) : nullptr;

    {
        std::lock_guard<std::mutex> lock(gMutex);
        gDetails = detailsChars ? detailsChars : "Browsing Minecraft";
        gState = stateChars ? stateChars : "Cryonix Launcher";
        gVersion = versionChars ? versionChars : "";
        pushPresenceLocked();
    }

    if (detailsChars) env->ReleaseStringUTFChars(details, detailsChars);
    if (stateChars) env->ReleaseStringUTFChars(state, stateChars);
    if (versionChars) env->ReleaseStringUTFChars(version, versionChars);
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeClear(
        JNIEnv*, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gClient) gClient->ClearRichPresence();
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_discord_DiscordRichPresence_nativeShutdown(
        JNIEnv*, jclass) {
    {
        std::lock_guard<std::mutex> lock(gMutex);
        if (!gRunning.load()) return;
        gRunning.store(false);
        if (gClient) gClient->ClearRichPresence();
    }

    if (gCallbackThread.joinable()) {
        gCallbackThread.join();
    }

    std::lock_guard<std::mutex> lock(gMutex);
    if (gClient) gClient->Disconnect();
    gClient.reset();
    log("Discord Rich Presence shut down.");
}
