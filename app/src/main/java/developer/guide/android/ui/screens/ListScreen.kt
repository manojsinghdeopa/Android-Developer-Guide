package developer.guide.android.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import developer.guide.android.R
import developer.guide.android.data.GuideRepository
import developer.guide.android.data.GuideSection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(onItemClick: (Int) -> Unit) {

    val items = GuideRepository.getGuideSections()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_description)) }
            )
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                SectionItem(section = item, onClick = {
                    onItemClick(item.id)
                })
            }
        }
    }
}


@Composable
fun SectionItem(section: GuideSection, onClick: () -> Unit) {

    Row(modifier = Modifier
        .padding(16.dp)
        .clickable(onClick = onClick))
    {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Navigate to ${section.title}"
        )
    }

}