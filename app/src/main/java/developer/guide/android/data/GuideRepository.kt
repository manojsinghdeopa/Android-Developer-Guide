package developer.guide.android.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object GuideRepository {

    private val SECTION_DATA = listOf(
        // Initial Setup & Core Concepts
        Triple(1, "Setting Up Your Development Environment", "guides/tools_and_environment_setup.md"),
        Triple(2, "Mastering Version Control with Git", "guides/version_control_setup.md"), // Or "Version Control (Git) Essentials"
        Triple(3, "Designing Your App's Architecture", "guides/architecture_setup.md"),
        Triple(4, "Building the Data Layer", "guides/data_layer_setup.md"),
        Triple(5, "Crafting the User Interface (UI) Layer", "guides/ui_layer_guide.md"),
        Triple(6, "Managing Project Dependencies", "guides/dependencies_setup.md"),
        Triple(7, "Implementing Dependency Injection", "guides/dependency_injection_setup.md"),

        // Advanced Topics & Features
        Triple(8, "Integrating with External Services & APIs", "guides/integrations_guide.md"), // Or "Service & API Integrations"
        Triple(9, "Understanding Background Tasks & WorkManager", "guides/background_tasks_guide.md"), // Or "Executing Background Work"
        Triple(10, "Effective Concurrency in Android", "guides/concurrency_guide.md"),

        // Performance & Optimization
        Triple(11, "Optimizing Network Usage & Efficiency", "guides/network_efficiency_guide.md"),
        Triple(12, "Optimizing Memory & CPU Performance", "guides/memory_and_cpu_optimization_guide.md"),
        Triple(13, "Maximizing Battery Life", "guides/battery_optimization_guide.md"), // Or "Battery Optimization Techniques"
        Triple(14, "Reducing Your App's Size", "guides/app_size_guide.md"),

        // Quality & Reliability
        Triple(15, "Enhancing App Security", "guides/security_guide.md"), // Or "Security Best Practices"
        Triple(16, "Leveraging Debugging Tools", "guides/debugging_tools_guide.md"), // Or "Effective Debugging Strategies"
        Triple(17, "Fundamentals of Unit Testing", "guides/unit_testing_guide.md"),
        Triple(18, "Implementing UI Tests", "guides/ui_testing_guide.md"),
        Triple(19, "Writing Instrumentation Tests", "guides/instrumentation_testing_guide.md"),
        Triple(20, "Automating Builds & CI/CD Pipelines", "guides/automation_and_cicd_guide.md"),

        // Best Practices & Conclusion
        Triple(21, "Adopting Android Development Best Practices", "guides/android_best_practices.md") // Or "Key Android Best Practices"
    )


    fun getGuideSections(): List<GuideSection> {
        return SECTION_DATA.map { (id, title) ->
            GuideSection(
                id = id,
                title = title,
                content = null
            )
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