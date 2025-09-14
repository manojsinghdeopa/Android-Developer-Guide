package developer.guide.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import developer.guide.android.data.GuideRepository
import developer.guide.android.data.GuideSection

@Composable
fun DetailsScreen(itemId: Int) {

    val context = LocalContext.current
    var item: GuideSection? by remember { mutableStateOf(null) }

    // Launched the Effect
    LaunchedEffect(itemId) {
        item = GuideRepository.getSectionById(context = context, id = itemId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())) {

        Spacer(modifier = Modifier.height(24.dp))

        Markdown(
            content = item?.content.orEmpty(),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}



