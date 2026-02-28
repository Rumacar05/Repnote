package com.ruma.repnote.core.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A simplified screen component with common Repnote UI patterns.
 *
 * This component provides a consistent screen structure with optional top bar,
 * bottom bar, FAB, and automatic handling of system insets.
 *
 * Usage example:
 * ```
 * RepnoteScreen(
 *     topBar = { TopAppBar(title = { Text("Screen Title") }) },
 *     bottomBar = { NavigationBar { /* items */ } }
 * ) { paddingValues ->
 *     // Your content with proper padding
 *     LazyColumn(contentPadding = paddingValues) { /* ... */ }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the scaffold
 * @param topBar Custom top bar (overrides title/onNavigateBack)
 * @param bottomBar Bottom navigation bar
 * @param floatingActionButton Floating action button
 * @param floatingActionButtonPosition Position of the FAB
 * @param snackbarHostState State for managing snackbars
 * @param containerColor Background color of the scaffold
 * @param contentColor Color for content
 * @param content Screen content that receives PaddingValues for proper insets handling
 */
@Composable
fun RepnoteScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
