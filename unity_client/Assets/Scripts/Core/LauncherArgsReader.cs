using UnityEngine;

namespace CRMP.Core
{
    /// <summary>
    /// Читает JWT-токен, адрес сервера и ник, переданные из Android-лаунчера (или CLI на ПК).
    /// </summary>
    public class LauncherArgsReader : MonoBehaviour
    {
        public static LauncherArgsReader Instance { get; private set; }

        public string AuthToken { get; private set; }
        public string ServerAddress { get; private set; } = "127.0.0.1:7777";
        public string Nickname { get; private set; } = "Test_Player";

        private void Awake()
        {
            if (Instance != null) { Destroy(gameObject); return; }
            Instance = this;
            DontDestroyOnLoad(gameObject);

            ReadLaunchParameters();
        }

        private void ReadLaunchParameters()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            using var currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
            using var intent = currentActivity.Call<AndroidJavaObject>("getIntent");

            if (intent.Call<bool>("hasExtra", "token"))
                AuthToken = intent.Call<string>("getStringExtra", "token");
            if (intent.Call<bool>("hasExtra", "server"))
                ServerAddress = intent.Call<string>("getStringExtra", "server");
            if (intent.Call<bool>("hasExtra", "nickname"))
                Nickname = intent.Call<string>("getStringExtra", "nickname");
#else
            var args = System.Environment.GetCommandLineArgs();
            for (int i = 0; i < args.Length - 1; i++)
            {
                if (args[i] == "-token") AuthToken = args[i + 1];
                if (args[i] == "-server") ServerAddress = args[i + 1];
                if (args[i] == "-nickname") Nickname = args[i + 1];
            }
#endif
        }
    }
}
