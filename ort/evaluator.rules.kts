/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

/*******************************************************
 * OSS Review Toolkit (ORT) policy rules for PowSyBl   *
 *                                                     *
 * PowSyBl projects are licensed under MPL-2.0.        *
 * Only ERROR violations fail the CI                   *
 * (see severeRuleViolationThreshold in config.yml).   *
 *                                                     *
 * These rules are not legal advice: changes to the    *
 * policy should be reviewed by the PowSyBl TSC.       *
 *******************************************************/

/**
 * Project-wide constants.
 */

// License of the PowSyBl code itself.
val powsyblLicense = "MPL-2.0"

// Licenses accepted in the root LICENSE file of a PowSyBl repository.
val allowedRootLicenses = setOf(powsyblLicense)

// Maven groupId prefix of the PowSyBl artifacts.
val powsyblNamespace = "com.powsybl"

// Only vulnerabilities with a CVSS score at least this high fail the CI.
val vulnerabilityScoreThreshold = 7.0f
val vulnerabilityScoringSystem = "CVSS:3.1"

/**
 * Import the license classifications from license-classifications.yml.
 */

val permissiveLicenses = licenseClassifications.licensesByCategory["permissive"].orEmpty()

val publicDomainLicenses = licenseClassifications.licensesByCategory["public-domain"].orEmpty()

val weakCopyleftLicenses = licenseClassifications.licensesByCategory["weak-copyleft"].orEmpty()

val strongCopyleftLicenses = licenseClassifications.licensesByCategory["strong-copyleft"].orEmpty()

val reviewRequiredLicenses = licenseClassifications.licensesByCategory["review-required"].orEmpty()

// The complete set of licenses covered by policy rules.
val handledLicenses = listOf(
    permissiveLicenses,
    publicDomainLicenses,
    weakCopyleftLicenses,
    strongCopyleftLicenses,
    reviewRequiredLicenses
).flatten().let {
    it.getDuplicates().let { duplicates ->
        require(duplicates.isEmpty()) {
            "The classifications for the following licenses overlap: $duplicates"
        }
    }

    it.toSet()
}

/**
 * Markdown guidance attached to violations.
 */

val ortDocs = "https://oss-review-toolkit.org/ort/docs/configuration"

fun howToFixLicense() = """
    * If the package is **dual-licensed** (e.g. `EPL-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0`), select the
      acceptable license with a `license_choices` entry in the repository `.ort.yml`
      ([documentation]($ortDocs/ort-yml)).
    * If the license metadata of the package is **wrong or missing**, fix it with a `curations` entry in the
      repository `.ort.yml` ([documentation]($ortDocs/package-curations)).
    * If the package is only used for **tests or the build**, exclude its Maven scope in the repository `.ort.yml`
      (`excludes.scopes`).
    * Otherwise, replace the dependency or ask the PowSyBl TSC for a review.
""".trimIndent()

fun howToFixUnhandledLicense() = """
    The license is not classified yet in the global `license-classifications.yml`. Ask for it to be added to one of
    the categories (`permissive`, `public-domain`, `weak-copyleft`, `strong-copyleft`, `review-required`).
    If the license was wrongly detected or declared, see below.

""".trimIndent() + "\n" + howToFixLicense()

fun howToFixVulnerability() = """
    * Upgrade the dependency to a version fixing the vulnerability (check the Maven dependency tree with
      `mvn dependency:tree` to find which direct dependency brings it).
    * If the vulnerable code is not reachable or the match is wrong, add a `resolutions.vulnerabilities` entry with a
      justification in the repository `.ort.yml` ([documentation]($ortDocs/resolutions)).
""".trimIndent()

/**
 * Helpers and matchers.
 */

// True for the projects analyzed in the repository (i.e. the PowSyBl source code), false for dependencies.
fun PackageRule.isProjectPackage() =
    object : RuleMatcher {
        override val description = "isProjectPackage(${pkg.metadata.id.toCoordinates()})"

        override fun matches() = ortResult.isProject(pkg.metadata.id)
    }

// True for PowSyBl artifacts consumed as dependencies (e.g. powsybl-core in powsybl-open-loadflow).
fun PackageRule.isPowsyblPackage() =
    object : RuleMatcher {
        override val description = "isPowsyblPackage(${pkg.metadata.id.toCoordinates()})"

        override fun matches() =
            pkg.metadata.id.namespace == powsyblNamespace ||
                    pkg.metadata.id.namespace.startsWith("$powsyblNamespace.")
    }

