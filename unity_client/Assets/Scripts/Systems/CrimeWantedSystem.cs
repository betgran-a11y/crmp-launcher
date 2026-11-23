using System;
using UnityEngine;

namespace CRMP.Systems
{
    public class CrimeWantedSystem : MonoBehaviour
    {
        public int WantedStars { get; private set; }
        public float JailSecondsRemaining { get; private set; }

        public event Action<int> OnWantedChanged;

        public void AddWantedStar(int stars = 1)
        {
            WantedStars = Mathf.Clamp(WantedStars + stars, 0, 6);
            OnWantedChanged?.Invoke(WantedStars);
        }

        public void ArrestPlayer(float jailSeconds)
        {
            WantedStars = 0;
            JailSecondsRemaining = jailSeconds;
            OnWantedChanged?.Invoke(WantedStars);
        }

        private void Update()
        {
            if (JailSecondsRemaining > 0f)
            {
                JailSecondsRemaining = Mathf.Max(0f, JailSecondsRemaining - Time.deltaTime);
            }
        }
    }
}
