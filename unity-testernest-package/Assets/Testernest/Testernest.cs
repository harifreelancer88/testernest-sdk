using System;
using UnityEngine;

namespace Testernest
{
    public static class Testernest
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        private const string BridgeClassName = "com.testernest.unity.TesternestUnityBridge";

        private static AndroidJavaObject CurrentActivity
        {
            get
            {
                using (var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                {
                    return unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
                }
            }
        }

        private static AndroidJavaClass BridgeClass => new AndroidJavaClass(BridgeClassName);
#endif

        public static void Init(string publicKey, string baseUrl = null, bool enableLogs = false)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                bridge.CallStatic("init", CurrentActivity, publicKey, baseUrl, enableLogs);
            }
#endif
        }

        public static void Track(string name, string jsonProps = null)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                bridge.CallStatic("track", name, jsonProps);
            }
#endif
        }

        public static void Flush()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                bridge.CallStatic("flush");
            }
#endif
        }

        public static void SetScreen(string screen)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                bridge.CallStatic("setScreen", screen);
            }
#endif
        }

        public static void ConnectTester(string code6)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                bridge.CallStatic("connectTester", code6);
            }
#endif
        }

        public static void DisconnectTester()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                bridge.CallStatic("disconnectTester");
            }
#endif
        }

        public static string GetDebugSnapshot()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (var bridge = BridgeClass)
            {
                return bridge.CallStatic<string>("getDebugSnapshot");
            }
#else
            return "{}";
#endif
        }
    }
}