fun PackageRule.LicenseRule.isHandled() =
    object : RuleMatcher {
        override val description = "isHandled($license)"

        override fun matches() =
            license in handledLicenses && ("-exception" !in license.toString() || " WITH " in license.toString())
    }

fun PackageRule.LicenseRule.isStrongCopyleft() =
    object : RuleMatcher {
        override val description = "isStrongCopyleft($license)"

        override fun matches() = license in strongCopyleftLicenses
    }

fun PackageRule.LicenseRule.isWeakCopyleft() =
    object : RuleMatcher {
        override val description = "isWeakCopyleft($license)"

        override fun matches() = license in weakCopyleftLicenses
    }

fun PackageRule.LicenseRule.isReviewRequired() =
    object : RuleMatcher {
        override val description = "isReviewRequired($license)"

        override fun matches() = license in reviewRequiredLicenses
    }

fun PackageRule.LicenseRule.isPowsyblLicense() =
    object : RuleMatcher {
        override val description = "isPowsyblLicense($license)"

        override fun matches() = license.toString() == powsyblLicense
    }

/**
 * Rules executed for each package and project.
 */

fun RuleSet.unhandledLicenseRule() = packageRule("UNHANDLED_LICENSE") {
    require {
        -isExcluded()
    }

    licenseRule("UNHANDLED_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
        require {
            -isExcluded()
            -isHandled()
        }

        error(
            "The license $license is currently not covered by the PowSyBl policy rules. " +
                    "The license was ${licenseSource.name.lowercase()} in package " +
                    "${pkg.metadata.id.toCoordinates()}.",
            howToFixUnhandledLicense()
        )
    }
}

fun RuleSet.unmappedDeclaredLicenseRule() = packageRule("UNMAPPED_DECLARED_LICENSE") {
    require {
        -isExcluded()
    }

    resolvedLicenseInfo.licenseInfo.declaredLicenseInfo.processed.unmapped.forEach { unmappedLicense ->
        warning(
            "The declared license '$unmappedLicense' could not be mapped to a valid license or parsed as an SPDX " +
                    "expression. The license was found in package ${pkg.metadata.id.toCoordinates()}.",
            howToFixLicense()
        )
    }
}

fun RuleSet.missingLicenseRule() = packageRule("MISSING_LICENSE") {
    require {
        -isExcluded()
        -isProjectPackage()
        // PowSyBl artifacts are MPL-2.0 by policy, even when an old release lacks <licenses> in its POM.
        -isPowsyblPackage()
    }

    if (pkg.metadata.declaredLicenses.isEmpty() && pkg.metadata.concludedLicense == null) {
        error(
            "The package ${pkg.metadata.id.toCoordinates()} does not declare any license, so it cannot be " +
                    "redistributed safely.",
            howToFixLicense()
        )
    }
}

fun RuleSet.reviewRequiredLicenseRule() = packageRule("REVIEW_REQUIRED_LICENSE") {
    require {
        -isExcluded()
    }

    licenseRule("REVIEW_REQUIRED_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            -isExcluded()
            +isReviewRequired()
        }

        warning(
            "The package ${pkg.metadata.id.toCoordinates()} uses the license $license, which requires a review by " +
                    "the PowSyBl TSC before being distributed.",
            howToFixLicense()
        )
    }
}

/**
 * Licenses found in the PowSyBl source code itself (the analyzed projects).
 */

fun RuleSet.strongCopyleftInSourceRule() = packageRule("STRONG_COPYLEFT_IN_SOURCE") {
    require {
        -isExcluded()
        +isProjectPackage()
    }

    licenseRule("STRONG_COPYLEFT_IN_SOURCE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
        require {
            -isExcluded()
            +isStrongCopyleft()
        }

        error(
            "The strong copyleft license $license was ${licenseSource.name.lowercase()} in the source code of " +
                    "${pkg.metadata.id.toCoordinates()}. This is incompatible with distributing it under " +
                    "$powsyblLicense.",
            "Remove or rewrite the affected files, or exclude them in `.ort.yml` (`excludes.paths`) if they are " +
                    "not part of the distribution (e.g. test resources)."
        )
    }
}

