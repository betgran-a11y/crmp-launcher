using System;
using UnityEngine;

namespace CRMP.Systems
{
    public class EconomyManager : MonoBehaviour
    {
        public long Cash { get; private set; }
        public long BankBalance { get; private set; }

        public event Action<long, long> OnBalanceChanged;

        public void Initialize(long cash, long bank)
        {
            Cash = cash;
            BankBalance = bank;
            OnBalanceChanged?.Invoke(Cash, BankBalance);
        }

        public bool TrySpendCash(long amount)
        {
            if (amount <= 0 || Cash < amount) return false;
            Cash -= amount;
            OnBalanceChanged?.Invoke(Cash, BankBalance);
            return true;
        }

        public bool TransferToBank(long amount)
        {
            if (amount <= 0 || Cash < amount) return false;
            Cash -= amount;
            BankBalance += amount;
            OnBalanceChanged?.Invoke(Cash, BankBalance);
            return true;
        }
    }
}
