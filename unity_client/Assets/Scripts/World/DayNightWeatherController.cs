using UnityEngine;

namespace CRMP.World
{
    public enum WeatherType { Overcast, Rain, Snow, Fog }

    /// <summary>
    /// Управляет постсоветской атмосферой: цикл дня и ночи, тусклое освещение, дождь, снег, туман.
    /// </summary>
    public class DayNightWeatherController : MonoBehaviour
    {
        [Range(0f, 24f)] public float timeOfDay = 15.5f;
        public float dayDurationSeconds = 1200f; // 20 минут реального времени = 1 игровые сутки
        public Light sunLight;
        public WeatherType currentWeather = WeatherType.Overcast;

        private void Update()
        {
            timeOfDay += (24f / dayDurationSeconds) * Time.deltaTime;
            if (timeOfDay >= 24f) timeOfDay -= 24f;

            UpdateSunAndFog();
        }

        private void UpdateSunAndFog()
        {
            if (sunLight == null) return;

            float sunAngle = (timeOfDay / 24f) * 360f - 90f;
            sunLight.transform.rotation = Quaternion.Euler(sunAngle, 170f, 0f);

            bool isDay = timeOfDay >= 6f && timeOfDay <= 20f;
            float baseIntensity = isDay ? 0.65f : 0.15f; // Тусклая серая атмосфера
            if (currentWeather == WeatherType.Rain || currentWeather == WeatherType.Fog)
                baseIntensity *= 0.7f;

            sunLight.intensity = baseIntensity;
            RenderSettings.fog = true;
            RenderSettings.fogDensity = currentWeather == WeatherType.Fog ? 0.025f : 0.008f;
        }
    }
}