fun RuleSet.weakCopyleftInSourceRule() = packageRule("WEAK_COPYLEFT_IN_SOURCE") {
    require {
        -isExcluded()
        +isProjectPackage()
    }

    licenseRule("WEAK_COPYLEFT_IN_SOURCE", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            -isExcluded()
            +isWeakCopyleft()
            -isPowsyblLicense()
        }

        warning(
            "The weak copyleft license $license was ${licenseSource.name.lowercase()} in the source code of " +
                    "${pkg.metadata.id.toCoordinates()}. Third-party files copied into PowSyBl must keep their " +
                    "original license header and be listed in the NOTICE file.",
            "Check that the files are unmodified third-party code with their original header, or exclude them in " +
                    "`.ort.yml` (`excludes.paths`) if they are not distributed."
        )
    }
}

/**
 * Rules executed for each dependency of each project.
 */

fun RuleSet.strongCopyleftInDependencyRule() = dependencyRule("STRONG_COPYLEFT_IN_DEPENDENCY") {
    require {
        -isExcluded()
    }

    licenseRule("STRONG_COPYLEFT_IN_DEPENDENCY", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            +isStrongCopyleft()
        }

        error(
            "The project ${project.id.toCoordinates()} depends on ${pkg.metadata.id.toCoordinates()} which is " +
                    "licensed under the strong copyleft license $license.",
            howToFixLicense()
        )
    }
}

fun RuleSet.weakCopyleftStaticallyLinkedRule() = dependencyRule("WEAK_COPYLEFT_STATICALLY_LINKED") {
    // Maven dependencies are dynamically linked from ORT's point of view; this rule only fires for
    // shaded/embedded code or other package managers.
    require {
        -isExcluded()
        +isStaticallyLinked()
    }

    licenseRule("WEAK_COPYLEFT_STATICALLY_LINKED", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            +isWeakCopyleft()
            -isPowsyblLicense()
        }

        warning(
            "The project ${project.id.toCoordinates()} statically links ${pkg.metadata.id.toCoordinates()} which " +
                    "is licensed under the weak copyleft license $license.",
            "Make sure the license obligations (source availability of the linked library) are fulfilled."
        )
    }
}

/**
 * Vulnerability rules (require the "advisor" step).
 */

fun RuleSet.vulnerabilityInPackageRule() = packageRule("VULNERABILITY_IN_PACKAGE") {
    require {
        -isExcluded()
        +hasVulnerability()
    }

    issue(
        Severity.WARNING,
        "The package ${pkg.metadata.id.toCoordinates()} has a known vulnerability.",
        howToFixVulnerability()
    )
}

fun RuleSet.highSeverityVulnerabilityInPackageRule() = packageRule("HIGH_SEVERITY_VULNERABILITY_IN_PACKAGE") {
    require {
        -isExcluded()
        +hasVulnerability(vulnerabilityScoreThreshold, vulnerabilityScoringSystem)
    }

    issue(
        Severity.ERROR,
        "The package ${pkg.metadata.id.toCoordinates()} has a vulnerability with $vulnerabilityScoringSystem " +
                "score >= $vulnerabilityScoreThreshold.",
        howToFixVulnerability()
    )
}

/**
 * Rules executed once.
 */

fun RuleSet.deprecatedScopeExcludeReasonInOrtYmlRule() = ortResultRule("DEPRECATED_SCOPE_EXCLUDE_REASON_IN_ORT_YML") {
    val reasons = ortResult.repository.config.excludes.scopes.mapTo(mutableSetOf()) { it.reason }

    @Suppress("DEPRECATION")
    val deprecatedReasons = setOf(ScopeExcludeReason.TEST_TOOL_OF)

    reasons.intersect(deprecatedReasons).forEach { offendingReason ->
        warning(
            "The repository configuration is using the deprecated scope exclude reason '$offendingReason'.",
            "Please use only non-deprecated scope exclude reasons, see " +
                    "https://github.com/oss-review-toolkit/ort/blob/main/model/src/main/" +
                    "kotlin/config/ScopeExcludeReason.kt."
        )
    }
}

/**
 * Repository hygiene rules (executed once on the project source code).
 */

fun RuleSet.dependencyInProjectSourceRule() = projectSourceRule("DEPENDENCY_IN_PROJECT_SOURCE") {
    val denyDirPatterns = listOf(
        "**/node_modules" to setOf("NPM", "Yarn", "PNPM")
    )

    denyDirPatterns.forEach { (pattern, packageManagers) ->
        val offendingDirs = projectSourceFindDirectories(pattern)

        if (offendingDirs.isNotEmpty()) {
            error(
                "The directories ${offendingDirs.joinToString()} belong to the package manager(s) " +
                        "${packageManagers.joinToString()} and must not be committed.",
                "Please delete the directories: ${offendingDirs.joinToString()}."
            )
        }
    }
}

