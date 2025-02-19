package jabava.window.ui

sealed interface AppState {

	object InTray : AppState

	object Open : AppState
}