# Jetpack Compose — Reference Guide

## State Management

- Use `StateFlow` from ViewModels, collected in Compose via `collectAsStateWithLifecycle()`
- Use `remember` and `mutableStateOf` for local composable state
- Hoist state to the caller when multiple composables need it or when the ViewModel owns it
- `snapshotFlow {}` converts Compose state into a `Flow` for use in ViewModels

```kotlin
// ViewModel exposing StateFlow
class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
}

// Composable collecting it
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
}
```

## Side Effects

- `LaunchedEffect(key)` — runs a coroutine when `key` changes; cancels and restarts on rekey
- `DisposableEffect(key)` — for setup/teardown (e.g., lifecycle observers); must call `onDispose {}`
- `rememberUpdatedState(value)` — captures the latest value inside a long-lived effect without restarting it

```kotlin
// LaunchedEffect: runs once on composition, restarts if pulseRateMs changes
LaunchedEffect(pulseRateMs) {
    while (isActive) {
        delay(pulseRateMs)
        alpha.animateTo(0f)
        alpha.animateTo(1f)
    }
}

// DisposableEffect: observe lifecycle, clean up on dispose
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) currentOnStart()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}

// rememberUpdatedState: keep latest callback without restarting LaunchedEffect
val currentOnTimeout by rememberUpdatedState(onTimeout)
LaunchedEffect(true) {
    delay(SplashWaitTimeMillis)
    currentOnTimeout()
}
```

## Navigation

- Use Navigation Compose (`NavHost`, `composable()` routes)
- Pass simple args via route strings; complex data via ViewModels or saved state

## Project Conventions

- All screens are `@Composable` functions named `*Screen.kt`
- ViewModels expose `StateFlow`, never `LiveData`
- Battle renderer uses custom `SurfaceView`, not Compose
- Theme defined in `presentation/ui/theme/` (Material3)
