# Rampa Mobile 💰

[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Solana](https://img.shields.io/badge/Solana-9945FF?style=for-the-badge&logo=solana&logoColor=white)](https://solana.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)
![Platform](https://img.shields.io/badge/platform-Android-green.svg?style=flat-square)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat-square)

A modern Kotlin-based mobile application for Solana blockchain interactions, built for the Solana Mobile Hackathon.

## 📱 Screenshots

<table>
  <tr>
    <td><img src="docs/screenshots/home_screen.png" width="250" alt="Home Screen"/></td>
    <td><img src="docs/screenshots/send_screen.png" width="250" alt="Send Screen"/></td>
  </tr>
  <tr>
    <td align="center">Home Screen</td>
    <td align="center">Send Tokens</td>
  </tr>
</table>

<table>
  <tr>
    <td><img src="docs/screenshots/investment_screen.png" width="250" alt="Investment Screen"/></td>
    <td><img src="docs/screenshots/profile_screen.png" width="250" alt="Profile Screen"/></td>
    <td><img src="docs/screenshots/transfers_screen.png" width="250" alt="Transfers Screen"/></td>
  </tr>
  <tr>
    <td align="center">Investment Portfolio</td>
    <td align="center">Profile</td>
    <td align="center">Transaction History</td>
  </tr>
</table>

## ✨ Features

### 🔐 Authentication & Security
- **Web3Auth Integration** - Secure wallet authentication
- **MWA** - Log in with your existing wallet (e.g. PhantomWallet or Solscan)

### 💸 Token Operations
- **Multi-Token Support** - USDC, EURC, BONK, and SOL
- **Send & Receive** - Fast and secure token transfers
- **Real-time Balance** - Live account balance updates
- **Transaction History** - Complete transfer records

### 📊 Investment Features
- **Portfolio Tracking** - Monitor your crypto investments
- **Stock Integration** - Track tokenized stocks (TSLA, NVDA, AAPL)
- **Real-time Prices** - Live market data updates

### 🎯 User Experience
- **Intuitive UI** - Clean and modern Material Design
- **Quick Actions** - Easy-to-use transfer interface
- **Token Switching** - Seamless multi-token management
- **Responsive Design** - Optimized for all screen sizes

## 🛠 Technology Stack

- **Language**: Kotlin
- **Framework**: Jetpack Compose
- **Blockchain**: Solana
- **Authentication**: Web3Auth
- **Architecture**: MVVM with Use Cases
- **DI**: Dagger Hilt
- **Networking**: Custom HTTP Driver
- **UI**: Material Design 3

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 24+
- Kotlin 1.8+
- Solana Mobile Stack (if testing on Solana Mobile)

### Installation

1. Clone the repository
```bash
git clone https://github.com/your-username/rampa-cash-mobile.git
cd rampa-cash-mobile
```

2. Open in Android Studio
```bash
studio .
```

3. Build and run
```bash
./gradlew assembleDebug
```

## 📱 App Architecture

```
app/
├── di/                    # Dependency Injection
├── navigation/            # Navigation setup
├── networking/            # HTTP client & API
├── ui/
│   ├── components/        # Reusable UI components
│   ├── screens/          # App screens
│   └── theme/            # Material Design theme
├── usecase/              # Business logic layer
├── viewmodel/            # UI state management
└── web3auth/             # Authentication logic
```

## 🔗 Key Components

- **MainScreen** - Dashboard with portfolio overview
- **SendScreen** - Token transfer interface
- **ReceiveScreen** - QR code and address sharing
- **InvestmentScreen** - Portfolio and stock tracking
- **TransfersScreen** - Transaction history
- **ProfileScreen** - User settings and wallet info

## 🏆 Hackathon Features

Built specifically for the Solana Mobile Hackathon with focus on:
- **Mobile-First Design** - Optimized for smartphone usage
- **Solana Integration** - Native blockchain interactions
- **User Onboarding** - Simplified crypto experience
- **Real-World Utility** - Practical daily use cases

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📧 Contact

Rampa: [https://rampa.cash/](https://rampa.cash/)

---

**Built with ❤️ for the Solana Mobile Hackathon**