package view.common


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LimitedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLength: Int,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    allowSpaces: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = if (allowSpaces) newValue else newValue.replace(" ", "")
            if (filtered.length <= maxLength) {
                onValueChange(filtered)
            }
        },

        label = { Text(label) },
        supportingText = { Text("${value.length}/$maxLength") },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines

    )
}