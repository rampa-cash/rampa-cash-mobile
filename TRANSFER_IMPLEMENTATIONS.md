# SPL Token Transfer Implementations

This project provides **two different implementations** for SPL token transfers to address wallet compatibility issues.

## 🔧 **Manual Transfer Implementation** (Recommended)

**Location**: `ManualSplTokenTransferUseCase.kt`

**Purpose**: Fixes the "transaction cannot be simulated" error in Solflare wallet by bypassing the `web3-solana` library's transaction serialization bugs.

**How it works**:
- ✅ Uses direct RPC calls for account checking and blockhash retrieval
- ✅ Manually builds transaction bytes without library serialization
- ✅ Compatible with Solflare mobile wallet simulation
- ✅ Handles ATA creation automatically

**Best for**: Production use with Solflare wallet

## 📚 **Library Transfer Implementation** (Original)

**Location**: `SplTokenTransferUseCase.kt`

**Purpose**: Original implementation using the `web3-solana` Kotlin library's `Message.Builder()` and transaction serialization.

**How it works**:
- Uses `Message.Builder()` from web3-solana library
- Automatic transaction serialization via library
- May encounter simulation issues with some wallets

**Best for**: Debugging, testing, and wallets that don't have simulation issues

## 🔄 **Switching Between Implementations**

### **Easy Switch** (Recommended)

Edit `TransferConfig.kt`:

```kotlin
object TransferConfig {
    // Change this value to switch implementations
    const val USE_MANUAL_TRANSFER = true  // ← Change to false for library implementation
}
```

### **What Each Setting Does**

| Setting | Implementation | Wallet Compatibility |
|---------|---------------|----------------------|
| `true` | Manual Transfer | ✅ Solflare, ✅ Phantom, ✅ Other wallets |
| `false` | Library Transfer | ❌ Solflare (simulation issues), ✅ Phantom |

## 🚀 **Testing Both Implementations**

1. **Test Manual Implementation** (default):
   ```kotlin
   const val USE_MANUAL_TRANSFER = true
   ```
   - Should work with Solflare mobile
   - Should simulate and send successfully

2. **Test Library Implementation**:
   ```kotlin
   const val USE_MANUAL_TRANSFER = false
   ```
   - May show "transaction cannot be simulated" in Solflare
   - Should work with Phantom wallet

## 📝 **Logging & Debugging**

Enable detailed transfer logging:

```kotlin
const val ENABLE_TRANSFER_LOGGING = true
```

**Log Messages**:
- `🔧 Using Manual Transfer (Solflare-Compatible)`
- `📚 Using Library Transfer (web3-solana)`

## 🛠️ **Development Notes**

### **Why Two Implementations?**

The `web3-solana` Kotlin library has serialization bugs when used with Mobile Wallet Adapter that cause transaction simulation failures in Solflare wallet. The manual implementation bypasses these library bugs by building transactions from scratch.

### **Performance**

- **Manual**: Slightly more RPC calls (for blockhash, account checking)
- **Library**: Fewer RPC calls but unreliable simulation

### **Future**

- Keep both implementations until the `web3-solana` library fixes are confirmed
- Manual implementation can be removed once library issues are resolved
- Easy to switch back for testing or if issues arise

## 🔍 **Troubleshooting**

| Issue | Solution |
|-------|----------|
| "transaction cannot be simulated" | Set `USE_MANUAL_TRANSFER = true` |
| Transaction fails with manual | Try `USE_MANUAL_TRANSFER = false` |
| Want detailed logs | Set `ENABLE_TRANSFER_LOGGING = true` |
| Need to test library | Set `USE_MANUAL_TRANSFER = false` |

## ✅ **Recommended Configuration**

For production use with Solflare wallet:

```kotlin
object TransferConfig {
    const val USE_MANUAL_TRANSFER = true      // Manual implementation
    const val ENABLE_TRANSFER_LOGGING = true // Enable debugging
}
``` 