fun RuleSet.missingCiConfigurationRule() = projectSourceRule("MISSING_CI_CONFIGURATION") {
    require {
        -projectSourceHasDirectory(".github/workflows")
    }

    error(
        message = "This project does not have any GitHub Actions workflow.",
        howToFix = "Please add the PowSyBl CI workflows in '.github/workflows'."
    )
}

fun RuleSet.missingReadmeFileRule() = projectSourceRule("MISSING_README_FILE") {
    require {
        -projectSourceHasFile("README.md")
    }

    error("The project's code repository does not contain the file 'README.md'.")
}

fun RuleSet.missingLicenseFileRule() = projectSourceRule("MISSING_LICENSE_FILE") {
    require {
        -projectSourceHasFile("LICENSE")
    }

    error(
        message = "The project's code repository does not contain the file 'LICENSE'.",
        howToFix = "Please add the $powsyblLicense license text in a 'LICENSE' file at the root of the repository."
    )
}

fun RuleSet.wrongLicenseInLicenseFileRule() = projectSourceRule("WRONG_LICENSE_IN_LICENSE_FILE") {
    require {
        +projectSourceHasFile("LICENSE")
    }

    val detectedRootLicenses = projectSourceGetDetectedLicensesByFilePath("LICENSE").values.flatten().toSet()
    val wrongLicenses = detectedRootLicenses - allowedRootLicenses

    if (wrongLicenses.isNotEmpty()) {
        error(
            message = "The file 'LICENSE' contains the following disallowed licenses: ${wrongLicenses.joinToString()}.",
            howToFix = "PowSyBl repositories must be licensed under: ${allowedRootLicenses.joinToString()}."
        )
    } else if (detectedRootLicenses.isEmpty()) {
        warning(
            message = "No license could be detected in the file 'LICENSE' (was the project source code scanned?).",
            howToFix = "Please put the full $powsyblLicense license text in the file 'LICENSE'."
        )
    }
}

// Contribution guidelines and the README license section are usually provided at the organization level
// (https://github.com/powsybl/.github), so they are only reported as hints.
fun RuleSet.missingContributingFileRule() = projectSourceRule("MISSING_CONTRIBUTING_FILE") {
    require {
        -projectSourceHasFile("CONTRIBUTING.md", ".github/CONTRIBUTING.md", "docs/CONTRIBUTING.md")
    }

    hint(
        "The repository has no CONTRIBUTING.md file; the organization-level one from powsybl/.github applies.",
        "Add a CONTRIBUTING.md file only if this repository has specific contribution rules."
    )
}

fun RuleSet.missingReadmeFileLicenseSectionRule() = projectSourceRule("MISSING_README_FILE_LICENSE_SECTION") {
    require {
        +projectSourceHasFile("README.md")
        -projectSourceHasFileWithContent("(?i).*^#{1,3} +licen[sc]e *$.*", "README.md")
    }

    hint(
        message = "The file 'README.md' has no \"License\" section.",
        howToFix = "Consider adding a \"License\" section stating that the project is licensed under $powsyblLicense."
    )
}

/**
 * The set of policy rules.
 */
val ruleSet = ruleSet(ortResult, licenseInfoResolver, resolutionProvider) {
    // Rules executed for each package and project:
    unhandledLicenseRule()
    unmappedDeclaredLicenseRule()
    missingLicenseRule()
    reviewRequiredLicenseRule()
    strongCopyleftInSourceRule()
    weakCopyleftInSourceRule()
    vulnerabilityInPackageRule()
    highSeverityVulnerabilityInPackageRule()

    // Rules executed for each dependency of each project:
    strongCopyleftInDependencyRule()
    weakCopyleftStaticallyLinkedRule()

    // Rules executed once:
    deprecatedScopeExcludeReasonInOrtYmlRule()

    // Repository hygiene rules (executed once):
    dependencyInProjectSourceRule()
    missingCiConfigurationRule()
    missingReadmeFileRule()
    missingLicenseFileRule()
    wrongLicenseInLicenseFileRule()
    missingContributingFileRule()
    missingReadmeFileLicenseSectionRule()
}

// Populate the list of policy rule violations to return.
ruleViolations += ruleSet.violations
