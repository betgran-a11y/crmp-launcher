using System;
using System.Collections;
using UnityEngine;
using UnityEngine.Networking;

namespace CRMP.Core
{
    [Serializable]
    public class CharacterData
    {
        public int id;
        public string nickname;
        public int gender;
        public int skin_id;
        public long cash;
        public long bank_balance;
        public int level;
        public string job_id;
        public int wanted_stars;
    }

    [Serializable]
    public class CharacterResponse
    {
        public CharacterData character;
    }

    public class ApiClient : MonoBehaviour
    {
        [SerializeField] private string apiBaseUrl = "http://10.0.2.2:3000";

        public IEnumerator LoadMyCharacter(string jwtToken, Action<CharacterData> onSuccess, Action<string> onError)
        {
            using var req = UnityWebRequest.Get($"{apiBaseUrl}/api/character/me");
            req.SetRequestHeader("Authorization", $"Bearer {jwtToken}");

            yield return req.SendWebRequest();

            if (req.result == UnityWebRequest.Result.Success)
            {
                var wrapper = JsonUtility.FromJson<CharacterResponse>(req.downloadHandler.text);
                onSuccess?.Invoke(wrapper.character);
            }
            else
            {
                onError?.Invoke(req.error);
            }
        }
    }
}
