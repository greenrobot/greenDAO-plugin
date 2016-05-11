package org.greenrobot.greendao.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.util.PatternFilterable
import org.greenrobot.greendao.codechanger.Greendao3Generator
import org.greenrobot.greendao.codechanger.SchemaOptions
import java.io.File

class Greendao3GradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("greendao", GreendaoOptions::class.java, project)

        project.afterEvaluate {
            val options = project.extensions.getByType(GreendaoOptions::class.java)

            requireNotNull(options.entities) { "greenDAO: 'entities' property should be specified" }

            val genSrcDir = options.genSrcDir

            val sourceProvider = when {
                project.plugins.findPlugin("java") != null -> JavaPluginSourceProvider(project)

                project.plugins.findPlugin("android") != null
                    || project.plugins.findPlugin("com.android.application") != null
                    || project.plugins.findPlugin("com.android.library") != null -> AndroidPluginSourceProvider(project)

                else -> throw RuntimeException("greenDAO supports only Java or Android projects. " +
                    "None of corresponding plugins have been applied to the project")
            }

            val inputFiles = getInputFiles(
                project,
                options.entities!!,
                if (options.scanOutputDir) null else genSrcDir,
                sourceProvider
            )

            require(inputFiles.isNotEmpty()) {
                "greenDAO: No java files found among specified file collection. " +
                   "Please check 'greendao.entities' property"
            }

            // TODO document daoPackage gen, extract fun?
            val daoPackageDir = options.daoPackage?.replace('.', '/') ?: run {
                // detect package name
                val firstFile = inputFiles.first()
                val pkgDir = sourceProvider.sourceDirs()
                    .find { firstFile.startsWith(it) }
                    ?.let { firstFile.parentFile.relativeToOrNull(it)?.path }
                requireNotNull(pkgDir) {
                    "greenDAO: Can't determine package name for output dao classes. " +
                        "Please specify it explicitly with daoPackage option."
                }
            }

            val daoPackage = options.daoPackage ?: daoPackageDir.replace(File.separatorChar, '.')

            // define task
            val generateCode = project.task("generateGreenDao").apply {
                logging.captureStandardOutput(LogLevel.INFO)

                inputs.files(*inputFiles.toTypedArray())
                outputs.dir(File(genSrcDir, daoPackageDir))
                if (options.generateTests) {
                    outputs.dir(options.testsGenSrcDir)
                }

                // run generateBuildConfig Gradle task to generate BuildConfig first
                inputs.property("plugin-version", BuildConfig.version)

                val defaultOptions = SchemaOptions(
                    name = "default",
                    version = options.schemaVersion,
                    encrypt = options.encrypt,
                    daoPackage = daoPackage,
                    outputDir = genSrcDir,
                    testsOutputDir = if (options.generateTests) options.testsGenSrcDir else null
                )

                val schemaOptions = mutableMapOf("default" to defaultOptions)

                options.schemas.schemasMap.map { e ->
                    val (name, schemaExt) = e
                    SchemaOptions(
                        name = name,
                        version = schemaExt.version ?: defaultOptions.version,
                        encrypt = schemaExt.encrypt ?: defaultOptions.encrypt,
                        daoPackage = schemaExt.daoPackage ?: "${defaultOptions.daoPackage}.$name",
                        outputDir = schemaExt.genSrcDir ?: defaultOptions.outputDir,
                        testsOutputDir = if (options.generateTests) {
                            schemaExt.testsGenSrcDir ?: defaultOptions.testsOutputDir
                        } else null
                    )
                }.associateTo(schemaOptions, { it.name to it })

                // define inputs of the task
                schemaOptions.forEach { e ->
                    inputs.property("schema-${e.key}", e.value.toString())
                }

                doLast {
                    Greendao3Generator(options.formatting.data, options.skipTestGeneration)
                        .run(inputFiles, schemaOptions)
                }
            }

            project.tasks.filter { it.name.startsWith("compile") }.forEach {
                it.dependsOn(generateCode)
            }
        }
    }

    private fun getInputFiles(project: Project, spec: Any, excludeDir: File?,
                              fileTreeProvider: SourceProvider) : List<File> {
        return when (spec) {
            is FileCollection -> spec.asFileTree.matching(Closure { pf: PatternFilterable ->
                pf.include("**/*.java")
            }).files

            is String -> spec.let {
                when {
                    // package name
                    it.matches(Regex("[a-z_.]+")) -> fileTreeProvider.sourceFiles().map { tree ->
                        tree.matching(Closure { pf: PatternFilterable ->
                            val path = it.replace('.', '/')
                            pf.include("$path/**/*.java")
                        }).files
                    }.flatten().toList()

                    // folder
                    else -> project.fileTree(it).apply { include("**/*.java") }.files
                }
            }
            else -> throw RuntimeException("greenDAO: entities should be package name, path or file collection")
        }.let {
            if (excludeDir != null) {
                it.filterNot { it.startsWith(excludeDir) }
            } else {
                it.toList()
            }
        }
    }
}