/**
 * @author xuy.
 * Copyright (c) Ai.codes
 *
 * ep = "Extension Point".
 * AI.codes plugin for IntelliJ has multiple extension points. This package contains all these
 * compoenets that are referenced by plugin.xml. They are:
 *
 *  * AiPluginComponent: registered to IntelliJ as a plugin component.
 *       It manages the life cycle of plugins and resources. The object itself is a singleton.
 *
 *  * AiSnippetContributor: registered to IntelliJ as a CompletionContributor.
 *      It provides additional AI-based candidates to IntelliJ.
 *
 *  * AiWeigher: registered to IntelliJ as a CompletionWeigher.
 *      It takes care of weighting of code completion items.
 */
package codes.ai.ep;