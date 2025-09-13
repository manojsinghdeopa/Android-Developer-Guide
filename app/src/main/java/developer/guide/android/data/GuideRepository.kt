package developer.guide.android.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object GuideRepository {

    private val SECTION_DATA = listOf(
        Triple(1, "Tools & Environment Setup", "guides/tools_and_environment_setup.md"),
        Triple(2, "Version Control Setup", "guides/version_control_setup.md"),
        Triple(3, "Architecture Setup", "guides/architecture_setup.md"),
        Triple(4, "Data Layer Setup", "guides/data_layer_setup.md"),
        Triple(5, "UI Layer Guide", "guides/ui_layer_guide.md"),
        Triple(6, "Dependencies Setup", "guides/dependencies_setup.md"),
        Triple(7, "Dependency Injection", "guides/dependency_injection_setup.md"),
        Triple(8, "Integrations Guide", "guides/integrations_guide.md"),
        Triple(9, "Background Tasks", "guides/background_tasks_guide.md"),
        Triple(10, "Concurrency Guide", "guides/concurrency_guide.md"),
        Triple(11, "Network Efficiency", "guides/network_efficiency_guide.md"),
        Triple(12, "Memory & CPU Optimization", "guides/memory_and_cpu_optimization_guide.md"),
        Triple(13, "Battery Optimization", "guides/battery_optimization_guide.md"),
        Triple(14, "App Size Guide", "guides/app_size_guide.md"),
        Triple(15, "Security Guide", "guides/security_guide.md"),
        Triple(16, "Debugging Tools", "guides/debugging_tools_guide.md"),
        Triple(17, "Unit Testing", "guides/unit_testing_guide.md"),
        Triple(18, "UI Testing", "guides/ui_testing_guide.md"),
        Triple(19, "Instrumentation Testing", "guides/instrumentation_testing_guide.md"),
        Triple(20, "Automation & CI/CD", "guides/automation_and_cicd_guide.md"),
        Triple(21, "Android Best Practices", "guides/android_best_practices.md")
    )

    fun getGuideSections(context: Context, loadContent: Boolean = true): List<GuideSection> {
        return SECTION_DATA.map { (id, title, filePath) ->
            GuideSection(
                id = id,
                title = title,
                // Content is not loaded here by default for the list
                content = if (loadContent) loadMarkdownBlocking(context, filePath) else null
            )
        }
    }


    private fun loadMarkdownBlocking(context: Context, fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            "Error loading content for $fileName: ${e.message}"
        }
    }


    private suspend fun loadMarkdown(context: Context, fileName: String): String =
        withContext(Dispatchers.IO) {
            try {
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                "Error loading content for $fileName: ${e.message}"
            }
        }


    suspend fun getSectionById(context: Context, id: Int): GuideSection? {
        val sectionData = SECTION_DATA.find { it.first == id }
        return sectionData?.let { (sectionId, title, filePath) ->
            GuideSection(
                id = sectionId,
                title = title,
                content = loadMarkdown(context, filePath)
            )
        }
    }
}